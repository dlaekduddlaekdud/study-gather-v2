package com.studygather.study.repository;

import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyMember;
import com.studygather.support.MySqlTestcontainersConfiguration;
import com.studygather.user.entity.User;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("testcontainers")
@Import(MySqlTestcontainersConfiguration.class)
@Transactional
class StudyMemberRepositoryTest {

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void rejectsDuplicateMemberForSameStudyAndUser() {
        User owner = saveUser("member-owner@example.com", "member-owner");
        User member = saveUser("duplicate-member@example.com", "duplicate-member");
        Study study = studyRepository.saveAndFlush(Study.create(
                owner,
                "멤버 중복 검증 스터디",
                "동일한 사용자가 두 번 멤버로 저장되지 않아야 합니다.",
                5,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));
        studyMemberRepository.saveAndFlush(StudyMember.createMember(study, member));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> studyMemberRepository.saveAndFlush(
                        StudyMember.createMember(study, member)
                )
        );
    }

    private User saveUser(String email, String nickname) {
        return userRepository.saveAndFlush(User.create(
                email,
                "encoded-password",
                nickname
        ));
    }
}
