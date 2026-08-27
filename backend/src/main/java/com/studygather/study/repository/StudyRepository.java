package com.studygather.study.repository;

import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select study from Study study where study.id = :studyId")
    Optional<Study> findByIdForUpdate(@Param("studyId") Long studyId);

    List<Study> findAllByStatusAndRecruitmentDeadlineAfterOrderByRecruitmentDeadlineAsc(
            StudyStatus status,
            LocalDateTime now
    );
}
