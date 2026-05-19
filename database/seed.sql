USE health_assistance_system;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE doctor_specializations;
TRUNCATE TABLE appointments;
TRUNCATE TABLE vitals;
TRUNCATE TABLE health_records;
TRUNCATE TABLE notifications;
TRUNCATE TABLE contacts;
TRUNCATE TABLE admins;
TRUNCATE TABLE doctors;
TRUNCATE TABLE patients;
TRUNCATE TABLE users;
TRUNCATE TABLE specializations;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO specializations (specialization_name) VALUES
                                                      ('Cardiology'), ('Dermatology'), ('Neurology'), ('Pediatrics'), ('Orthopedics'),
                                                      ('General Medicine'), ('Radiology'), ('Psychiatry');

-- ADMIN: Michael Abebe | Password: Admin@2024!Secure
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code)
VALUES ('michael.abebe', 'michael.abebe@healthassist.com',
        'fb15f3aad8237049f3d98d2c839f9c0c57564110b2d8fed083964f8a172abb13',
        'Michael', 'Abebe', 3, 'M');
SET @admin_user_id = LAST_INSERT_ID();
INSERT INTO admins (user_id, department, access_level) VALUES (@admin_user_id, 'System Administration', 5);
INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES
                                                                            (@admin_user_id, 'EMAIL', 'michael.abebe@healthassist.com', TRUE),
                                                                            (@admin_user_id, 'PHONE', '+251-911-234567', FALSE);

-- DOCTOR 1: Dr. Tesfaye Kebede | Password: Dr.Tesfaye#2024
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code)
VALUES ('dr.tesfaye.kebede', 'dr.tesfaye.kebede@healthassist.com',
        'dea0f130042f6a8635517f8743d1e97432e1db09f1ee15f334f19afd31bde85b',
        'Tesfaye', 'Kebede', 2, 'M');
SET @doc1_user_id = LAST_INSERT_ID();
INSERT INTO doctors (user_id, license_number, years_of_experience, clinic_address)
VALUES (@doc1_user_id, 'ETH-MC-2010-00421', 14, 'Bole Medhanialem, Addis Ababa');
SET @doc1_id = LAST_INSERT_ID();
INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES
                                                                            (@doc1_user_id, 'PHONE', '+251-922-345678', TRUE),
                                                                            (@doc1_user_id, 'EMAIL', 'dr.tesfaye.kebede@healthassist.com', FALSE),
                                                                            (@doc1_user_id, 'CLINIC', '+251-116-182345', FALSE);

-- DOCTOR 2: Dr. Hanna Tadesse | Password: Dr.Hanna#2024
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code)
VALUES ('dr.hanna.tadesse', 'dr.hanna.tadesse@healthassist.com',
        '22db42909bf83485506a35c41aabdfd125e77b3fe9497eb1fec71b0472d94094',
        'Hanna', 'Tadesse', 2, 'F');
SET @doc2_user_id = LAST_INSERT_ID();
INSERT INTO doctors (user_id, license_number, years_of_experience, clinic_address)
VALUES (@doc2_user_id, 'ETH-MC-2013-00876', 11, 'Cazanchise, Addis Ababa');
SET @doc2_id = LAST_INSERT_ID();
INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES
                                                                            (@doc2_user_id, 'PHONE', '+251-933-456789', TRUE),
                                                                            (@doc2_user_id, 'EMAIL', 'dr.hanna.tadesse@healthassist.com', FALSE);

-- DOCTOR 3: Dr. Yonas Desta | Password: Dr.Yonas#2024
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code)
VALUES ('dr.yonas.desta', 'dr.yonas.desta@healthassist.com',
        '84d18a09a59a1bb63bcf6049fff999744008c0dba3650c7c04432c552f68da00',
        'Yonas', 'Desta', 2, 'M');
SET @doc3_user_id = LAST_INSERT_ID();
INSERT INTO doctors (user_id, license_number, years_of_experience, clinic_address)
VALUES (@doc3_user_id, 'ETH-MC-2016-01532', 8, 'Piassa, Addis Ababa');
SET @doc3_id = LAST_INSERT_ID();
INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES
    (@doc3_user_id, 'PHONE', '+251-944-567890', TRUE);

INSERT INTO doctor_specializations (doctor_id, specialization_id) VALUES
                                                                      (@doc1_id, 1), (@doc1_id, 6),
                                                                      (@doc2_id, 4), (@doc2_id, 6),
                                                                      (@doc3_id, 5);

