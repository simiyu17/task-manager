package com.task.manage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Set;

/**
 * Security configuration to protect against QueryDSL CVE-2024-49203 (HQL Injection).
 *
 * This configuration adds validation for sort parameters to prevent malicious input
 * in orderBy clauses that could lead to HQL injection attacks.
 *
 * CVE Details:
 * - Vulnerability: HQL injection through orderBy method
 * - Severity: HIGH
 * - Status: No patched version available as of 2026-02-04
 *
 * Mitigation Strategy:
 * 1. Validate all sort field names against allowlist
 * 2. Use Spring Data's Pageable interface (built-in protection)
 * 3. Avoid PathBuilder with user input
 *
 * This configuration intercepts Pageable parameters and validates sort fields.
 */
@Configuration
@Slf4j
public class QueryDslSecurityConfig implements WebMvcConfigurer {

    /**
     * Allowlist of fields that can be used for sorting.
     * Add entity field names here as needed.
     */
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            // Task entity fields
            "id", "title", "description", "status", "dateCreated", "lastModifiedDate",
            "startDate", "endDate", "budget", "createdBy", "lastModifiedBy",

            // Partner entity fields
            "name", "email", "phoneNumber", "organizationType",

            // Document entity fields
            "documentName", "documentType", "version", "uploadDate",

            // Common audit fields
            "createdDate", "modifiedDate"
    );

    /**
     * Validates a sort field name against security rules.
     *
     * @param property the field name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateSortField(String property) {
        // Validate against allowlist
        if (!ALLOWED_SORT_FIELDS.contains(property)) {
            log.warn("Attempted to sort by invalid field: {}", property);
            throw new IllegalArgumentException(
                String.format("Invalid sort field: '%s'. Sorting is not allowed on this field.", property)
            );
        }

        // Additional validation: prevent special characters and SQL keywords
        String upperProperty = property.toUpperCase();
        if (property.contains(" ") || property.contains(";") ||
            property.contains("--") || property.contains("/*") ||
            property.contains("*/") || property.contains("'") ||
            property.contains("\"") || property.contains("\\") ||
            upperProperty.contains("UNION") || upperProperty.contains("SELECT") ||
            upperProperty.contains("INSERT") || upperProperty.contains("UPDATE") ||
            upperProperty.contains("DELETE") || upperProperty.contains("DROP") ||
            upperProperty.contains("EXEC") || upperProperty.contains("CAST") ||
            upperProperty.contains("SLEEP") || upperProperty.contains("WAITFOR")) {
            log.warn("Attempted SQL injection in sort field: {}", property);
            throw new IllegalArgumentException(
                "Sort field contains invalid characters or SQL keywords"
            );
        }
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver() {
            @Override
            public org.springframework.data.domain.Pageable resolveArgument(
                    org.springframework.core.MethodParameter methodParameter,
                    org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                    org.springframework.web.context.request.NativeWebRequest webRequest,
                    org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {

                org.springframework.data.domain.Pageable pageable = super.resolveArgument(
                    methodParameter, mavContainer, webRequest, binderFactory
                );

                // Validate sort fields
                if (pageable != null) {
                    Sort sort = pageable.getSort();
                    if (sort != null && !sort.isEmpty()) {
                        for (Sort.Order order : sort) {
                            validateSortField(order.getProperty());
                        }
                    }
                }

                return pageable;
            }
        };

        // Set fallback sort for safety
        pageableResolver.setFallbackPageable(
            org.springframework.data.domain.PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        );

        resolvers.add(pageableResolver);
    }
}
