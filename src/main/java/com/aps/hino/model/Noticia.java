package com.aps.hino.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("noticias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Noticia {
    @Id
    private Long id;

    private String titulo;

    private String slug;

    private String resumen;

    private String contenido;

    /** tipo: 'promocion' | 'descuentos' | 'lanzamientos' | 'testimonios' */
    private String tipo;

    /** estado: 'ACTIVE' | 'INACTIVE' */
    private String estado = "ACTIVE";

    @Column("publish_at")
    private OffsetDateTime publishAt;

    @Column("orden_prioridad")
    private Integer ordenPrioridad = 0;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
