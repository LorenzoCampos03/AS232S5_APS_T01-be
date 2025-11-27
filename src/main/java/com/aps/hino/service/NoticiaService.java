package com.aps.hino.service;

import com.aps.hino.dto.NoticiaCreateRequest;
import com.aps.hino.dto.NoticiaResponse;
import com.aps.hino.dto.NoticiaUpdateRequest;
import com.aps.hino.model.Noticia;
import com.aps.hino.model.NoticiaImagen;
import com.aps.hino.repository.NoticiaImagenRepository;
import com.aps.hino.repository.NoticiaRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBufferUtils;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.Base64;

@Service
public class NoticiaService {

    private final NoticiaRepository noticiaRepository;
    private final NoticiaImagenRepository imagenRepository;
    private final DatabaseClient databaseClient;

    public NoticiaService(NoticiaRepository noticiaRepository, NoticiaImagenRepository imagenRepository, DatabaseClient databaseClient) {
        this.noticiaRepository = noticiaRepository;
        this.imagenRepository = imagenRepository;
        this.databaseClient = databaseClient;
    }

    public Flux<NoticiaResponse> listPublic() {
        return noticiaRepository.findPublicList().flatMap(this::toResponseMono);
    }

    public Flux<NoticiaResponse> listAll() {
        return noticiaRepository.findAllOrderByCreatedAtDesc().flatMap(this::toResponseMono);
    }

    public Mono<NoticiaResponse> getBySlug(String slug) {
        return noticiaRepository.findBySlug(slug).flatMap(this::toResponseMono);
    }

    public Mono<NoticiaResponse> create(NoticiaCreateRequest req, Flux<FilePart> images) {
        String titulo = req.getTitulo();
        String resumen = req.getResumen();
        String contenido = req.getContenido();
        String tipo = req.getTipo();
        String estado = "ACTIVE";
        OffsetDateTime publishAt = req.getPublishAt();
        Integer ordenPrioridad = req.getOrdenPrioridad() == null ? 0 : req.getOrdenPrioridad();
        OffsetDateTime createdAt = OffsetDateTime.now();

        return generateSlug(titulo)
                .flatMap(slug -> databaseClient.sql(
                    "INSERT INTO noticias (titulo, slug, resumen, contenido, tipo, estado, publish_at, orden_prioridad, created_at) " +
                    "VALUES (:titulo, :slug, :resumen, :contenido, :tipo::noticia_tipo, :estado::noticia_estado, :publishAt, :ordenPrioridad, :createdAt) " +
                    "RETURNING id, titulo, slug, resumen, contenido, tipo, estado, publish_at, orden_prioridad, created_at, updated_at"
                )
                .bind("titulo", titulo)
                .bind("slug", slug)
                .bind("resumen", resumen)
                .bind("contenido", contenido)
                .bind("tipo", tipo)
                .bind("estado", estado)
                .bind("publishAt", publishAt != null ? publishAt : io.r2dbc.spi.Parameters.in(io.r2dbc.spi.R2dbcType.TIMESTAMP_WITH_TIME_ZONE))
                .bind("ordenPrioridad", ordenPrioridad)
                .bind("createdAt", createdAt)
                .map((row, metadata) -> {
                    Noticia n = new Noticia();
                    n.setId(row.get("id", Long.class));
                    n.setTitulo(row.get("titulo", String.class));
                    n.setSlug(row.get("slug", String.class));
                    n.setResumen(row.get("resumen", String.class));
                    n.setContenido(row.get("contenido", String.class));
                    n.setTipo(row.get("tipo", String.class));
                    n.setEstado(row.get("estado", String.class));
                    n.setPublishAt(row.get("publish_at", OffsetDateTime.class));
                    n.setOrdenPrioridad(row.get("orden_prioridad", Integer.class));
                    n.setCreatedAt(row.get("created_at", OffsetDateTime.class));
                    n.setUpdatedAt(row.get("updated_at", OffsetDateTime.class));
                    return n;
                })
                .one())
                .flatMap(saved -> {
                    if (images == null) return toResponseMono(saved);
                    return images.flatMap(filePart -> saveFileForNoticia(saved.getId(), filePart))
                            .collectList()
                            .flatMap(imgs -> toResponseMono(saved));
                });
    }

