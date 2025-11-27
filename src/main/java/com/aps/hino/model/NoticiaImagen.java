package com.aps.hino.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("noticia_imagen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticiaImagen {
    @Id
    private Long id;

    @Column("noticia_id")
    private Long noticiaId;

    @Column("file_name")
    private String fileName;

    @Column("content_type")
    private String contentType;

    private byte[] data;

    @Column("file_size")
    private Long fileSize;

    private Integer orden = 0;

    @Column("is_cover")
    private Boolean cover = false;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
