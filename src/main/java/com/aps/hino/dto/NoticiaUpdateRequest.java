package com.aps.hino.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@Data
public class NoticiaUpdateRequest {
    private String titulo;
    private String resumen;
    private String contenido;
    private String tipo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime publishAt;

    private Integer ordenPrioridad;
    private String estado; // ACTIVE | INACTIVE
}