-- PATIENT 1: Rahel Asmamaw | Password: Rahel@2024!
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code)
VALUES ('rahel.asmamaw', 'rahel.asmamaw@gmail.com',
        '7c4a66be3842c399d20efd22656a4fb546ce7d3cf983ffdff9a7f99eec1ff35a',
        'Rahel', 'Asmamaw', 1, 'F');
SET @pat1_user_id = LAST_INSERT_ID();
INSERT INTO patients (user_id, date_of_birth, blood_code, address)
VALUES (@pat1_user_id, '1994-08-21', 'O+', 'Summit Condominium, Addis Ababa');
SET @pat1_id = LAST_INSERT_ID();
INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES
                                                                            (@pat1_user_id, 'PHONE', '+251-955-123456', TRUE),
                                                                            (@pat1_user_id, 'EMAIL', 'rahel.asmamaw@gmail.com', FALSE),
                                                                            (@pat1_user_id, 'EMERGENCY', '+251-966-789012', FALSE);

-- PATIENT 2: Abel Teshome | Password: Abel@2024!
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code)
VALUES ('abel.teshome', 'abel.teshome@yahoo.com',
        '935dbd099aed86c56ad712e07a221c3e018cd4b41c6fdf8e0107e070ecb33d7b',
        'Abel', 'Teshome', 1, 'M');
SET @pat2_user_id = LAST_INSERT_ID();
INSERT INTO patients (user_id, date_of_birth, blood_code, address)
VALUES (@pat2_user_id, '1999-12-05', 'A+', 'Gerji, Addis Ababa');
SET @pat2_id = LAST_INSERT_ID();
INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES
                                                                            (@pat2_user_id, 'PHONE', '+251-977-234567', TRUE),
                                                                            (@pat2_user_id, 'EMERGENCY', '+251-988-345678', FALSE);

-- PATIENT 3: Mahlet Worku | Password: Mahlet@2024!
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code)
VALUES ('mahlet.worku', 'mahlet.worku@gmail.com',
        'e6ed60b6f3428cf3a3170b6483079c615950c81ccd2afae526556aad8099292e',
        'Mahlet', 'Worku', 1, 'F');
SET @pat3_user_id = LAST_INSERT_ID();
INSERT INTO patients (user_id, date_of_birth, blood_code, address)
VALUES (@pat3_user_id, '1991-03-14', 'B+', 'Megenagna, Addis Ababa');
SET @pat3_id = LAST_INSERT_ID();
INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES
                                                                            (@pat3_user_id, 'PHONE', '+251-999-456789', TRUE),
                                                                            (@pat3_user_id, 'EMAIL', 'mahlet.worku@gmail.com', FALSE);

-- PATIENT 4: Daniel Girma | Password: Daniel@2024!
INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code)
VALUES ('daniel.girma', 'daniel.girma@outlook.com',
        '935dbd099aed86c56ad712e07a221c3e018cd4b41c6fdf8e0107e070ecb33d7b',
        'Daniel', 'Girma', 1, 'M');
SET @pat4_user_id = LAST_INSERT_ID();
INSERT INTO patients (user_id, date_of_birth, blood_code, address)
VALUES (@pat4_user_id, '1987-07-30', 'AB+', 'Ayat, Addis Ababa');
SET @pat4_id = LAST_INSERT_ID();
INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES
                                                                            (@pat4_user_id, 'PHONE', '+251-910-112233', TRUE),
                                                                            (@pat4_user_id, 'EMERGENCY', '+251-920-445566', FALSE);

-- VITALS
INSERT INTO vitals (patient_id, recorded_by, temperature_celsius, weight_kg, height_cm,
                    blood_pressure_systolic, blood_pressure_diastolic, heart_rate_bpm, respiratory_rate, oxygen_saturation)
VALUES (@pat1_id, @doc1_user_id, 36.8, 64.5, 165.0, 118, 76, 72, 16, 98);

-- APPOINTMENTS
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status_code, reason)
VALUES
    (@pat1_id, @doc1_id, CURDATE() + INTERVAL 1 DAY, '09:00:00', 'CONFIRMED', 'Annual physical examination'),
    (@pat2_id, @doc1_id, CURDATE() + INTERVAL 3 DAY, '14:30:00', 'PENDING', 'Chest pain consultation'),
    (@pat3_id, @doc2_id, CURDATE() + INTERVAL 2 DAY, '11:00:00', 'CONFIRMED', 'Child vaccination follow-up'),
    (@pat4_id, @doc3_id, CURDATE() - INTERVAL 5 DAY, '10:00:00', 'COMPLETED', 'Knee pain assessment');