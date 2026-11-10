-- Fix seed user passwords to match Admin@123
-- This corrects the wrong hash that was inserted in V2.
UPDATE users
SET password = '$2b$10$6ODQ5dj7G4U9iXrOJQOoB.jLf71MTg1GNXIU8w6Koa3h7s5pSVnDe'
WHERE email IN ('admin@elms.com', 'hr@elms.com');
