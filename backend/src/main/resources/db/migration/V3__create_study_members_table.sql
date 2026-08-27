CREATE TABLE study_members
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    study_id    BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    member_role VARCHAR(20) NOT NULL,
    joined_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_study_members PRIMARY KEY (id),
    CONSTRAINT fk_study_members_study FOREIGN KEY (study_id) REFERENCES studies (id),
    CONSTRAINT fk_study_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_study_members_study_user UNIQUE (study_id, user_id),
    CONSTRAINT chk_study_members_role CHECK (member_role IN ('OWNER', 'MEMBER')),

    INDEX idx_study_members_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
