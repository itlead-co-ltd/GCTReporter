-- V2__init_users.sql
-- 初始化用户表和测试数据

-- 插入测试用户（密码已使用BCrypt加密）
-- admin/admin123
INSERT INTO users (username, password, role, enabled, created_at, updated_at)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- designer/designer123
INSERT INTO users (username, password, role, enabled, created_at, updated_at)
VALUES ('designer', '$2a$10$8K1p/a0dL1LkwxrZ9/UgJeGMEo7PjAd0/vJvf4a0n1Zvl2.G8qB6e', 'DESIGNER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- viewer/viewer123
INSERT INTO users (username, password, role, enabled, created_at, updated_at)
VALUES ('viewer', '$2a$10$vPHs/BzL4K5c2hEYYQ8x4eKwS2/4OUXOvJpU5A0V7FwYZ7ZJhJ5Pu', 'VIEWER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
