package com.studygather.study.repository;

import com.studygather.study.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    Optional<StudyMember> findByStudyIdAndUserId(Long studyId, Long userId);

    boolean existsByStudyIdAndUserId(Long studyId, Long userId);

    List<StudyMember> findAllByStudyId(Long studyId);
}
