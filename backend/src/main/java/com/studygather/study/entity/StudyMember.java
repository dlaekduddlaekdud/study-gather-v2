package com.studygather.study.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "study_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_study_members_study_user",
                columnNames = {"study_id", "user_id"}
        )
)
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 20)
    private StudyMemberRole memberRole;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    protected StudyMember() {
    }

    private StudyMember(Study study, User user, StudyMemberRole memberRole) {
        this.study = study;
        this.user = user;
        this.memberRole = memberRole;
    }

    public static StudyMember createOwner(Study study, User owner) {
        Objects.requireNonNull(study, "스터디는 필수입니다.");
        Objects.requireNonNull(owner, "개설자는 필수입니다.");

        if (!study.isOwnedBy(owner.getId())) {
            throw new IllegalArgumentException("스터디 개설자만 OWNER 멤버가 될 수 있습니다.");
        }

        return new StudyMember(study, owner, StudyMemberRole.OWNER);
    }

    public static StudyMember createMember(Study study, User user) {
        Objects.requireNonNull(study, "스터디는 필수입니다.");
        Objects.requireNonNull(user, "사용자는 필수입니다.");

        return new StudyMember(study, user, StudyMemberRole.MEMBER);
    }

    @PrePersist
    private void prePersist() {
        joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Study getStudy() {
        return study;
    }

    public User getUser() {
        return user;
    }

    public StudyMemberRole getMemberRole() {
        return memberRole;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
