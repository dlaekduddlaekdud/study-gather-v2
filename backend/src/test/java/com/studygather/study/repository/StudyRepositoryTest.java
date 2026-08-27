package com.studygather.study.repository;

import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyStatus;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class StudyRepositoryTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindStudy() {
        User owner = userRepository.saveAndFlush(User.create(
                "study-owner@example.com",
                "encoded-password",
                "study-owner"
        ));
        LocalDateTime recruitmentDeadline = LocalDateTime.now().plusDays(7).withNano(0);
        Study study = Study.create(
                owner,
                "Spring Boot 스터디",
                "함께 Spring Boot를 공부합니다.",
                5,
                recruitmentDeadline
        );

        Study savedStudy = studyRepository.saveAndFlush(study);
        Long studyId = savedStudy.getId();
        Long ownerId = owner.getId();
        entityManager.clear();

        Study foundStudy = studyRepository.findById(studyId).orElseThrow();

        assertEquals(ownerId, foundStudy.getOwner().getId());
        assertEquals("Spring Boot 스터디", foundStudy.getTitle());
        assertEquals("함께 Spring Boot를 공부합니다.", foundStudy.getDescription());
        assertEquals(5, foundStudy.getCapacity());
        assertEquals(1, foundStudy.getApprovedCount());
        assertEquals(recruitmentDeadline, foundStudy.getRecruitmentDeadline());
        assertEquals(StudyStatus.OPEN, foundStudy.getStatus());
        assertNotNull(foundStudy.getCreatedAt());
        assertNotNull(foundStudy.getUpdatedAt());
    }
}
