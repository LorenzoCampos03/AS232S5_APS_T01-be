package com.aps.hino.model.enums;

public enum UserRole {
    ADMIN("admin"),
    ASESOR("asesor"),
    MECANICO("mecanico"),
    SUPERVISOR("supervisor");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid UserRole: " + value);
    }
}
