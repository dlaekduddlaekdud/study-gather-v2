package com.studygather.application.repository;

import com.studygather.application.entity.ApplicationStatus;
import com.studygather.application.entity.StudyApplication;
import com.studygather.study.entity.Study;
import com.studygather.study.repository.StudyRepository;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StudyApplicationRepositoryTest {

    @Autowired
    private StudyApplicationRepository applicationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesPendingApplication() {
        TestData testData = createTestData("application-repository");
        StudyApplication application = StudyApplication.create(
                testData.study(),
                testData.applicant(),
                "참여하고 싶습니다."
        );

        StudyApplication savedApplication = applicationRepository.saveAndFlush(application);

        assertNotNull(savedApplication.getId());
        assertEquals(ApplicationStatus.PENDING, savedApplication.getStatus());
        assertEquals("참여하고 싶습니다.", savedApplication.getMessage());
        assertNotNull(savedApplication.getCreatedAt());
        assertNotNull(savedApplication.getUpdatedAt());
    }

    @Test
    void rejectsDuplicateApplicationForSameStudyAndApplicant() {
        TestData testData = createTestData("duplicate-application");
        applicationRepository.saveAndFlush(StudyApplication.create(
                testData.study(),
                testData.applicant(),
                "첫 번째 신청"
        ));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> applicationRepository.saveAndFlush(StudyApplication.create(
                        testData.study(),
                        testData.applicant(),
                        "두 번째 신청"
                ))
        );
    }

    private TestData createTestData(String prefix) {
        User owner = userRepository.saveAndFlush(User.create(
                prefix + "-owner@example.com",
                "encoded-password",
                prefix + "-owner"
        ));
        User applicant = userRepository.saveAndFlush(User.create(
                prefix + "-applicant@example.com",
                "encoded-password",
                prefix + "-applicant"
        ));
        Study study = studyRepository.saveAndFlush(Study.create(
                owner,
                prefix + " 스터디",
                "Repository 테스트용 스터디입니다.",
                5,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));

        return new TestData(study, applicant);
    }

    private record TestData(Study study, User applicant) {
    }
}
