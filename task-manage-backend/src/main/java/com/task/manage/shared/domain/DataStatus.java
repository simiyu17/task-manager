package com.task.manage.shared.domain;

import lombok.Getter;

@Getter
public enum DataStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    DELETED;

    private final String displayName;
    DataStatus() {
        this.displayName = this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
    }

    public static DataStatus getDataStatus(String status) {
        for (DataStatus dataStatus : DataStatus.values()) {
            if (dataStatus.name().equalsIgnoreCase(status)) {
                return dataStatus;
            }
        }
        return null;
    }
}
