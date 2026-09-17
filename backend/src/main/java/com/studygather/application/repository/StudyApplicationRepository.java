package com.studygather.application.repository;

import com.studygather.application.entity.StudyApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    // 배포 DB에서도 별칭 없는 잠금 SQL을 사용하도록 직접 지정한다.
    @Query(value = "SELECT * FROM study_applications WHERE id = :applicationId FOR UPDATE", nativeQuery = true)
    Optional<StudyApplication> findByIdForUpdate(@Param("applicationId") Long applicationId);

    boolean existsByStudyIdAndApplicantId(Long studyId, Long applicantId);

    @EntityGraph(attributePaths = {"study", "applicant"})
    List<StudyApplication> findAllByStudyIdOrderByCreatedAtAsc(Long studyId);

    @EntityGraph(attributePaths = {"study", "applicant"})
    List<StudyApplication> findAllByApplicantIdOrderByCreatedAtDesc(Long applicantId);
}
