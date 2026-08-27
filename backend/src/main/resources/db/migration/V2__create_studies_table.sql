CREATE TABLE studies
(
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    owner_id             BIGINT        NOT NULL,
    title                VARCHAR(100)  NOT NULL,
    description          VARCHAR(2000) NOT NULL,
    capacity             INT           NOT NULL,
    approved_count       INT           NOT NULL DEFAULT 1,
    recruitment_deadline DATETIME(6)   NOT NULL,
    status               VARCHAR(20)   NOT NULL,
    created_at           DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_studies PRIMARY KEY (id),
    CONSTRAINT fk_studies_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT chk_studies_capacity CHECK (capacity >= 2),
    CONSTRAINT chk_studies_approved_count CHECK (approved_count >= 1 AND approved_count <= capacity),
    CONSTRAINT chk_studies_status CHECK (status IN ('OPEN', 'CLOSED')),

    INDEX idx_studies_owner_id (owner_id),
    INDEX idx_studies_status_deadline (status, recruitment_deadline)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
