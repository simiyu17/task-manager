package com.task.manage.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for QueryDSL CVE-2024-49203 protection.
 * Verifies that malicious sort parameters are blocked.
 */
class QueryDslSecurityConfigTest {

    @Test
    void testValidSortField_shouldPass() {
        // Valid field names should not throw exception
        assertDoesNotThrow(() -> QueryDslSecurityConfig.validateSortField("id"));
        assertDoesNotThrow(() -> QueryDslSecurityConfig.validateSortField("title"));
        assertDoesNotThrow(() -> QueryDslSecurityConfig.validateSortField("dateCreated"));
        assertDoesNotThrow(() -> QueryDslSecurityConfig.validateSortField("name"));
    }

    @Test
    void testInvalidSortField_shouldThrowException() {
        // Invalid field names should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("invalidField");
        });
        assertTrue(exception.getMessage().contains("Invalid sort field"));
    }

    @Test
    void testSqlInjectionAttempt_shouldBlock() {
        // SQL injection patterns should be blocked
        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("name UNION SELECT");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("id; DROP TABLE users--");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("name'--");
        });
    }

    @Test
    void testSqlKeywords_shouldBlock() {
        // SQL keywords should be blocked even in lowercase
        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("idunion");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("nameSELECT");
        });
    }

    @Test
    void testSpecialCharacters_shouldBlock() {
        // Special characters should be blocked
        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("id;");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("name--");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("id/*comment*/");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("name'");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField("id\\");
        });
    }

    @Test
    void testAllowedFields_shouldIncludeCommonFields() {
        // Verify important fields are in allowlist
        assertTrue(QueryDslSecurityConfig.ALLOWED_SORT_FIELDS.contains("id"));
        assertTrue(QueryDslSecurityConfig.ALLOWED_SORT_FIELDS.contains("title"));
        assertTrue(QueryDslSecurityConfig.ALLOWED_SORT_FIELDS.contains("name"));
        assertTrue(QueryDslSecurityConfig.ALLOWED_SORT_FIELDS.contains("dateCreated"));
        assertTrue(QueryDslSecurityConfig.ALLOWED_SORT_FIELDS.contains("email"));
    }

    @Test
    void testCvePoC_shouldBlock() {
        // Test the actual CVE-2024-49203 proof of concept pattern
        assertThrows(IllegalArgumentException.class, () -> {
            QueryDslSecurityConfig.validateSortField(
                "name INTERSECT SELECT t FROM Test t WHERE (SELECT cast(pg_sleep(10) AS text))='2' ORDER BY t.id"
            );
        });
    }
}
