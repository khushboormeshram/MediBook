-- Development-only accounts. Password for all accounts: password
-- Generated with BCrypt; replace these accounts before deployment.
INSERT INTO app_user(id,email,password_hash,full_name,role) VALUES
 (1,'admin@medibook.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','MediBook Admin','ADMIN'),
 (2,'patient@medibook.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Aarav Sharma','PATIENT'),
 (3,'doctor@medibook.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Dr. Meera Iyer','DOCTOR'),
 (4,'doctor2@medibook.local','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Dr. Kabir Mehta','DOCTOR');
INSERT INTO patient_profile(user_id,phone,location) VALUES(2,'+91 98765 43210','Bengaluru');
INSERT INTO specialty(id,name) VALUES(1,'Cardiology'),(2,'Dermatology'),(3,'Pediatrics'),(4,'General Practice');
INSERT INTO doctor_profile(user_id,specialty_id,qualifications,experience_years,consultation_fee,location,bio,verified,manual_confirmation) VALUES
 (3,1,'MBBS, MD Cardiology',12,900,'Bengaluru','Dr. Meera Iyer combines 12 years of heart care with a calm, patient-first approach. Special interests include preventive cardiology and blood pressure management.',true,false),
 (4,2,'MBBS, MD Dermatology',9,750,'Bengaluru','Dr. Kabir Mehta focuses on practical, evidence-led care for skin and hair concerns, with clear follow-up plans for every patient.',true,true);
INSERT INTO doctor_availability(doctor_id,day_of_week,start_time,end_time,slot_minutes,break_start,break_end) VALUES
 (3,'MONDAY','09:00','13:00',30,'11:00','11:30'),(3,'WEDNESDAY','09:00','13:00',30,'11:00','11:30'),(3,'FRIDAY','14:00','18:00',30,'16:00','16:30'),
 (4,'TUESDAY','10:00','14:00',30,'12:00','12:30'),(4,'THURSDAY','10:00','14:00',30,'12:00','12:30'),(4,'SATURDAY','09:00','13:00',30,'11:00','11:30');
SELECT setval(pg_get_serial_sequence('app_user','id'),4,true);
SELECT setval(pg_get_serial_sequence('specialty','id'),4,true);
