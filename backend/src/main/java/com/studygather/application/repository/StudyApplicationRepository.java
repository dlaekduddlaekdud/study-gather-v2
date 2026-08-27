package com.studygather.application.repository;

import com.studygather.application.entity.StudyApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    boolean existsByStudyIdAndApplicantId(Long studyId, Long applicantId);

    @EntityGraph(attributePaths = {"study", "applicant"})
    List<StudyApplication> findAllByStudyIdOrderByCreatedAtAsc(Long studyId);

    @EntityGraph(attributePaths = {"study", "applicant"})
    List<StudyApplication> findAllByApplicantIdOrderByCreatedAtDesc(Long applicantId);
}
