package com.studygather.study.repository;

import com.studygather.study.entity.StudyMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    Optional<StudyMember> findByStudyIdAndUserId(Long studyId, Long userId);

    boolean existsByStudyIdAndUserId(Long studyId, Long userId);

    @EntityGraph(attributePaths = "user")
    List<StudyMember> findAllByStudyIdOrderByJoinedAtAscIdAsc(Long studyId);

    List<StudyMember> findAllByStudyId(Long studyId);
}
