-- GCT Reporter Database Schema Initialization
-- Version: 1.0
-- Description: Creates 6 core tables for report generation system

-- ============================================================
-- Table: users (用户表)
-- Description: Stores user accounts with roles and authentication
-- ============================================================
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'DESIGNER', 'VIEWER')),
    enabled BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for username lookups (login performance)
CREATE INDEX idx_users_username ON users(username);

-- Index for role-based queries
CREATE INDEX idx_users_role ON users(role);

-- ============================================================
-- Table: reports (报表表)
-- Description: Stores report templates with SQL queries
-- ============================================================
CREATE TABLE reports (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    sql_content TEXT NOT NULL,
    creator_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for name lookups
CREATE INDEX idx_reports_name ON reports(name);

-- Index for creator queries
CREATE INDEX idx_reports_creator ON reports(creator_id);

-- ============================================================
-- Table: report_params (参数表)
-- Description: Stores parameters for dynamic SQL queries
-- ============================================================
CREATE TABLE report_params (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id INTEGER NOT NULL,
    param_name VARCHAR(50) NOT NULL,
    param_type VARCHAR(20) NOT NULL CHECK (param_type IN ('STRING', 'NUMBER', 'DATE', 'DATETIME')),
    required BOOLEAN NOT NULL DEFAULT 0,
    default_value VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    UNIQUE (report_id, param_name)
);

-- Index for report_id lookups
CREATE INDEX idx_report_params_report_id ON report_params(report_id);

-- ============================================================
-- Table: report_columns (列配置表)
-- Description: Stores column display configuration for reports
-- ============================================================
CREATE TABLE report_columns (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id INTEGER NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    column_width INTEGER DEFAULT 120,
    format_type VARCHAR(20) CHECK (format_type IN ('TEXT', 'NUMBER', 'DATE', 'DATETIME', 'CURRENCY')),
    column_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    UNIQUE (report_id, field_name)
);

-- Index for report_id lookups
CREATE INDEX idx_report_columns_report_id ON report_columns(report_id);

-- ============================================================
-- Table: report_permissions (权限表)
-- Description: Stores role-based access control for reports
-- ============================================================
CREATE TABLE report_permissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id INTEGER NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'DESIGNER', 'VIEWER')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    UNIQUE (report_id, role)
);

-- Index for report_id lookups
CREATE INDEX idx_report_permissions_report_id ON report_permissions(report_id);

-- Index for role-based queries
CREATE INDEX idx_report_permissions_role ON report_permissions(role);

-- ============================================================
-- Table: execution_logs (执行日志表)
-- Description: Stores audit logs for report executions
-- ============================================================
CREATE TABLE execution_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    report_id INTEGER NOT NULL,
    params_json TEXT,
    execute_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL DEFAULT 1,
    error_message TEXT,
    execution_duration_ms INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE
);

-- Index for user_id lookups (audit queries)
CREATE INDEX idx_execution_logs_user_id ON execution_logs(user_id);

-- Index for report_id lookups
CREATE INDEX idx_execution_logs_report_id ON execution_logs(report_id);

-- Index for execute_time (performance monitoring)
CREATE INDEX idx_execution_logs_execute_time ON execution_logs(execute_time);

-- ============================================================
-- Schema Initialization Complete
-- 6 tables created with indexes and foreign key constraints
-- ============================================================
