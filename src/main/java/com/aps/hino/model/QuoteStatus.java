package com.aps.hino.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuoteStatus {
    pendiente("pendiente"),
    en_proceso("en-proceso"),
    enviada("enviada"),
    cerrada("cerrada");

    private final String value;

    QuoteStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static QuoteStatus fromValue(String value) {
        for (QuoteStatus status : QuoteStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Valor inválido para QuoteStatus: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
