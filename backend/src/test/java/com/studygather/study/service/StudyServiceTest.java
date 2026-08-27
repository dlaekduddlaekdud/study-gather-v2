package com.studygather.study.service;

import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.dto.response.StudySummaryResponse;
import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyMember;
import com.studygather.study.entity.StudyMemberRole;
import com.studygather.study.entity.StudyStatus;
import com.studygather.study.exception.StudyNotFoundException;
import com.studygather.study.repository.StudyMemberRepository;
import com.studygather.study.repository.StudyRepository;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class StudyServiceTest {

    @Autowired
    private StudyService studyService;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private StudyRepository studyRepository;

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

    @Test
    void getOpenStudiesReturnsOnlyRecruitingStudiesInDeadlineOrder() {
        User owner = saveOwner("study-list-owner@example.com", "study-list-owner");
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Study laterStudy = Study.create(
                owner,
                "나중에 마감되는 스터디",
                "두 번째로 조회되어야 합니다.",
                5,
                now.plusDays(10)
        );
        Study expiredStudy = Study.create(
                owner,
                "이미 마감된 스터디",
                "목록에서 제외되어야 합니다.",
                5,
                now.minusDays(1)
        );
        Study earlierStudy = Study.create(
                owner,
                "먼저 마감되는 스터디",
                "첫 번째로 조회되어야 합니다.",
                5,
                now.plusDays(3)
        );
        studyRepository.saveAllAndFlush(List.of(laterStudy, expiredStudy, earlierStudy));

        List<StudySummaryResponse> response = studyService.getOpenStudies();
        List<Long> studyIds = response.stream()
                .map(StudySummaryResponse::id)
                .toList();

        assertTrue(studyIds.contains(earlierStudy.getId()));
        assertTrue(studyIds.contains(laterStudy.getId()));
        assertFalse(studyIds.contains(expiredStudy.getId()));
        assertTrue(
                studyIds.indexOf(earlierStudy.getId()) < studyIds.indexOf(laterStudy.getId())
        );
    }

    @Test
    void getStudyReturnsStudyDetails() {
        User owner = saveOwner("study-detail-owner@example.com", "study-detail-owner");
        Study study = studyRepository.saveAndFlush(Study.create(
                owner,
                "상세 조회 스터디",
                "상세 설명입니다.",
                3,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));

        StudyResponse response = studyService.getStudy(study.getId());

        assertEquals(study.getId(), response.id());
        assertEquals(owner.getId(), response.ownerId());
        assertEquals("상세 설명입니다.", response.description());
    }

    @Test
    void getStudyRejectsUnknownStudy() {
        assertThrows(
                StudyNotFoundException.class,
                () -> studyService.getStudy(Long.MAX_VALUE)
        );
    }

    private User saveOwner(String email, String nickname) {
        return userRepository.saveAndFlush(User.create(
                email,
                "encoded-password",
                nickname
        ));
    }
}
