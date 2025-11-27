package com.aps.hino.repository;

import com.aps.hino.model.Noticia;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface NoticiaRepository extends R2dbcRepository<Noticia, Long> {

    @Query("SELECT * FROM noticias WHERE estado = 'ACTIVE' AND (publish_at IS NULL OR publish_at <= NOW()) ORDER BY publish_at DESC NULLS LAST, orden_prioridad DESC")
    Flux<Noticia> findPublicList();

    @Query("SELECT * FROM noticias ORDER BY created_at DESC")
    Flux<Noticia> findAllOrderByCreatedAtDesc();

    Mono<Noticia> findBySlug(String slug);

    @Query("INSERT INTO noticias (titulo, slug, resumen, contenido, tipo, estado, publish_at, orden_prioridad, created_at) " +
           "VALUES (:titulo, :slug, :resumen, :contenido, :tipo::noticia_tipo, :estado::noticia_estado, :publishAt, :ordenPrioridad, :createdAt) " +
           "RETURNING id, titulo, slug, resumen, contenido, tipo, estado, publish_at, orden_prioridad, created_at, updated_at")
    Mono<Noticia> insertWithCast(String titulo, String slug, String resumen, String contenido, 
                                  String tipo, String estado, OffsetDateTime publishAt, 
                                  Integer ordenPrioridad, OffsetDateTime createdAt);

    @Query("UPDATE noticias SET titulo = :titulo, slug = :slug, resumen = :resumen, contenido = :contenido, " +
           "tipo = :tipo::noticia_tipo, estado = :estado::noticia_estado, publish_at = :publishAt, " +
           "orden_prioridad = :ordenPrioridad, updated_at = :updatedAt WHERE id = :id " +
           "RETURNING id, titulo, slug, resumen, contenido, tipo, estado, publish_at, orden_prioridad, created_at, updated_at")
    Mono<Noticia> updateWithCast(Long id, String titulo, String slug, String resumen, String contenido, 
                                  String tipo, String estado, OffsetDateTime publishAt, 
                                  Integer ordenPrioridad, OffsetDateTime updatedAt);
}
