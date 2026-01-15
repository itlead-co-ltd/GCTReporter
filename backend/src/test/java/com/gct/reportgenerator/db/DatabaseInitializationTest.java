package com.gct.reportgenerator.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database Initialization Integration Test
 * Verifies Flyway migration, table creation, and initial data
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("US003: 数据库初始化集成测试")
class DatabaseInitializationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("T003-1: 验证6张核心表创建成功")
    void testTablesCreated() {
        // Given: Database initialized by Flyway
        String[] expectedTables = {
            "users",
            "reports", 
            "report_params",
            "report_columns",
            "report_permissions",
            "execution_logs"
        };
        
        // When: Query SQLite master table
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'flyway_schema_history' ORDER BY name";
        List<String> actualTables = jdbcTemplate.queryForList(sql, String.class);
        
        // Then: All 6 tables should exist
        assertEquals(6, actualTables.size(), "应该有6张核心表");
        for (String expectedTable : expectedTables) {
            assertTrue(actualTables.contains(expectedTable), 
                "表 " + expectedTable + " 应该存在");
        }
    }

    @Test
    @DisplayName("T003-1: 验证users表结构包含所有必需字段")
    void testUsersTableStructure() {
        // Given & When: Query table structure
        String sql = "PRAGMA table_info(users)";
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        // Then: Verify all required columns exist
        String[] expectedColumns = {"id", "username", "password", "role", "enabled", "created_at", "updated_at"};
        assertEquals(7, columns.size(), "users表应该有7个字段");
        
        for (String expectedColumn : expectedColumns) {
            boolean found = columns.stream()
                .anyMatch(col -> expectedColumn.equals(col.get("name")));
            assertTrue(found, "字段 " + expectedColumn + " 应该存在");
        }
    }

    @Test
    @DisplayName("T003-1: 验证reports表包含外键约束")
    void testReportsTableForeignKey() {
        // Given & When: Query foreign keys
        String sql = "PRAGMA foreign_key_list(reports)";
        List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(sql);
        
        // Then: Should have foreign key to users table
        assertFalse(foreignKeys.isEmpty(), "reports表应该有外键约束");
        assertEquals("users", foreignKeys.get(0).get("table"), 
            "外键应该指向users表");
    }

    @Test
    @DisplayName("T003-1: 验证索引创建成功")
    void testIndexesCreated() {
        // Given & When: Query indexes
        String sql = "SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'idx_%' ORDER BY name";
        List<String> indexes = jdbcTemplate.queryForList(sql, String.class);
        
        // Then: Should have at least 10 custom indexes
        assertTrue(indexes.size() >= 10, 
            "应该至少有10个自定义索引，实际有 " + indexes.size() + " 个");
        
        // Verify key indexes exist
        assertTrue(indexes.contains("idx_users_username"), 
            "应该有 idx_users_username 索引");
        assertTrue(indexes.contains("idx_reports_name"), 
            "应该有 idx_reports_name 索引");
        assertTrue(indexes.contains("idx_execution_logs_execute_time"), 
            "应该有 idx_execution_logs_execute_time 索引");
    }

    @Test
    @DisplayName("T003-2: 验证3个测试账号创建成功")
    void testTestAccountsCreated() {
        // Given & When: Query users table
        String sql = "SELECT username, role, enabled FROM users ORDER BY id";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql);
        
        // Then: Should have 3 users
        assertEquals(3, users.size(), "应该有3个测试账号");
        
        // Verify admin account
        Map<String, Object> admin = users.get(0);
        assertEquals("admin", admin.get("username"));
        assertEquals("ADMIN", admin.get("role"));
        assertEquals(1, admin.get("enabled"));
        
        // Verify designer account
        Map<String, Object> designer = users.get(1);
        assertEquals("designer", designer.get("username"));
        assertEquals("DESIGNER", designer.get("role"));
        assertEquals(1, designer.get("enabled"));
        
        // Verify viewer account
        Map<String, Object> viewer = users.get(2);
        assertEquals("viewer", viewer.get("username"));
        assertEquals("VIEWER", viewer.get("role"));
        assertEquals(1, viewer.get("enabled"));
    }

    @Test
    @DisplayName("T003-2: 验证密码使用BCrypt加密")
    void testPasswordsEncryptedWithBCrypt() {
        // Given: BCrypt encoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // When: Query passwords
        String sql = "SELECT username, password FROM users ORDER BY id";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql);
        
        // Then: All passwords should be BCrypt hashes
        for (Map<String, Object> user : users) {
            String password = (String) user.get("password");
            assertNotNull(password, "密码不应该为空");
            assertTrue(password.startsWith("$2a$"), 
                "密码应该是BCrypt格式（以$2a$开头）");
            assertTrue(password.length() == 60, 
                "BCrypt密码长度应该是60个字符");
        }
        
        // Verify admin password (admin123)
        String adminPassword = (String) users.get(0).get("password");
        assertTrue(encoder.matches("admin123", adminPassword), 
            "admin密码应该能匹配 admin123");
        
        // Verify designer password (designer123)
        String designerPassword = (String) users.get(1).get("password");
        assertTrue(encoder.matches("designer123", designerPassword), 
            "designer密码应该能匹配 designer123");
        
        // Verify viewer password (viewer123)
        String viewerPassword = (String) users.get(2).get("password");
        assertTrue(encoder.matches("viewer123", viewerPassword), 
            "viewer密码应该能匹配 viewer123");
    }

    @Test
    @DisplayName("T003-3: 验证Flyway迁移历史记录")
    void testFlywayMigrationHistory() {
        // Given & When: Query flyway_schema_history
        String sql = "SELECT installed_rank, version, description, type, script, success FROM flyway_schema_history ORDER BY installed_rank";
        List<Map<String, Object>> migrations = jdbcTemplate.queryForList(sql);
        
        // Then: Should have 2 migrations
        assertEquals(2, migrations.size(), "应该有2条迁移记录");
        
        // Verify first migration
        Map<String, Object> migration1 = migrations.get(0);
        assertEquals(1, migration1.get("installed_rank"));
        assertEquals("1", migration1.get("version"));
        assertEquals("init schema", migration1.get("description"));
        assertEquals("SQL", migration1.get("type"));
        assertEquals("V1__init_schema.sql", migration1.get("script"));
        assertEquals(1, migration1.get("success"));
        
        // Verify second migration
        Map<String, Object> migration2 = migrations.get(1);
        assertEquals(2, migration2.get("installed_rank"));
        assertEquals("2", migration2.get("version"));
        assertEquals("init data", migration2.get("description"));
        assertEquals("SQL", migration2.get("type"));
        assertEquals("V2__init_data.sql", migration2.get("script"));
        assertEquals(1, migration2.get("success"));
    }

    @Test
    @DisplayName("T003-3: 验证数据库约束生效")
    void testDatabaseConstraints() {
        // Test 1: Username unique constraint
        String insertDuplicateUser = "INSERT INTO users (username, password, role, enabled) VALUES ('admin', 'test', 'ADMIN', 1)";
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute(insertDuplicateUser);
        }, "重复的username应该抛出异常");
        
        // Test 2: Role check constraint
        String insertInvalidRole = "INSERT INTO users (username, password, role, enabled) VALUES ('test', 'test', 'INVALID', 1)";
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute(insertInvalidRole);
        }, "无效的role应该抛出异常");
    }

    @Test
    @DisplayName("T003-3: 验证所有表包含时间戳字段")
    void testTimestampFields() {
        // Given: Table names
        String[] tables = {"users", "reports", "report_params", "report_columns", "report_permissions", "execution_logs"};
        
        // When & Then: Check each table has created_at and updated_at
        for (String table : tables) {
            String sql = "PRAGMA table_info(" + table + ")";
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
            
            boolean hasCreatedAt = columns.stream()
                .anyMatch(col -> "created_at".equals(col.get("name")));
            boolean hasUpdatedAt = columns.stream()
                .anyMatch(col -> "updated_at".equals(col.get("name")));
            
            assertTrue(hasCreatedAt, table + "表应该有created_at字段");
            assertTrue(hasUpdatedAt, table + "表应该有updated_at字段");
        }
    }
}
