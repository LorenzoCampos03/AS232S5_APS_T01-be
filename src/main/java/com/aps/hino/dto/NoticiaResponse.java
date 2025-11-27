package com.aps.hino.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticiaResponse {
    private Long id;
    private String titulo;
    private String slug;
    private String resumen;
    private String contenido;
    private String tipo;
    private String estado;
    private OffsetDateTime publishAt;
    private Integer ordenPrioridad;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<String> imagenes; // file paths
}