    public Mono<NoticiaResponse> update(Long id, NoticiaUpdateRequest req) {
        return noticiaRepository.findById(id)
                .flatMap(existing -> {
                    String titulo = req.getTitulo() != null ? req.getTitulo() : existing.getTitulo();
                    String resumen = req.getResumen() != null ? req.getResumen() : existing.getResumen();
                    String contenido = req.getContenido() != null ? req.getContenido() : existing.getContenido();
                    String tipo = req.getTipo() != null ? req.getTipo() : existing.getTipo();
                    String estado = req.getEstado() != null ? req.getEstado() : existing.getEstado();
                    OffsetDateTime publishAt = req.getPublishAt() != null ? req.getPublishAt() : existing.getPublishAt();
                    Integer ordenPrioridad = req.getOrdenPrioridad() != null ? req.getOrdenPrioridad() : existing.getOrdenPrioridad();
                    OffsetDateTime updatedAt = OffsetDateTime.now();

                    Mono<String> slugMono = (req.getTitulo() != null && !req.getTitulo().equals(existing.getTitulo()))
                            ? generateSlug(titulo)
                            : Mono.just(existing.getSlug());

                    return slugMono.flatMap(slug -> databaseClient.sql(
                        "UPDATE noticias SET titulo = :titulo, slug = :slug, resumen = :resumen, contenido = :contenido, " +
                        "tipo = :tipo::noticia_tipo, estado = :estado::noticia_estado, publish_at = :publishAt, " +
                        "orden_prioridad = :ordenPrioridad, updated_at = :updatedAt WHERE id = :id " +
                        "RETURNING id, titulo, slug, resumen, contenido, tipo, estado, publish_at, orden_prioridad, created_at, updated_at"
                    )
                    .bind("id", id)
                    .bind("titulo", titulo)
                    .bind("slug", slug)
                    .bind("resumen", resumen)
                    .bind("contenido", contenido)
                    .bind("tipo", tipo)
                    .bind("estado", estado)
                    .bind("publishAt", publishAt != null ? publishAt : io.r2dbc.spi.Parameters.in(io.r2dbc.spi.R2dbcType.TIMESTAMP_WITH_TIME_ZONE))
                    .bind("ordenPrioridad", ordenPrioridad)
                    .bind("updatedAt", updatedAt)
                    .map((row, metadata) -> {
                        Noticia n = new Noticia();
                        n.setId(row.get("id", Long.class));
                        n.setTitulo(row.get("titulo", String.class));
                        n.setSlug(row.get("slug", String.class));
                        n.setResumen(row.get("resumen", String.class));
                        n.setContenido(row.get("contenido", String.class));
                        n.setTipo(row.get("tipo", String.class));
                        n.setEstado(row.get("estado", String.class));
                        n.setPublishAt(row.get("publish_at", OffsetDateTime.class));
                        n.setOrdenPrioridad(row.get("orden_prioridad", Integer.class));
                        n.setCreatedAt(row.get("created_at", OffsetDateTime.class));
                        n.setUpdatedAt(row.get("updated_at", OffsetDateTime.class));
                        return n;
                    })
                    .one());
                })
                .flatMap(this::toResponseMono);
    }

    public Mono<Void> softDelete(Long id) {
        return databaseClient.sql(
            "UPDATE noticias SET estado = 'INACTIVE'::noticia_estado, updated_at = :updatedAt WHERE id = :id"
        )
        .bind("id", id)
        .bind("updatedAt", OffsetDateTime.now())
        .fetch()
        .rowsUpdated()
        .then();
    }

    public Mono<Void> restore(Long id) {
        return databaseClient.sql(
            "UPDATE noticias SET estado = 'ACTIVE'::noticia_estado, updated_at = :updatedAt WHERE id = :id"
        )
        .bind("id", id)
        .bind("updatedAt", OffsetDateTime.now())
        .fetch()
        .rowsUpdated()
        .then();
    }

