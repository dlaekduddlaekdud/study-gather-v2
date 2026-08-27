package com.studygather.application.service;

import com.studygather.application.dto.request.CreateApplicationRequest;
import com.studygather.application.dto.response.ApplicationResponse;
import com.studygather.application.dto.response.ApplicationListResponse;
import com.studygather.application.entity.ApplicationStatus;
import com.studygather.application.exception.ApplicationApplicantRequiredException;
import com.studygather.application.exception.ApplicationAlreadyExistsException;
import com.studygather.application.exception.ApplicationNotFoundException;
import com.studygather.application.exception.InvalidApplicationStatusException;
import com.studygather.application.exception.StudyOwnerCannotApplyException;
import com.studygather.application.repository.StudyApplicationRepository;
import com.studygather.study.entity.StudyMember;
import com.studygather.study.entity.StudyMemberRole;
import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.exception.StudyCapacityExceededException;
import com.studygather.study.exception.StudyClosedException;
import com.studygather.study.exception.StudyOwnerRequiredException;
import com.studygather.study.repository.StudyMemberRepository;
import com.studygather.study.repository.StudyRepository;
import com.studygather.study.service.StudyService;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StudyApplicationServiceTest {

    @Autowired
    private StudyApplicationService applicationService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyApplicationRepository applicationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Test
    void createApplicationSavesPendingApplication() {
        TestData testData = createTestData("application-service");

        ApplicationResponse response = applicationService.createApplication(
                testData.applicant().getId(),
                testData.study().id(),
                new CreateApplicationRequest("꾸준히 참여하겠습니다.")
        );

        assertNotNull(response.id());
        assertEquals(testData.study().id(), response.studyId());
        assertEquals(testData.applicant().getId(), response.applicantId());
        assertEquals(ApplicationStatus.PENDING, response.status());
        assertNotNull(response.createdAt());
    }

    @Test
    void createApplicationRejectsStudyOwner() {
        TestData testData = createTestData("owner-application");

        assertThrows(
                StudyOwnerCannotApplyException.class,
                () -> applicationService.createApplication(
                        testData.study().ownerId(),
                        testData.study().id(),
                        new CreateApplicationRequest("본인 스터디 신청")
                )
        );
    }

    @Test
    void createApplicationRejectsDuplicateApplication() {
        TestData testData = createTestData("duplicate-service");
        CreateApplicationRequest request = new CreateApplicationRequest("중복 신청 테스트");
        applicationService.createApplication(
                testData.applicant().getId(),
                testData.study().id(),
                request
        );

        assertThrows(
                ApplicationAlreadyExistsException.class,
                () -> applicationService.createApplication(
                        testData.applicant().getId(),
                        testData.study().id(),
                        request
                )
        );
    }

    @Test
    void createApplicationRejectsClosedStudy() {
        TestData testData = createTestData("closed-application");
        studyService.closeStudy(testData.study().ownerId(), testData.study().id());

        assertThrows(
                StudyClosedException.class,
                () -> applicationService.createApplication(
                        testData.applicant().getId(),
                        testData.study().id(),
                        new CreateApplicationRequest("마감된 스터디 신청")
                )
        );
    }

    @Test
    void createApplicationRejectsExpiredStudy() {
        User owner = saveUser("expired-application-owner");
        User applicant = saveUser("expired-application-applicant");
        StudyResponse expiredStudy = studyService.createStudy(
                owner.getId(),
                new CreateStudyRequest(
                        "마감일이 지난 스터디",
                        "마감일 검증 테스트입니다.",
                        5,
                        LocalDateTime.now().minusDays(1).withNano(0)
                )
        );

        assertThrows(
                StudyClosedException.class,
                () -> applicationService.createApplication(
                        applicant.getId(),
                        expiredStudy.id(),
                        new CreateApplicationRequest("마감일이 지난 후 신청")
                )
        );
    }

    @Test
    void getStudyApplicationsReturnsApplicationsToOwner() {
        TestData testData = createTestData("owner-list");
        applicationService.createApplication(
                testData.applicant().getId(),
                testData.study().id(),
                new CreateApplicationRequest("목록 조회 신청")
        );

        List<ApplicationListResponse> response = applicationService.getStudyApplications(
                testData.study().ownerId(),
                testData.study().id()
        );

        assertEquals(1, response.size());
        assertEquals(testData.applicant().getNickname(), response.get(0).applicantNickname());
    }

    @Test
    void getStudyApplicationsRejectsNonOwner() {
        TestData testData = createTestData("non-owner-list");

        assertThrows(
                StudyOwnerRequiredException.class,
                () -> applicationService.getStudyApplications(
                        testData.applicant().getId(),
                        testData.study().id()
                )
        );
    }

    @Test
    void getMyApplicationsReturnsOnlyApplicantsApplications() {
        TestData firstStudy = createTestData("my-list-first");
        User secondOwner = saveUser("my-list-second-owner");
        StudyResponse secondStudy = studyService.createStudy(
                secondOwner.getId(),
                new CreateStudyRequest(
                        "두 번째 신청 스터디",
                        "내 신청 목록 테스트입니다.",
                        5,
                        LocalDateTime.now().plusDays(7).withNano(0)
                )
        );
        applicationService.createApplication(
                firstStudy.applicant().getId(),
                firstStudy.study().id(),
                new CreateApplicationRequest("첫 번째 신청")
        );
        applicationService.createApplication(
                firstStudy.applicant().getId(),
                secondStudy.id(),
                new CreateApplicationRequest("두 번째 신청")
        );

        List<ApplicationListResponse> response = applicationService.getMyApplications(
                firstStudy.applicant().getId()
        );

        assertEquals(2, response.size());
        assertEquals(secondStudy.id(), response.get(0).studyId());
        assertEquals(firstStudy.study().id(), response.get(1).studyId());
    }

    @Test
    void cancelApplicationChangesPendingStatusToCanceled() {
        TestData testData = createTestData("cancel-service");
        ApplicationResponse application = applicationService.createApplication(
                testData.applicant().getId(),
                testData.study().id(),
                new CreateApplicationRequest("취소할 신청")
        );

        ApplicationResponse response = applicationService.cancelApplication(
                testData.applicant().getId(),
                application.id()
        );

        assertEquals(ApplicationStatus.CANCELED, response.status());
    }

    @Test
    void cancelApplicationRejectsOtherUser() {
        TestData testData = createTestData("cancel-other-user");
        User otherUser = saveUser("cancel-unrelated-user");
        ApplicationResponse application = applicationService.createApplication(
                testData.applicant().getId(),
                testData.study().id(),
                new CreateApplicationRequest("본인만 취소 가능")
        );

        assertThrows(
                ApplicationApplicantRequiredException.class,
                () -> applicationService.cancelApplication(otherUser.getId(), application.id())
        );
    }

    @Test
    void cancelApplicationRejectsAlreadyCanceledApplication() {
        TestData testData = createTestData("cancel-twice");
        ApplicationResponse application = applicationService.createApplication(
                testData.applicant().getId(),
                testData.study().id(),
                new CreateApplicationRequest("두 번 취소할 수 없음")
        );
        applicationService.cancelApplication(testData.applicant().getId(), application.id());

        assertThrows(
                InvalidApplicationStatusException.class,
                () -> applicationService.cancelApplication(
                        testData.applicant().getId(),
                        application.id()
                )
        );
    }

    @Test
    void cancelApplicationRejectsUnknownApplication() {
        assertThrows(
                ApplicationNotFoundException.class,
                () -> applicationService.cancelApplication(1L, Long.MAX_VALUE)
        );
    }

    @Test
    void approveApplicationCreatesMemberAndIncreasesApprovedCount() {
        TestData testData = createTestData("approve-service");
        ApplicationResponse application = createApplication(testData, "승인할 신청");

        ApplicationResponse response = applicationService.approveApplication(
                testData.study().ownerId(),
                application.id()
        );

        StudyMember member = studyMemberRepository.findByStudyIdAndUserId(
                        testData.study().id(),
                        testData.applicant().getId()
                )
                .orElseThrow();
        int approvedCount = studyRepository.findById(testData.study().id())
                .orElseThrow()
                .getApprovedCount();

        assertEquals(ApplicationStatus.APPROVED, response.status());
        assertNotNull(response.decidedAt());
        assertEquals(StudyMemberRole.MEMBER, member.getMemberRole());
        assertEquals(2, approvedCount);
    }

    @Test
    void rejectApplicationChangesStatusWithoutCreatingMember() {
        TestData testData = createTestData("reject-service");
        ApplicationResponse application = createApplication(testData, "거절할 신청");

        ApplicationResponse response = applicationService.rejectApplication(
                testData.study().ownerId(),
                application.id()
        );

        assertEquals(ApplicationStatus.REJECTED, response.status());
        assertNotNull(response.decidedAt());
        assertEquals(
                1,
                studyRepository.findById(testData.study().id()).orElseThrow().getApprovedCount()
        );
        assertEquals(
                false,
                studyMemberRepository.existsByStudyIdAndUserId(
                        testData.study().id(),
                        testData.applicant().getId()
                )
        );
    }

    @Test
    void approveApplicationRejectsNonOwner() {
        TestData testData = createTestData("approve-non-owner");
        User otherUser = saveUser("approve-unrelated-user");
        ApplicationResponse application = createApplication(testData, "권한 없는 승인");

        assertThrows(
                StudyOwnerRequiredException.class,
                () -> applicationService.approveApplication(otherUser.getId(), application.id())
        );
    }

    @Test
    void approveApplicationRejectsWhenCapacityIsFull() {
        User owner = saveUser("capacity-owner");
        User firstApplicant = saveUser("capacity-first-applicant");
        User secondApplicant = saveUser("capacity-second-applicant");
        StudyResponse study = studyService.createStudy(owner.getId(), new CreateStudyRequest(
                "정원 검증 스터디",
                "승인 정원 검증 테스트입니다.",
                2,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));
        ApplicationResponse firstApplication = applicationService.createApplication(
                firstApplicant.getId(),
                study.id(),
                new CreateApplicationRequest("첫 번째 신청")
        );
        ApplicationResponse secondApplication = applicationService.createApplication(
                secondApplicant.getId(),
                study.id(),
                new CreateApplicationRequest("두 번째 신청")
        );
        applicationService.approveApplication(owner.getId(), firstApplication.id());

        assertThrows(
                StudyCapacityExceededException.class,
                () -> applicationService.approveApplication(owner.getId(), secondApplication.id())
        );
        assertEquals(
                ApplicationStatus.PENDING,
                applicationRepository.findById(secondApplication.id()).orElseThrow().getStatus()
        );
        assertEquals(
                false,
                studyMemberRepository.existsByStudyIdAndUserId(study.id(), secondApplicant.getId())
        );
    }

    @Test
    void rejectApplicationRejectsAlreadyDecidedApplication() {
        TestData testData = createTestData("reject-decided");
        ApplicationResponse application = createApplication(testData, "한 번만 결정 가능");
        applicationService.rejectApplication(testData.study().ownerId(), application.id());

        assertThrows(
                InvalidApplicationStatusException.class,
                () -> applicationService.rejectApplication(
                        testData.study().ownerId(),
                        application.id()
                )
        );
    }

    private ApplicationResponse createApplication(TestData testData, String message) {
        return applicationService.createApplication(
                testData.applicant().getId(),
                testData.study().id(),
                new CreateApplicationRequest(message)
        );
    }

    private TestData createTestData(String prefix) {
        User owner = saveUser(prefix + "-owner");
        User applicant = saveUser(prefix + "-applicant");
        StudyResponse study = studyService.createStudy(owner.getId(), new CreateStudyRequest(
                prefix + " 스터디",
                "Service 테스트용 스터디입니다.",
                5,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));

        return new TestData(study, applicant);
    }

    private User saveUser(String prefix) {
        return userRepository.saveAndFlush(User.create(
                prefix + "@example.com",
                "encoded-password",
                prefix
        ));
    }

    private record TestData(StudyResponse study, User applicant) {
    }
}
