CREATE TABLE study_applications
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    study_id     BIGINT       NOT NULL,
    applicant_id BIGINT       NOT NULL,
    message      VARCHAR(500) NULL,
    status       VARCHAR(20)  NOT NULL,
    decided_at   DATETIME(6)  NULL,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_study_applications PRIMARY KEY (id),
    CONSTRAINT fk_study_applications_study FOREIGN KEY (study_id) REFERENCES studies (id),
    CONSTRAINT fk_study_applications_applicant FOREIGN KEY (applicant_id) REFERENCES users (id),
    CONSTRAINT uk_study_applications_study_applicant UNIQUE (study_id, applicant_id),
    CONSTRAINT chk_study_applications_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELED')),

    INDEX idx_study_applications_applicant_id (applicant_id),
    INDEX idx_study_applications_study_status (study_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
