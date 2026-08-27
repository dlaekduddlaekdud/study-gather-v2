package com.studygather.application.service;

import com.studygather.application.dto.request.CreateApplicationRequest;
import com.studygather.application.dto.response.ApplicationResponse;
import com.studygather.application.entity.ApplicationStatus;
import com.studygather.application.exception.ApplicationAlreadyExistsException;
import com.studygather.application.exception.StudyOwnerCannotApplyException;
import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.exception.StudyClosedException;
import com.studygather.study.service.StudyService;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
