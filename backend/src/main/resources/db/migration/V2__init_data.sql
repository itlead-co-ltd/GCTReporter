-- GCT Reporter Initial Data
-- Version: 2.0
-- Description: Insert test accounts with BCrypt encrypted passwords

-- ============================================================
-- Insert Test User Accounts
-- All passwords follow pattern: <username>123
-- Password encryption: BCrypt (strength 10)
-- ============================================================

-- Admin Account (Full Access)
-- Username: admin
-- Password: admin123
INSERT INTO users (username, password, role, enabled) 
VALUES (
    'admin', 
    '$2a$10$UZsXLxHAPwujAtYUE.YCw.Z0idWYW6EXFy.lCJcQSjtN4PSMAlUKO', 
    'ADMIN', 
    1
);

-- Designer Account (Report Design & Query)
-- Username: designer
-- Password: designer123
INSERT INTO users (username, password, role, enabled) 
VALUES (
    'designer', 
    '$2a$10$KlxlxJ208jRJwtfFafzFp.v6G14ciURWOxlKEu8Oh/R4C6.lbZM52', 
    'DESIGNER', 
    1
);

-- Viewer Account (Query Only)
-- Username: viewer
-- Password: viewer123
INSERT INTO users (username, password, role, enabled) 
VALUES (
    'viewer', 
    '$2a$10$Txz/4MKjLfoFN1uqf7aIqeEMBLW5Gtrl2t7/tqbxIyfDq3LcGYWUq', 
    'VIEWER', 
    1
);

-- ============================================================
-- Data Initialization Complete
-- 3 test accounts created
-- ============================================================
