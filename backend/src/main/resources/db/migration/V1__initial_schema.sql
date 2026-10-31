CREATE TABLE app_user (
 id BIGSERIAL PRIMARY KEY, email VARCHAR(254) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL,
 full_name VARCHAR(80) NOT NULL, role VARCHAR(16) NOT NULL CHECK (role IN ('PATIENT','DOCTOR','ADMIN')),
 active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_user_email ON app_user(email);
CREATE TABLE specialty (id BIGSERIAL PRIMARY KEY, name VARCHAR(80) NOT NULL UNIQUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE patient_profile (user_id BIGINT PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE, phone VARCHAR(30), location VARCHAR(120), created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE doctor_profile (
 user_id BIGINT PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE, specialty_id BIGINT REFERENCES specialty(id),
 qualifications VARCHAR(120), experience_years INTEGER CHECK (experience_years IS NULL OR experience_years>=0),
 consultation_fee NUMERIC(10,2) CHECK (consultation_fee IS NULL OR consultation_fee>=0), location VARCHAR(100), bio VARCHAR(1000),
 verified BOOLEAN NOT NULL DEFAULT FALSE, manual_confirmation BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE doctor_availability (
 id BIGSERIAL PRIMARY KEY, doctor_id BIGINT NOT NULL REFERENCES doctor_profile(user_id) ON DELETE CASCADE,
 day_of_week VARCHAR(16) NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL, slot_minutes INTEGER NOT NULL CHECK (slot_minutes BETWEEN 10 AND 120),
 break_start TIME, break_end TIME, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), CHECK(start_time<end_time), CHECK((break_start IS NULL AND break_end IS NULL) OR (break_start<break_end AND break_start>=start_time AND break_end<=end_time))
);
CREATE INDEX idx_availability_doctor_day ON doctor_availability(doctor_id,day_of_week);
CREATE TABLE appointment (
 id BIGSERIAL PRIMARY KEY, patient_id BIGINT NOT NULL REFERENCES app_user(id), doctor_id BIGINT NOT NULL REFERENCES doctor_profile(user_id),
 start_at TIMESTAMPTZ NOT NULL, end_at TIMESTAMPTZ NOT NULL, status VARCHAR(16) NOT NULL CHECK(status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED','REJECTED')),
 reason VARCHAR(500), created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), version BIGINT NOT NULL DEFAULT 0,
 CHECK(start_at<end_at)
);
CREATE INDEX idx_appointment_doctor_start ON appointment(doctor_id,start_at);
CREATE INDEX idx_appointment_patient_start ON appointment(patient_id,start_at);
CREATE TABLE notification (
 id BIGSERIAL PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE, title VARCHAR(160) NOT NULL,
 message VARCHAR(500) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), is_read BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE audit_log (
 id BIGSERIAL PRIMARY KEY, actor_id BIGINT REFERENCES app_user(id), action VARCHAR(80) NOT NULL, details VARCHAR(500), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_actor_created ON audit_log(actor_id,created_at);
