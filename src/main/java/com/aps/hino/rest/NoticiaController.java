package com.aps.hino.rest;

import com.aps.hino.dto.NoticiaCreateRequest;
import com.aps.hino.dto.NoticiaResponse;
import com.aps.hino.dto.NoticiaUpdateRequest;
import com.aps.hino.service.NoticiaService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class NoticiaController {

    private final NoticiaService noticiaService;

    public NoticiaController(NoticiaService noticiaService) {
        this.noticiaService = noticiaService;
    }

    // Public endpoints
    @GetMapping("/public/noticias")
    public Flux<NoticiaResponse> listPublic() {
        return noticiaService.listPublic();
    }

    @GetMapping("/public/noticias/{slug}")
    public Mono<NoticiaResponse> getPublicBySlug(@PathVariable String slug) {
        return noticiaService.getBySlug(slug);
    }

    // Admin endpoints
    @GetMapping("/admin/noticias")
    public Flux<NoticiaResponse> listAll() {
        return noticiaService.listAll();
    }

    @PostMapping(value = "/admin/noticias", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<NoticiaResponse> createNoticiaJson(@RequestBody @Validated NoticiaCreateRequest data) {
        return noticiaService.create(data, null);
    }

    @PostMapping(value = "/admin/noticias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<NoticiaResponse> createNoticiaMultipart(
            @RequestPart("data") @Validated NoticiaCreateRequest data,
            @RequestPart(value = "images", required = false) Flux<FilePart> images
    ) {
        return noticiaService.create(data, images);
    }

    @PostMapping(value = "/admin/noticias/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Flux<String> uploadImages(@PathVariable Long id,
                                     @RequestPart(value = "images") Flux<FilePart> images) {
        return noticiaService.uploadImages(id, images);
    }

    @DeleteMapping("/admin/noticias/{noticiaId}/imagenes/{imageId}")
    public Mono<Map<String, String>> deleteImage(@PathVariable Long noticiaId, @PathVariable Long imageId) {
        return noticiaService.deleteImage(noticiaId, imageId).thenReturn(Map.of("status", "deleted"));
    }

    @PutMapping("/admin/noticias/{noticiaId}/imagenes/{imageId}/cover")
    public Mono<Map<String, String>> setCover(@PathVariable Long noticiaId, @PathVariable Long imageId) {
        return noticiaService.setCoverImage(noticiaId, imageId).thenReturn(Map.of("status", "ok"));
    }

    @PutMapping("/admin/noticias/{noticiaId}/imagenes/reorder")
    public Mono<Map<String, String>> reorderImages(@PathVariable Long noticiaId, @RequestBody com.aps.hino.dto.NoticiaReorderRequest req) {
        return noticiaService.reorderImages(noticiaId, req.getImageIds()).thenReturn(Map.of("status", "ok"));
    }

    @PutMapping("/admin/noticias/{id}")
    public Mono<NoticiaResponse> updateNoticia(@PathVariable Long id, @RequestBody NoticiaUpdateRequest req) {
        return noticiaService.update(id, req);
    }

    @DeleteMapping("/admin/noticias/{id}")
    public Mono<Map<String, String>> deleteNoticia(@PathVariable Long id) {
        return noticiaService.softDelete(id).thenReturn(Map.of("status", "deleted"));
    }

    @PostMapping("/admin/noticias/{id}/restore")
    public Mono<Map<String, String>> restoreNoticia(@PathVariable Long id) {
        return noticiaService.restore(id).thenReturn(Map.of("status", "restored"));
    }
}
