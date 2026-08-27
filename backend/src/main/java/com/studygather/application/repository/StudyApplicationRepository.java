package com.studygather.application.repository;

import com.studygather.application.entity.StudyApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    boolean existsByStudyIdAndApplicantId(Long studyId, Long applicantId);
}
