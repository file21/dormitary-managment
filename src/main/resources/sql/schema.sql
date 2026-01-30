-- PostgreSQL schema for Dorm Application Management

CREATE TYPE user_role AS ENUM ('OWNER','ADMIN','PROCTOR','STUDENT');
CREATE TYPE student_category AS ENUM ('NORMAL','STAFF_PRIVILEGED');
CREATE TYPE application_status AS ENUM ('DRAFT','SUBMITTED','NEEDS_EDIT','UNDER_REVIEW','ACCEPTED','REJECTED','CHECKED_IN','WITHDREW');
CREATE TYPE sponsorship_type AS ENUM ('GOV','SELF');

CREATE TABLE IF NOT EXISTS app_user (
  id              BIGSERIAL PRIMARY KEY,
  username        VARCHAR(50) UNIQUE NOT NULL,
  password_hash   TEXT NOT NULL,
  role            user_role NOT NULL,
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS student_profile (
  user_id         BIGINT PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
  full_name       VARCHAR(120) NOT NULL,
  aau_id          VARCHAR(30) UNIQUE,
  department      VARCHAR(120),
  year_of_study   INT CHECK (year_of_study BETWEEN 1 AND 7),
  category        student_category NOT NULL DEFAULT 'NORMAL'
);

CREATE TABLE IF NOT EXISTS campus (
  id              BIGSERIAL PRIMARY KEY,
  name            VARCHAR(80) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS block (
  id              BIGSERIAL PRIMARY KEY,
  campus_id       BIGINT NOT NULL REFERENCES campus(id) ON DELETE CASCADE,
  block_code      VARCHAR(20) NOT NULL,
  UNIQUE (campus_id, block_code)
);

CREATE TABLE IF NOT EXISTS bed (
  id              BIGSERIAL PRIMARY KEY,
  block_id        BIGINT NOT NULL REFERENCES block(id) ON DELETE CASCADE,
  bed_label       VARCHAR(20) NOT NULL,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (block_id, bed_label)
);

CREATE TABLE IF NOT EXISTS proctor_assignment (
  proctor_user_id BIGINT PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
  block_id        BIGINT NOT NULL REFERENCES block(id),
  active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS application_window (
  window_code     VARCHAR(40) PRIMARY KEY,
  open_at         TIMESTAMP NOT NULL,
  close_at        TIMESTAMP NOT NULL,
  active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS dorm_application (
  id              BIGSERIAL PRIMARY KEY,
  student_user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  window_code     VARCHAR(40) NOT NULL REFERENCES application_window(window_code),
  status          application_status NOT NULL DEFAULT 'DRAFT',
  sponsorship     sponsorship_type NOT NULL,
  disability      BOOLEAN NOT NULL DEFAULT FALSE,
  department      VARCHAR(120),
  campus_pref     VARCHAR(80),
  distance_km     NUMERIC(6,2),
  notes           TEXT,
  score           INT NOT NULL DEFAULT 0,
  submitted_at    TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (student_user_id, window_code)
);

CREATE TABLE IF NOT EXISTS application_review (
  id              BIGSERIAL PRIMARY KEY,
  application_id  BIGINT NOT NULL REFERENCES dorm_application(id) ON DELETE CASCADE,
  reviewer_user_id BIGINT NOT NULL REFERENCES app_user(id),
  decision        application_status NOT NULL,
  comment         TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS allocation (
  id              BIGSERIAL PRIMARY KEY,
  application_id  BIGINT UNIQUE NOT NULL REFERENCES dorm_application(id) ON DELETE CASCADE,
  bed_id          BIGINT UNIQUE NOT NULL REFERENCES bed(id),
  assigned_by     BIGINT NOT NULL REFERENCES app_user(id),
  room_number     VARCHAR(30),
  allocated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  checked_in_at   TIMESTAMP,
  checked_out_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification (
  id              BIGSERIAL PRIMARY KEY,
  to_user_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  title           VARCHAR(120) NOT NULL,
  message         TEXT NOT NULL,
  is_read         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_log (
  id              BIGSERIAL PRIMARY KEY,
  actor_user_id   BIGINT REFERENCES app_user(id),
  action          VARCHAR(80) NOT NULL,
  entity_type     VARCHAR(80) NOT NULL,
  entity_id       BIGINT,
  details         TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
