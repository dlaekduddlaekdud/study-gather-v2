package com.studygather.application.repository;

import com.studygather.application.entity.StudyApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select application from StudyApplication application where application.id = :applicationId")
    Optional<StudyApplication> findByIdForUpdate(@Param("applicationId") Long applicationId);

    boolean existsByStudyIdAndApplicantId(Long studyId, Long applicantId);

    @EntityGraph(attributePaths = {"study", "applicant"})
    List<StudyApplication> findAllByStudyIdOrderByCreatedAtAsc(Long studyId);

    @EntityGraph(attributePaths = {"study", "applicant"})
    List<StudyApplication> findAllByApplicantIdOrderByCreatedAtDesc(Long applicantId);
}
