package com.studygather.study.service;

import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.entity.StudyMember;
import com.studygather.study.entity.StudyMemberRole;
import com.studygather.study.entity.StudyStatus;
import com.studygather.study.repository.StudyMemberRepository;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class StudyServiceTest {

    @Autowired
    private StudyService studyService;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createStudySavesOwnerMemberTogether() {
        User owner = userRepository.saveAndFlush(User.create(
                "study-service-owner@example.com",
                "encoded-password",
                "study-service-owner"
        ));
        CreateStudyRequest request = new CreateStudyRequest(
                "JPA 스터디",
                "JPA의 영속성 컨텍스트를 공부합니다.",
                4,
                LocalDateTime.now().plusDays(7).withNano(0)
        );

        StudyResponse response = studyService.createStudy(owner.getId(), request);
        StudyMember ownerMember = studyMemberRepository
                .findByStudyIdAndUserId(response.id(), owner.getId())
                .orElseThrow();

        assertNotNull(response.id());
        assertEquals(owner.getId(), response.ownerId());
        assertEquals(1, response.approvedCount());
        assertEquals(StudyStatus.OPEN, response.status());
        assertEquals(StudyMemberRole.OWNER, ownerMember.getMemberRole());
        assertNotNull(ownerMember.getJoinedAt());
    }
}
