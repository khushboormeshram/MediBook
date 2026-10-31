-- The original documented development hash does not match the documented password.
-- Repair only untouched seed hashes; leave any password changed by an existing user alone.
UPDATE app_user
SET password_hash = '$2a$12$.bWdeFG47b3E8jI2ZxEQ6OeiQW0EJCASnhkuV/rfjORR3IMXE4HAi'
WHERE email IN ('admin@medibook.local','patient@medibook.local','doctor@medibook.local','doctor2@medibook.local')
  AND password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
