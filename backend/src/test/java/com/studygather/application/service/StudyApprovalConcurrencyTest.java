package com.studygather.application.service;

import com.studygather.application.dto.request.CreateApplicationRequest;
import com.studygather.application.entity.ApplicationStatus;
import com.studygather.application.repository.StudyApplicationRepository;
import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.exception.StudyCapacityExceededException;
import com.studygather.study.repository.StudyMemberRepository;
import com.studygather.study.repository.StudyRepository;
import com.studygather.study.service.StudyService;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.datasource.hikari.maximum-pool-size=25")
class StudyApprovalConcurrencyTest {

    private static final int REQUEST_COUNT = 20;

    @Autowired
    private StudyApplicationService applicationService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private StudyApplicationRepository applicationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final List<Long> createdUserIds = new ArrayList<>();
    private Long studyId;

    @RepeatedTest(20)
    void onlyOneConcurrentApprovalSucceedsForLastSeat() throws Exception {
        TestData testData = createTestData();
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<ApprovalResult>> futures = testData.applicationIds().stream()
                    .map(applicationId -> executor.submit(() -> {
                        start.await();
                        try {
                            applicationService.approveApplication(testData.ownerId(), applicationId);
                            return ApprovalResult.APPROVED;
                        } catch (StudyCapacityExceededException exception) {
                            return ApprovalResult.CAPACITY_EXCEEDED;
                        }
                    }))
                    .toList();

            start.countDown();

            List<ApprovalResult> results = new ArrayList<>();
            for (Future<ApprovalResult> future : futures) {
                results.add(future.get());
            }

            assertEquals(1, results.stream().filter(ApprovalResult.APPROVED::equals).count());
            assertEquals(
                    REQUEST_COUNT - 1,
                    results.stream().filter(ApprovalResult.CAPACITY_EXCEEDED::equals).count()
            );
            verifyStoredResult();
        } finally {
            executor.shutdownNow();
        }
    }

    private TestData createTestData() {
        return transactionTemplate.execute(status -> {
            String uniquePrefix = "concurrency-" + UUID.randomUUID();
            User owner = saveUser(uniquePrefix + "-owner");
            StudyResponse study = studyService.createStudy(owner.getId(), new CreateStudyRequest(
                    "동시 승인 테스트",
                    "마지막 한 자리 동시 승인 검증입니다.",
                    2,
                    LocalDateTime.now().plusDays(7).withNano(0)
            ));
            studyId = study.id();

            List<Long> applicationIds = new ArrayList<>();
            for (int index = 0; index < REQUEST_COUNT; index++) {
                User applicant = saveUser(uniquePrefix + "-applicant-" + index);
                Long applicationId = applicationService.createApplication(
                        applicant.getId(),
                        study.id(),
                        new CreateApplicationRequest("동시 승인 신청 " + index)
                ).id();
                applicationIds.add(applicationId);
            }

            return new TestData(owner.getId(), applicationIds);
        });
    }

    private User saveUser(String prefix) {
        User user = userRepository.save(User.create(
                prefix + "@example.com",
                "encoded-password",
                prefix.substring(0, Math.min(prefix.length(), 50))
        ));
        userRepository.flush();
        createdUserIds.add(user.getId());
        return user;
    }

    private void verifyStoredResult() {
        transactionTemplate.executeWithoutResult(status -> {
            long approvedApplications = applicationRepository
                    .findAllByStudyIdOrderByCreatedAtAsc(studyId)
                    .stream()
                    .filter(application -> application.getStatus() == ApplicationStatus.APPROVED)
                    .count();

            assertEquals(1, approvedApplications);
            assertEquals(2, studyRepository.findById(studyId).orElseThrow().getApprovedCount());
            assertEquals(2, studyMemberRepository.findAllByStudyId(studyId).size());
        });
    }

    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            if (studyId != null) {
                applicationRepository.deleteAll(
                        applicationRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId)
                );
                applicationRepository.flush();
                studyMemberRepository.deleteAll(studyMemberRepository.findAllByStudyId(studyId));
                studyMemberRepository.flush();
                studyRepository.deleteById(studyId);
                studyRepository.flush();
            }
            userRepository.deleteAllById(createdUserIds);
        });
    }

    private enum ApprovalResult {
        APPROVED,
        CAPACITY_EXCEEDED
    }

    private record TestData(Long ownerId, List<Long> applicationIds) {
    }
}
