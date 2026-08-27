package com.studygather.study.repository;

import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Long> {

    List<Study> findAllByStatusAndRecruitmentDeadlineAfterOrderByRecruitmentDeadlineAsc(
            StudyStatus status,
            LocalDateTime now
    );
}
