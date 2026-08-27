package com.studygather.application.entity;

import com.studygather.study.entity.Study;
import com.studygather.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "study_applications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_study_applications_study_applicant",
                columnNames = {"study_id", "applicant_id"}
        )
)
public class StudyApplication {

    public static final int MAX_MESSAGE_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(length = MAX_MESSAGE_LENGTH)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected StudyApplication() {
    }

    private StudyApplication(Study study, User applicant, String message) {
        this.study = study;
        this.applicant = applicant;
        this.message = message;
        this.status = ApplicationStatus.PENDING;
    }

    public static StudyApplication create(Study study, User applicant, String message) {
        Objects.requireNonNull(study, "스터디는 필수입니다.");
        Objects.requireNonNull(applicant, "신청자는 필수입니다.");

        if (message != null) {
            if (message.isBlank()) {
                throw new IllegalArgumentException("신청 메시지는 공백일 수 없습니다.");
            }
            if (message.length() > MAX_MESSAGE_LENGTH) {
                throw new IllegalArgumentException(
                        "신청 메시지는 " + MAX_MESSAGE_LENGTH + "자를 초과할 수 없습니다."
                );
            }
        }

        return new StudyApplication(study, applicant, message);
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Study getStudy() {
        return study;
    }

    public User getApplicant() {
        return applicant;
    }

    public String getMessage() {
        return message;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
