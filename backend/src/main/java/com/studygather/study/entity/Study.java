package com.studygather.study.entity;

import com.studygather.study.exception.StudyCapacityExceededException;
import com.studygather.study.exception.StudyClosedException;
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

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "studies")
public class Study {

    public static final int MIN_CAPACITY = 2;
    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(nullable = false, length = MAX_DESCRIPTION_LENGTH)
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "approved_count", nullable = false)
    private int approvedCount;

    @Column(name = "recruitment_deadline", nullable = false)
    private LocalDateTime recruitmentDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Study() {
    }

    private Study(
            User owner,
            String title,
            String description,
            int capacity,
            LocalDateTime recruitmentDeadline
    ) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.approvedCount = 1;
        this.recruitmentDeadline = recruitmentDeadline;
        this.status = StudyStatus.OPEN;
    }

    public static Study create(
            User owner,
            String title,
            String description,
            int capacity,
            LocalDateTime recruitmentDeadline
    ) {
        validateOwner(owner);
        validateText(title, MAX_TITLE_LENGTH, "제목");
        validateText(description, MAX_DESCRIPTION_LENGTH, "설명");
        validateCapacity(capacity);
        Objects.requireNonNull(recruitmentDeadline, "모집 마감일은 필수입니다.");

        // 정원은 개설자를 포함하므로 생성 시 승인 인원을 1명으로 시작한다.
        return new Study(owner, title, description, capacity, recruitmentDeadline);
    }

    private static void validateOwner(User owner) {
        Objects.requireNonNull(owner, "개설자는 필수입니다.");
    }

    private static void validateText(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + "은(는) " + maxLength + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateCapacity(int capacity) {
        if (capacity < MIN_CAPACITY) {
            throw new IllegalArgumentException("정원은 " + MIN_CAPACITY + "명 이상이어야 합니다.");
        }
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

    public User getOwner() {
        return owner;
    }

    public boolean isOwnedBy(Long userId) {
        return userId != null && Objects.equals(owner.getId(), userId);
    }

    public boolean isRecruitingAt(LocalDateTime now) {
        return status == StudyStatus.OPEN && recruitmentDeadline.isAfter(now);
    }

    public void update(
            String title,
            String description,
            Integer capacity,
            LocalDateTime recruitmentDeadline
    ) {
        if (title != null) {
            validateText(title, MAX_TITLE_LENGTH, "제목");
            this.title = title;
        }
        if (description != null) {
            validateText(description, MAX_DESCRIPTION_LENGTH, "설명");
            this.description = description;
        }
        if (capacity != null) {
            validateCapacity(capacity);
            if (capacity < approvedCount) {
                throw new StudyCapacityExceededException(
                        "현재 승인 인원보다 정원을 작게 설정할 수 없습니다."
                );
            }
            this.capacity = capacity;
        }
        if (recruitmentDeadline != null) {
            this.recruitmentDeadline = recruitmentDeadline;
        }
    }

    public void close() {
        if (status == StudyStatus.CLOSED) {
            throw new StudyClosedException();
        }

        status = StudyStatus.CLOSED;
    }

    public void increaseApprovedCount() {
        validateCapacityAvailable();
        approvedCount++;
    }

    public void validateCapacityAvailable() {
        if (approvedCount >= capacity) {
            throw new StudyCapacityExceededException();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getApprovedCount() {
        return approvedCount;
    }

    public LocalDateTime getRecruitmentDeadline() {
        return recruitmentDeadline;
    }

    public StudyStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
