package com.task.manage.shared.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum DataStatus {
    ACTIVE,
    APPROVED,
    REJECTED,
    DELETED;

    @JsonValue
    public String getName() {
        return this.name();
    }

    public String getDisplayName() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
    }

    /**
     * Get DataStatus from string value (case-insensitive)
     * @param value the string value
     * @return DataStatus or null if not found
     */
    public static DataStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        for (DataStatus dataStatus : DataStatus.values()) {
            if (dataStatus.name().equalsIgnoreCase(value)) {
                return dataStatus;
            }
        }
        return null;
    }

    /**
     * @deprecated Use {@link #fromString(String)} instead
     */
    @Deprecated
    public static DataStatus getDataStatus(String status) {
        return fromString(status);
    }
}
