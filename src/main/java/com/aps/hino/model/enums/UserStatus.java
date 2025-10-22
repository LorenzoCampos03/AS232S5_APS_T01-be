package com.aps.hino.model.enums;

public enum UserStatus {
    ACTIVO("activo"),
    INACTIVO("inactivo");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserStatus fromValue(String value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid UserStatus: " + value);
    }
}
