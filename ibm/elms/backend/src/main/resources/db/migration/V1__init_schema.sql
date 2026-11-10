-- ============================================================
-- ELMS Database Schema V1
-- ============================================================

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    role        VARCHAR(50)  NOT NULL,         -- EMPLOYEE, MANAGER, HR_ADMIN, SYSTEM_ADMIN
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    manager_id  BIGINT REFERENCES users(id),
    department  VARCHAR(100),
    job_title   VARCHAR(100),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_manager  ON users(manager_id);

-- ---------------------------------------------------------------

CREATE TABLE leave_types (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------

CREATE TABLE leave_policies (
    id                       BIGSERIAL PRIMARY KEY,
    leave_type_id            BIGINT    NOT NULL REFERENCES leave_types(id),
    annual_entitlement_days  INT       NOT NULL,
    carry_over_days          INT       NOT NULL DEFAULT 0,
    max_consecutive_days     INT,
    min_advance_notice_days  INT       NOT NULL DEFAULT 0,
    allow_half_day           BOOLEAN   NOT NULL DEFAULT FALSE,
    exclude_weekends         BOOLEAN   NOT NULL DEFAULT TRUE,
    exclude_public_holidays  BOOLEAN   NOT NULL DEFAULT TRUE,
    allow_cancellation       BOOLEAN   NOT NULL DEFAULT TRUE,
    active                   BOOLEAN   NOT NULL DEFAULT TRUE,
    effective_from           DATE      NOT NULL,
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------

CREATE TABLE leave_balances (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT    NOT NULL REFERENCES users(id),
    leave_type_id   BIGINT    NOT NULL REFERENCES leave_types(id),
    year            INT       NOT NULL,
    total_days      NUMERIC(6,2) NOT NULL DEFAULT 0,
    used_days       NUMERIC(6,2) NOT NULL DEFAULT 0,
    pending_days    NUMERIC(6,2) NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (employee_id, leave_type_id, year)
);

CREATE INDEX idx_lb_employee ON leave_balances(employee_id);

-- ---------------------------------------------------------------

CREATE TABLE public_holidays (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    holiday_date DATE        NOT NULL UNIQUE
);

-- ---------------------------------------------------------------

CREATE TABLE leave_requests (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT    NOT NULL REFERENCES users(id),
    leave_type_id   BIGINT    NOT NULL REFERENCES leave_types(id),
    start_date      DATE      NOT NULL,
    end_date        DATE      NOT NULL,
    working_days    NUMERIC(6,2) NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',  -- PENDING, APPROVED, REJECTED, CANCELLED
    reason          TEXT,
    manager_comment TEXT,
    reviewed_by_id  BIGINT    REFERENCES users(id),
    reviewed_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lr_employee ON leave_requests(employee_id);
CREATE INDEX idx_lr_status   ON leave_requests(status);
CREATE INDEX idx_lr_dates    ON leave_requests(start_date, end_date);

-- ---------------------------------------------------------------

CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    actor_id    BIGINT    REFERENCES users(id),
    action      VARCHAR(200) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   BIGINT,
    details     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_actor  ON audit_logs(actor_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);

-- ---------------------------------------------------------------

CREATE TABLE balance_adjustments (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT    NOT NULL REFERENCES users(id),
    leave_type_id BIGINT    NOT NULL REFERENCES leave_types(id),
    year          INT       NOT NULL,
    adjusted_by   BIGINT    NOT NULL REFERENCES users(id),
    delta_days    NUMERIC(6,2) NOT NULL,
    reason        TEXT      NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
