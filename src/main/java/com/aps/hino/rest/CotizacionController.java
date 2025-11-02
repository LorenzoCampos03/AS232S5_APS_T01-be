package com.aps.hino.rest;

import com.aps.hino.dto.CotizacionCreateFullRequest;
import com.aps.hino.dto.CotizacionDetailResponse;
import com.aps.hino.dto.CotizacionItemRequest;
import com.aps.hino.dto.CotizacionUpdateRequest;
import com.aps.hino.model.Cotizacion;
import com.aps.hino.model.CotizacionDetalle;
import com.aps.hino.service.CotizacionService;
import com.aps.hino.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(path = "/api/cotizaciones", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService service;
    private final NotificationService notificationService;

    @GetMapping
    public Flux<Cotizacion> list() {
        log.info("📄 Listing all quotations...");
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Cotizacion> getById(@PathVariable Integer id) {
        log.info("🔍 Fetching quotation with ID: {}", id);
        return service.findById(id);
    }

    @GetMapping("/{id}/detalle")
    public Mono<CotizacionDetailResponse> getDetail(@PathVariable Integer id) {
        log.info("🧾 Fetching quotation detail for ID: {}", id);
        return service.getDetail(id);
    }

    @PostMapping(path = "/full", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CotizacionDetailResponse> createFull(@RequestBody CotizacionCreateFullRequest req) {
        log.info("🆕 Creating full quotation...");
        return service.createFull(req)
            .flatMap(resp -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .switchIfEmpty(Mono.empty())
                .flatMap(auth -> {
                    Long actorId = null;
                    String actorEmail = null;
                    try {
                        if (auth != null) {
                            actorEmail = (String) auth.getPrincipal();
                            Object det = auth.getDetails();
                            if (det instanceof Long) actorId = (Long) det;
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Error extracting authentication details: {}", e.getMessage());
                    }
                    return notificationService.createNotification(
                            "cotizaciones",
                            resp.getCabecera().getId() != null ? Long.valueOf(resp.getCabecera().getId()) : null,
                            "CREATE",
                            "Cotización creada: id=" + resp.getCabecera().getId(),
                            actorId,
                            actorEmail
                    ).thenReturn(resp);
                })
            );
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Cotizacion> update(@PathVariable Integer id, @RequestBody CotizacionUpdateRequest req) {
        log.info("♻️ Updating quotation ID: {}", id);
        return service.updateCotizacion(id, req)
            .flatMap(updated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .switchIfEmpty(Mono.empty())
                .flatMap(auth -> {
                    Long actorId = null;
                    String actorEmail = null;
                    try {
                        if (auth != null) {
                            actorEmail = (String) auth.getPrincipal();
                            Object det = auth.getDetails();
                            if (det instanceof Long) actorId = (Long) det;
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Error extracting authentication details: {}", e.getMessage());
                    }
                    return notificationService.createNotification(
                            "cotizaciones",
                            updated.getId() != null ? Long.valueOf(updated.getId()) : null,
                            "UPDATE",
                            "Cotización actualizada: id=" + updated.getId(),
                            actorId,
                            actorEmail
                    ).thenReturn(updated);
                })
            );
    }

    @PostMapping(path = "/{id}/detalle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CotizacionDetalle> addDetalle(@PathVariable Integer id, @RequestBody CotizacionItemRequest item) {
        log.info("➕ Adding item to quotation ID: {}", id);
        return service.addDetalle(id, item)
            .flatMap(saved -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .switchIfEmpty(Mono.empty())
                .flatMap(auth -> {
                    Long actorId = null;
                    String actorEmail = null;
                    try {
                        if (auth != null) {
                            actorEmail = (String) auth.getPrincipal();
                            Object det = auth.getDetails();
                            if (det instanceof Long) actorId = (Long) det;
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Error extracting authentication details: {}", e.getMessage());
                    }
                    return notificationService.createNotification(
                            "cotizaciones",
                            Long.valueOf(id),
                            "ADD_ITEM",
                            "Ítem añadido a cotización id=" + id,
                            actorId,
                            actorEmail
                    ).thenReturn(saved);
                })
            );
    }

    @PutMapping(path = "/detalle/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CotizacionDetalle> updateDetalle(@PathVariable Integer itemId, @RequestBody CotizacionItemRequest item) {
        log.info("✏️ Updating quotation item ID: {}", itemId);
        return service.updateDetalle(itemId, item)
            .flatMap(updated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .switchIfEmpty(Mono.empty())
                .flatMap(auth -> {
                    Long actorId = null;
                    String actorEmail = null;
                    try {
                        if (auth != null) {
                            actorEmail = (String) auth.getPrincipal();
                            Object det = auth.getDetails();
                            if (det instanceof Long) actorId = (Long) det;
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Error extracting authentication details: {}", e.getMessage());
                    }
                    return notificationService.createNotification(
                            "cotizaciones",
                            updated.getCotizacionId() != null ? Long.valueOf(updated.getCotizacionId()) : null,
                            "UPDATE_ITEM",
                            "Ítem de cotización actualizado: itemId=" + updated.getId(),
                            actorId,
                            actorEmail
                    ).thenReturn(updated);
                })
            );
    }

    @DeleteMapping(path = "/detalle/{itemId}")
    public Mono<Void> deleteDetalle(@PathVariable Integer itemId) {
        log.info("🗑️ Deleting quotation item ID: {}", itemId);
        return service.deleteDetalle(itemId)
            .flatMap(v -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .switchIfEmpty(Mono.empty())
                .flatMap(auth -> {
                    Long actorId = null;
                    String actorEmail = null;
                    try {
                        if (auth != null) {
                            actorEmail = (String) auth.getPrincipal();
                            Object det = auth.getDetails();
                            if (det instanceof Long) actorId = (Long) det;
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Error extracting authentication details: {}", e.getMessage());
                    }
                    return notificationService.createNotification(
                            "cotizaciones",
                            null,
                            "DELETE_ITEM",
                            "Ítem de cotización eliminado: itemId=" + itemId,
                            actorId,
                            actorEmail
                    );
                })
            )
            .then();
    }
}
