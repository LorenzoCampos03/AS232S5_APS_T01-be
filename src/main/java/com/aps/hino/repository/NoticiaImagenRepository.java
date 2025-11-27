package com.aps.hino.repository;

import com.aps.hino.model.NoticiaImagen;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NoticiaImagenRepository extends R2dbcRepository<NoticiaImagen, Long> {
    Flux<NoticiaImagen> findByNoticiaIdOrderByOrden(Long noticiaId);
}
