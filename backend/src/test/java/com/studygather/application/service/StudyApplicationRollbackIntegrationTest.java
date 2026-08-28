package com.studygather.application.service;

import com.studygather.application.dto.request.CreateApplicationRequest;
import com.studygather.application.dto.response.ApplicationResponse;
import com.studygather.application.entity.ApplicationStatus;
import com.studygather.application.repository.StudyApplicationRepository;
import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.entity.StudyMember;
import com.studygather.study.repository.StudyMemberRepository;
import com.studygather.study.repository.StudyRepository;
import com.studygather.study.service.StudyService;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@SpringBootTest
class StudyApplicationRollbackIntegrationTest {

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

    @MockitoSpyBean
    private StudyMemberRepository studyMemberRepository;

    private Long applicationId;
    private Long studyId;
    private Long ownerId;
    private Long applicantId;

    @AfterEach
    void cleanUp() {
        reset(studyMemberRepository);

        if (applicationId != null && applicationRepository.existsById(applicationId)) {
            applicationRepository.deleteById(applicationId);
        }
        if (studyId != null && studyRepository.existsById(studyId)) {
            studyMemberRepository.deleteAll(studyMemberRepository.findAllByStudyId(studyId));
            studyRepository.deleteById(studyId);
        }

        List<Long> userIds = Stream.of(ownerId, applicantId)
                .filter(id -> id != null && userRepository.existsById(id))
                .toList();
        userRepository.deleteAllById(userIds);
    }

    @Test
    void approveApplicationRollsBackAllChangesWhenMemberSaveFails() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        User owner = saveUser("rollback-owner-" + uniqueSuffix);
        User applicant = saveUser("rollback-applicant-" + uniqueSuffix);
        ownerId = owner.getId();
        applicantId = applicant.getId();

        StudyResponse study = studyService.createStudy(ownerId, new CreateStudyRequest(
                "승인 롤백 검증 스터디",
                "승인 도중 실패하면 모든 변경이 롤백되는지 검증합니다.",
                3,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));
        studyId = study.id();

        ApplicationResponse application = applicationService.createApplication(
                applicantId,
                studyId,
                new CreateApplicationRequest("롤백 검증 신청")
        );
        applicationId = application.id();

        doThrow(new IllegalStateException("멤버 저장 강제 실패"))
                .when(studyMemberRepository)
                .saveAndFlush(any(StudyMember.class));

        assertThrows(
                IllegalStateException.class,
                () -> applicationService.approveApplication(ownerId, applicationId)
        );

        assertEquals(
                ApplicationStatus.PENDING,
                applicationRepository.findById(applicationId).orElseThrow().getStatus()
        );
        assertEquals(
                1,
                studyRepository.findById(studyId).orElseThrow().getApprovedCount()
        );
        assertFalse(studyMemberRepository.existsByStudyIdAndUserId(studyId, applicantId));
    }

    private User saveUser(String prefix) {
        return userRepository.saveAndFlush(User.create(
                prefix + "@example.com",
                "encoded-password",
                prefix
        ));
    }
}
