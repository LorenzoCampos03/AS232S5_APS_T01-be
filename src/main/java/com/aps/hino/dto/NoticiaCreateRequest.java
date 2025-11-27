package com.aps.hino.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

@Data
public class NoticiaCreateRequest {
    @NotBlank
    private String titulo;

    private String resumen;

    private String contenido;

    @NotBlank
    private String tipo; // promocion|descuentos|lanzamientos|testimonios

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime publishAt;

    private Integer ordenPrioridad = 0;
}
