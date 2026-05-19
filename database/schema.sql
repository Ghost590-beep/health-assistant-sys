-- =============================================
-- HEALTH ASSISTANCE SYSTEM
-- Normalized: 1NF ✓ 2NF ✓ 3NF ✓ BCNF ✓
-- =============================================

DROP DATABASE IF EXISTS health_assistance_system;
CREATE DATABASE health_assistance_system;
USE health_assistance_system;

-- =============================================
-- LOOKUP TABLES
-- =============================================

CREATE TABLE gender (
                        gender_code VARCHAR(1) PRIMARY KEY,
                        gender_name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE blood_group (
                             blood_code VARCHAR(3) PRIMARY KEY,
                             blood_name VARCHAR(5) NOT NULL UNIQUE
);

CREATE TABLE appointment_status (
                                    status_code VARCHAR(10) PRIMARY KEY,
                                    status_name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE contact_type (
                              type_code VARCHAR(10) PRIMARY KEY,
                              type_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE roles (
                       role_id INT AUTO_INCREMENT PRIMARY KEY,
                       role_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE specializations (
                                 specialization_id INT AUTO_INCREMENT PRIMARY KEY,
                                 specialization_name VARCHAR(100) UNIQUE NOT NULL
);

-- =============================================
-- CORE ENTITY: USERS
-- =============================================

CREATE TABLE users (
                       user_id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       role_id INT NOT NULL,
                       gender_code VARCHAR(1) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT fk_users_role FOREIGN KEY (role_id)
                           REFERENCES roles(role_id) ON DELETE RESTRICT,
                       CONSTRAINT fk_users_gender FOREIGN KEY (gender_code)
                           REFERENCES gender(gender_code) ON DELETE RESTRICT
);

-- =============================================
-- PATIENTS
-- =============================================

CREATE TABLE patients (
                          patient_id INT PRIMARY KEY AUTO_INCREMENT,
                          user_id INT UNIQUE NOT NULL,
                          date_of_birth DATE NOT NULL,
                          blood_code VARCHAR(3) NOT NULL,
                          address TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT fk_patients_user FOREIGN KEY (user_id)
                              REFERENCES users(user_id) ON DELETE CASCADE,
                          CONSTRAINT fk_patients_blood FOREIGN KEY (blood_code)
                              REFERENCES blood_group(blood_code) ON DELETE RESTRICT
);

-- =============================================
-- DOCTORS
-- =============================================

CREATE TABLE doctors (
                         doctor_id INT PRIMARY KEY AUTO_INCREMENT,
                         user_id INT UNIQUE NOT NULL,
                         license_number VARCHAR(50) UNIQUE NOT NULL,
                         years_of_experience INT DEFAULT 0 CHECK (years_of_experience >= 0),
                         clinic_address TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT fk_doctors_user FOREIGN KEY (user_id)
                             REFERENCES users(user_id) ON DELETE CASCADE
);

-- DOCTOR SPECIALIZATIONS (M:N)
CREATE TABLE doctor_specializations (
                                        doctor_id INT NOT NULL,
                                        specialization_id INT NOT NULL,
                                        PRIMARY KEY (doctor_id, specialization_id),
                                        FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
                                        FOREIGN KEY (specialization_id) REFERENCES specializations(specialization_id) ON DELETE CASCADE
);

-- =============================================
-- ADMINS
-- =============================================

CREATE TABLE admins (
                        admin_id INT PRIMARY KEY AUTO_INCREMENT,
                        user_id INT UNIQUE NOT NULL,
                        department VARCHAR(100) NOT NULL,
                        access_level INT DEFAULT 1 CHECK (access_level BETWEEN 1 AND 5),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT fk_admins_user FOREIGN KEY (user_id)
                            REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================
-- CONTACTS (Unified with strict FK)
-- =============================================

CREATE TABLE contacts (
                          contact_id INT PRIMARY KEY AUTO_INCREMENT,
                          user_id INT NOT NULL,
                          contact_type VARCHAR(10) NOT NULL,
                          contact_value VARCHAR(100) NOT NULL,
                          is_primary BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_contact_user FOREIGN KEY (user_id)
                              REFERENCES users(user_id) ON DELETE CASCADE,
                          CONSTRAINT fk_contact_type FOREIGN KEY (contact_type)
                              REFERENCES contact_type(type_code) ON DELETE RESTRICT,
                          CONSTRAINT uk_contact UNIQUE (user_id, contact_type, contact_value)
);

-- =============================================
-- VITALS
-- =============================================

CREATE TABLE vitals (
                        vital_id INT PRIMARY KEY AUTO_INCREMENT,
                        patient_id INT NOT NULL,
                        recorded_by INT NOT NULL,
                        temperature_celsius DECIMAL(4,1) CHECK (temperature_celsius BETWEEN 30 AND 45),
                        weight_kg DECIMAL(5,2),
                        height_cm DECIMAL(5,2),
                        blood_pressure_systolic INT CHECK (blood_pressure_systolic BETWEEN 60 AND 250),
                        blood_pressure_diastolic INT CHECK (blood_pressure_diastolic BETWEEN 40 AND 150),
                        heart_rate_bpm INT CHECK (heart_rate_bpm BETWEEN 30 AND 250),
                        respiratory_rate INT,
                        oxygen_saturation INT CHECK (oxygen_saturation BETWEEN 50 AND 100),
                        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_vitals_patient FOREIGN KEY (patient_id)
                            REFERENCES patients(patient_id) ON DELETE CASCADE,
                        CONSTRAINT fk_vitals_recorder FOREIGN KEY (recorded_by)
                            REFERENCES users(user_id) ON DELETE RESTRICT
);

-- =============================================
-- APPOINTMENTS
-- =============================================

CREATE TABLE appointments (
                              appointment_id INT PRIMARY KEY AUTO_INCREMENT,
                              patient_id INT NOT NULL,
                              doctor_id INT NOT NULL,
                              appointment_date DATE NOT NULL,
                              appointment_time TIME NOT NULL,
                              status_code VARCHAR(10) NOT NULL DEFAULT 'PENDING',
                              reason TEXT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uk_appointment_slot UNIQUE (doctor_id, appointment_date, appointment_time),
                              CONSTRAINT uk_patient_slot UNIQUE (patient_id, appointment_date, appointment_time),
                              CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id)
                                  REFERENCES patients(patient_id) ON DELETE CASCADE,
                              CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id)
                                  REFERENCES doctors(doctor_id) ON DELETE CASCADE,
                              CONSTRAINT fk_appointment_status FOREIGN KEY (status_code)
                                  REFERENCES appointment_status(status_code) ON DELETE RESTRICT
);

-- =============================================
-- HEALTH RECORDS
-- =============================================

CREATE TABLE health_records (
                                record_id INT PRIMARY KEY AUTO_INCREMENT,
                                patient_id INT NOT NULL,
                                doctor_id INT NOT NULL,
                                diagnosis TEXT NOT NULL,
                                prescription TEXT,
                                notes TEXT,
                                record_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_record_patient FOREIGN KEY (patient_id)
                                    REFERENCES patients(patient_id) ON DELETE CASCADE,
                                CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id)
                                    REFERENCES doctors(doctor_id) ON DELETE RESTRICT
);

-- =============================================
-- NOTIFICATIONS
-- =============================================

CREATE TABLE notifications (
                               notification_id INT PRIMARY KEY AUTO_INCREMENT,
                               user_id INT NOT NULL,
                               title VARCHAR(100) NOT NULL,
                               message TEXT NOT NULL,
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_notification_user FOREIGN KEY (user_id)
                                   REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================
-- SEED DATA
-- =============================================

INSERT INTO gender (gender_code, gender_name) VALUES
                                                  ('M', 'Male'),
                                                  ('F', 'Female'),
                                                  ('O', 'Other');

INSERT INTO blood_group (blood_code, blood_name) VALUES
                                                     ('A+', 'A+'), ('A-', 'A-'),
                                                     ('B+', 'B+'), ('B-', 'B-'),
                                                     ('AB+', 'AB+'), ('AB-', 'AB-'),
                                                     ('O+', 'O+'), ('O-', 'O-');

INSERT INTO appointment_status (status_code, status_name) VALUES
                                                              ('PENDING', 'Pending'),
                                                              ('CONFIRMED', 'Confirmed'),
                                                              ('CANCELLED', 'Cancelled'),
                                                              ('COMPLETED', 'Completed');

INSERT INTO contact_type (type_code, type_name) VALUES
                                                    ('PHONE', 'Phone'),
                                                    ('EMAIL', 'Email'),
                                                    ('EMERGENCY', 'Emergency Contact'),
                                                    ('CLINIC', 'Clinic Contact');

INSERT INTO roles (role_id, role_name) VALUES
                                           (1, 'PATIENT'),
                                           (2, 'DOCTOR'),
                                           (3, 'ADMIN');