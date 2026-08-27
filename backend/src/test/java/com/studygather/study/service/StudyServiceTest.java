package com.studygather.study.service;

import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.request.UpdateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.dto.response.StudyMemberResponse;
import com.studygather.study.dto.response.StudySummaryResponse;
import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyMember;
import com.studygather.study.entity.StudyMemberRole;
import com.studygather.study.entity.StudyStatus;
import com.studygather.study.exception.StudyClosedException;
import com.studygather.study.exception.StudyNotFoundException;
import com.studygather.study.exception.StudyOwnerRequiredException;
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

    @Test
    void getStudyMembersReturnsOwnerAndApprovedMember() {
        User owner = saveOwner("member-list-owner@example.com", "member-list-owner");
        User memberUser = saveOwner("member-list-user@example.com", "member-list-user");
        StudyResponse studyResponse = studyService.createStudy(owner.getId(), new CreateStudyRequest(
                "멤버 목록 스터디",
                "멤버 목록 조회 테스트입니다.",
                5,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));
        Study study = studyRepository.findById(studyResponse.id()).orElseThrow();
        studyMemberRepository.saveAndFlush(StudyMember.createMember(study, memberUser));

        List<StudyMemberResponse> response = studyService.getStudyMembers(
                owner.getId(),
                study.getId()
        );

        assertEquals(2, response.size());
        assertEquals(StudyMemberRole.OWNER, response.get(0).memberRole());
        assertEquals(owner.getId(), response.get(0).userId());
        assertEquals(StudyMemberRole.MEMBER, response.get(1).memberRole());
        assertEquals(memberUser.getNickname(), response.get(1).nickname());
    }

    @Test
    void getStudyMembersRejectsNonOwner() {
        User owner = saveOwner("member-list-real-owner@example.com", "member-list-real-owner");
        User otherUser = saveOwner("member-list-other-user@example.com", "member-list-other-user");
        Study study = saveStudy(owner, "멤버 목록 권한 테스트", "개설자만 조회할 수 있습니다.");

        assertThrows(
                StudyOwnerRequiredException.class,
                () -> studyService.getStudyMembers(otherUser.getId(), study.getId())
        );
    }

    @Test
    void getStudyMembersRejectsUnknownStudy() {
        assertThrows(
                StudyNotFoundException.class,
                () -> studyService.getStudyMembers(1L, Long.MAX_VALUE)
        );
    }

    @Test
    void updateStudyChangesOnlyRequestedFields() {
        User owner = saveOwner("study-update-owner@example.com", "study-update-owner");
        Study study = saveStudy(owner, "수정 전 제목", "유지할 설명");
        LocalDateTime originalDeadline = study.getRecruitmentDeadline();

        StudyResponse response = studyService.updateStudy(
                owner.getId(),
                study.getId(),
                new UpdateStudyRequest("수정된 제목", null, 6, null)
        );

        assertEquals("수정된 제목", response.title());
        assertEquals("유지할 설명", response.description());
        assertEquals(6, response.capacity());
        assertEquals(originalDeadline, response.recruitmentDeadline());
    }

    @Test
    void updateStudyRejectsNonOwner() {
        User owner = saveOwner("update-real-owner@example.com", "update-real-owner");
        User otherUser = saveOwner("update-other-user@example.com", "update-other-user");
        Study study = saveStudy(owner, "원래 제목", "원래 설명");

        assertThrows(
                StudyOwnerRequiredException.class,
                () -> studyService.updateStudy(
                        otherUser.getId(),
                        study.getId(),
                        new UpdateStudyRequest("탈취한 수정", null, null, null)
                )
        );
        assertEquals("원래 제목", study.getTitle());
    }

    @Test
    void closeStudyChangesStatusToClosed() {
        User owner = saveOwner("study-close-owner@example.com", "study-close-owner");
        Study study = saveStudy(owner, "마감할 스터디", "마감 테스트");

        StudyResponse response = studyService.closeStudy(owner.getId(), study.getId());

        assertEquals(StudyStatus.CLOSED, response.status());
    }

    @Test
    void closeStudyRejectsNonOwner() {
        User owner = saveOwner("close-real-owner@example.com", "close-real-owner");
        User otherUser = saveOwner("close-other-user@example.com", "close-other-user");
        Study study = saveStudy(owner, "타인 마감 방지", "권한 테스트");

        assertThrows(
                StudyOwnerRequiredException.class,
                () -> studyService.closeStudy(otherUser.getId(), study.getId())
        );
        assertEquals(StudyStatus.OPEN, study.getStatus());
    }

    @Test
    void closeStudyRejectsAlreadyClosedStudy() {
        User owner = saveOwner("already-closed-owner@example.com", "already-closed-owner");
        Study study = saveStudy(owner, "이미 마감된 스터디", "상태 전이 테스트");
        studyService.closeStudy(owner.getId(), study.getId());

        assertThrows(
                StudyClosedException.class,
                () -> studyService.closeStudy(owner.getId(), study.getId())
        );
    }

    private User saveOwner(String email, String nickname) {
        return userRepository.saveAndFlush(User.create(
                email,
                "encoded-password",
                nickname
        ));
    }

    private Study saveStudy(User owner, String title, String description) {
        return studyRepository.saveAndFlush(Study.create(
                owner,
                title,
                description,
                5,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));
    }
}
