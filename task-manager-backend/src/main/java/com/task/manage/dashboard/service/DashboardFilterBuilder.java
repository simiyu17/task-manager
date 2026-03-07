package com.task.manage.dashboard.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for building SQL WHERE clauses with filters
 */
class DashboardFilterBuilder {

    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * Add donor ID filter
     */
    public DashboardFilterBuilder withDonorId(Long donorId) {
        if (donorId != null) {
            conditions.add("t.donor_id = :donorId");
            parameters.put("donorId", donorId);
        }
        return this;
    }

    /**
     * Add partner ID filter
     */
    public DashboardFilterBuilder withPartnerId(Long partnerId) {
        if (partnerId != null) {
            conditions.add("t.assigned_partner_id = :partnerId");
            parameters.put("partnerId", partnerId);
        }
        return this;
    }

    /**
     * Add from date filter
     */
    public DashboardFilterBuilder withFromDate(LocalDate fromDate) {
        if (fromDate != null) {
            conditions.add("t.date_created >= :fromDate");
            parameters.put("fromDate", fromDate.atStartOfDay());
        }
        return this;
    }

    /**
     * Add to date filter
     */
    public DashboardFilterBuilder withToDate(LocalDate toDate) {
        if (toDate != null) {
            conditions.add("t.date_created <= :toDate");
            parameters.put("toDate", toDate.plusDays(1).atStartOfDay());
        }
        return this;
    }

    /**
     * Add date range filter for history table
     */
    public DashboardFilterBuilder withHistoryDateRange(LocalDate fromDate, LocalDate toDate, String tableAlias) {
        if (fromDate != null) {
            conditions.add(tableAlias + ".changed_at >= :fromDate");
            parameters.put("fromDate", fromDate.atStartOfDay());
        }
        if (toDate != null) {
            conditions.add(tableAlias + ".changed_at <= :toDate");
            parameters.put("toDate", toDate.plusDays(1).atStartOfDay());
        }
        return this;
    }

    /**
     * Build WHERE clause
     */
    public String buildWhereClause() {
        if (conditions.isEmpty()) {
            return "";
        }
        return " AND " + String.join(" AND ", conditions);
    }

    /**
     * Get parameters map
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Check if any filters are active
     */
    public boolean hasFilters() {
        return !conditions.isEmpty();
    }
}

