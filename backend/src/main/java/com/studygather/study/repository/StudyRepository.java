package com.studygather.study.repository;

import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {

    // 신청 승인 시 정원 검사와 인원 변경이 끝날 때까지 행 잠금을 유지한다.
    @Query(value = "SELECT * FROM studies WHERE id = :studyId FOR UPDATE", nativeQuery = true)
    Optional<Study> findByIdForUpdate(@Param("studyId") Long studyId);

    List<Study> findAllByStatusAndRecruitmentDeadlineAfterOrderByRecruitmentDeadlineAsc(
            StudyStatus status,
            LocalDateTime now
    );
}
