-- IF NOT EXISTS permits explicit adoption of the original Hibernate schema.
-- Hibernate validate checks entity compatibility after migrations.
CREATE TABLE IF NOT EXISTS departments (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS patients (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, full_name VARCHAR(100) NOT NULL,
 email VARCHAR(120) NOT NULL UNIQUE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS providers (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, full_name VARCHAR(100) NOT NULL,
 department_id BIGINT NOT NULL, FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS time_slots (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, provider_id BIGINT NOT NULL,
 appointment_date DATE NOT NULL, start_time TIME(6) NOT NULL, end_time TIME(6) NOT NULL,
 status ENUM('AVAILABLE','BOOKED') NOT NULL,
 FOREIGN KEY (provider_id) REFERENCES providers(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS appointments (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, patient_id BIGINT NOT NULL,
 time_slot_id BIGINT NOT NULL, description VARCHAR(500) NOT NULL,
 status VARCHAR(30) NOT NULL, created_at DATETIME(6) NOT NULL,
 CONSTRAINT uk_appointment_time_slot UNIQUE (time_slot_id),
 FOREIGN KEY (patient_id) REFERENCES patients(id),
 FOREIGN KEY (time_slot_id) REFERENCES time_slots(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS app_users (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(80) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL, role ENUM('ADMIN','PATIENT','PROVIDER') NOT NULL,
 patient_id BIGINT UNIQUE, provider_id BIGINT UNIQUE,
 FOREIGN KEY (patient_id) REFERENCES patients(id), FOREIGN KEY (provider_id) REFERENCES providers(id)
) ENGINE=InnoDB;