    private Mono<NoticiaImagen> saveFileForNoticia(Long noticiaId, FilePart filePart) {
        String fname = filePart.filename();
        String contentType = filePart.headers().getContentType() != null 
            ? filePart.headers().getContentType().toString() 
            : "application/octet-stream";

        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    NoticiaImagen img = new NoticiaImagen();
                    img.setNoticiaId(noticiaId);
                    img.setFileName(fname);
                    img.setContentType(contentType);
                    img.setData(bytes);
                    img.setFileSize((long) bytes.length);
                    img.setOrden(0);
                    img.setCover(false);
                    img.setCreatedAt(OffsetDateTime.now());
                    return imagenRepository.save(img);
                });
    }

    private Mono<NoticiaResponse> toResponseMono(Noticia n) {
        return imagenRepository.findByNoticiaIdOrderByOrden(n.getId())
                .map(img -> "data:" + img.getContentType() + ";base64," + Base64.getEncoder().encodeToString(img.getData()))
                .collectList()
                .map(list -> {
                    NoticiaResponse r = new NoticiaResponse();
                    r.setId(n.getId());
                    r.setTitulo(n.getTitulo());
                    r.setSlug(n.getSlug());
                    r.setResumen(n.getResumen());
                    r.setContenido(n.getContenido());
                    r.setTipo(n.getTipo());
                    r.setEstado(n.getEstado());
                    r.setPublishAt(n.getPublishAt());
                    r.setOrdenPrioridad(n.getOrdenPrioridad());
                    r.setCreatedAt(n.getCreatedAt());
                    r.setUpdatedAt(n.getUpdatedAt());
                    r.setImagenes(list);
                    return r;
                });
    }

    private Mono<String> generateSlug(String input) {
        if (input == null) return Mono.just(UUID.randomUUID().toString());
        String s = input.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[áàäâ]","a")
                .replaceAll("[éèëê]","e")
                .replaceAll("[íìïî]","i")
                .replaceAll("[óòöô]","o")
                .replaceAll("[úùüû]","u")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-");
        String base = s;
        return noticiaRepository.findBySlug(base)
                .flatMap(existing -> Mono.just(base + "-" + UUID.randomUUID().toString().substring(0,6)))
                .switchIfEmpty(Mono.just(base));
    }

    // Image management operations
    public Flux<String> uploadImages(Long noticiaId, Flux<FilePart> images) {
        if (images == null) return Flux.empty();
        return images.flatMap(fp -> saveFileForNoticia(noticiaId, fp))
                .map(img -> "data:" + img.getContentType() + ";base64," + Base64.getEncoder().encodeToString(img.getData()));
    }

    public Mono<Void> deleteImage(Long noticiaId, Long imageId) {
        return imagenRepository.findById(imageId)
                .flatMap(img -> {
                    if (!img.getNoticiaId().equals(noticiaId)) return Mono.empty();
                    return imagenRepository.deleteById(imageId);
                });
    }

    public Mono<Void> setCoverImage(Long noticiaId, Long imageId) {
        return imagenRepository.findByNoticiaIdOrderByOrden(noticiaId).collectList()
                .flatMap(list -> {
                    return imagenRepository.findById(imageId)
                            .flatMap(img -> {
                                if (!img.getNoticiaId().equals(noticiaId)) return Mono.empty();
                                // unset others
                                return Flux.fromIterable(list)
                                        .flatMap(i -> {
                                            if (i.getId().equals(imageId)) {
                                                i.setCover(true);
                                            } else {
                                                i.setCover(false);
                                            }
                                            return imagenRepository.save(i);
                                        })
                                        .then();
                            });
                });
    }

    public Mono<Void> reorderImages(Long noticiaId, java.util.List<Long> orderedIds) {
        return imagenRepository.findByNoticiaIdOrderByOrden(noticiaId).collectList()
                .flatMap(existing -> {
                    java.util.Map<Long, NoticiaImagen> map = new java.util.HashMap<>();
                    existing.forEach(i -> map.put(i.getId(), i));
                    java.util.List<Mono<NoticiaImagen>> saves = new java.util.ArrayList<>();
                    for (int idx = 0; idx < orderedIds.size(); idx++) {
                        Long id = orderedIds.get(idx);
                        NoticiaImagen ni = map.get(id);
                        if (ni != null) {
                            ni.setOrden(idx);
                            saves.add(imagenRepository.save(ni));
                        }
                    }
                    return Flux.concat(saves).then();
                });
    }
}
