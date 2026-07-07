-- 1. Setting null is_group values to false in application_record
UPDATE application_record SET is_group = false WHERE is_group IS NULL;

-- 2. Changing campaign.is_group to VARCHAR
UPDATE campaign SET is_group = '0' WHERE is_group IS NULL;
ALTER TABLE campaign MODIFY COLUMN is_group VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL';
UPDATE campaign SET is_group = 'INDIVIDUAL' WHERE is_group = '0' OR is_group = 'false';
UPDATE campaign SET is_group = 'GROUP' WHERE is_group = '1' OR is_group = 'true';

-- 3. Changing campaign.level to VARCHAR(30)
UPDATE campaign SET level = 'UNIVERSITY' WHERE level IS NULL;
ALTER TABLE campaign MODIFY COLUMN level VARCHAR(30) NOT NULL;

-- 4. Changing standard.level to VARCHAR(30)
ALTER TABLE standard MODIFY COLUMN level VARCHAR(30) NULL;

-- 5. Changing application_record.level to VARCHAR(30)
ALTER TABLE application_record MODIFY COLUMN level VARCHAR(30) NULL;

-- 6. Seed detailed permissions
INSERT INTO permissions (code, name, description)
VALUES 
('MANAGE_NOTIFICATION_SETTINGS', 'Manage notification settings', 'Configure SMTP, realtime notification rules, and notification templates'),
('USER_MANAGE', 'Quản lý người dùng', 'Tạo tài khoản, phân quyền, đổi vai trò và trạng thái tài khoản'),
('CAMPAIGN_MANAGE', 'Quản lý đợt xét chọn', 'Tạo, sửa, xóa đợt xét chọn, cập nhật trạng thái đợt xét chọn'),
('CRITERIA_MANAGE', 'Quản lý tiêu chuẩn và tiêu chí', 'Cấu hình tiêu chuẩn, tiêu chí, sắp xếp thứ tự và yêu cầu minh chứng'),
('EVIDENCE_REVIEW', 'Duyệt minh chứng', 'Xem chi tiết, nhận xét, phê duyệt hoặc từ chối minh chứng của sinh viên'),
('SETTING_MANAGE', 'Quản lý cấu hình hệ thống', 'Quản lý các cài đặt chung và xem thông tin tổng quan dashboard'),
('AUDIT_LOG_VIEW', 'Xem nhật ký hoạt động', 'Tra cứu lịch sử thao tác của các tài khoản quản trị trên hệ thống')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- 7. Auto-assign all permissions to existing ADMIN users
INSERT IGNORE INTO user_permissions (user_id, permission_code)
SELECT u.id, p.code
FROM users u, permissions p
WHERE u.role = 'ADMIN';
