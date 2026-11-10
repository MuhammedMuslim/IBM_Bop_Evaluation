-- Seed initial leave types
INSERT INTO leave_types (name, description) VALUES
  ('Annual Leave',   'Standard paid annual leave'),
  ('Sick Leave',     'Leave due to illness or medical appointments'),
  ('Maternity Leave','Leave for new mothers'),
  ('Paternity Leave','Leave for new fathers'),
  ('Emergency Leave','Leave for unforeseen personal emergencies'),
  ('Unpaid Leave',   'Leave without pay');

-- Seed leave policies
INSERT INTO leave_policies (leave_type_id, annual_entitlement_days, carry_over_days,
    min_advance_notice_days, allow_half_day, exclude_weekends, exclude_public_holidays,
    allow_cancellation, effective_from)
VALUES
  (1, 21, 5, 2, TRUE,  TRUE, TRUE, TRUE, '2024-01-01'),
  (2, 10, 0, 0, TRUE,  TRUE, TRUE, TRUE, '2024-01-01'),
  (3, 90, 0, 14, FALSE, TRUE, TRUE, FALSE,'2024-01-01'),
  (4, 14, 0, 14, FALSE, TRUE, TRUE, FALSE,'2024-01-01'),
  (5, 5,  0, 0,  FALSE, TRUE, TRUE, TRUE, '2024-01-01'),
  (6, 30, 0, 5,  FALSE, TRUE, FALSE, TRUE,'2024-01-01');

-- Seed default admin user  (password = Admin@123)
INSERT INTO users (email, password, first_name, last_name, role, active)
VALUES ('admin@elms.com',
        '$2b$10$6ODQ5dj7G4U9iXrOJQOoB.jLf71MTg1GNXIU8w6Koa3h7s5pSVnDe',
        'System', 'Admin', 'SYSTEM_ADMIN', TRUE);

-- Seed HR admin user  (password = Admin@123)
INSERT INTO users (email, password, first_name, last_name, role, active)
VALUES ('hr@elms.com',
        '$2b$10$6ODQ5dj7G4U9iXrOJQOoB.jLf71MTg1GNXIU8w6Koa3h7s5pSVnDe',
        'HR', 'Administrator', 'HR_ADMIN', TRUE);

-- Seed public holidays (sample year 2025)
INSERT INTO public_holidays (name, holiday_date) VALUES
  ('New Year''s Day',     '2025-01-01'),
  ('Good Friday',         '2025-04-18'),
  ('Easter Monday',       '2025-04-21'),
  ('Labour Day',          '2025-05-01'),
  ('Christmas Day',       '2025-12-25'),
  ('Boxing Day',          '2025-12-26');
