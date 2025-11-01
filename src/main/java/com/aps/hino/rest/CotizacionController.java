package com.aps.hino.rest;

import com.aps.hino.dto.CotizacionCreateFullRequest;
import com.aps.hino.dto.CotizacionDetailResponse;
import com.aps.hino.dto.CotizacionItemRequest;
import com.aps.hino.dto.CotizacionUpdateRequest;
import com.aps.hino.model.Cotizacion;
import com.aps.hino.service.CotizacionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/cotizaciones", produces = MediaType.APPLICATION_JSON_VALUE)
@lombok.RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService service;
    private final com.aps.hino.service.NotificationService notificationService;

    @GetMapping
    public Flux<Cotizacion> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Cotizacion> getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/detalle")
    public Mono<CotizacionDetailResponse> getDetail(@PathVariable Integer id) {
        return service.getDetail(id);
    }

    @PostMapping(path = "/full", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CotizacionDetailResponse> createFull(@RequestBody CotizacionCreateFullRequest req) {
    return service.createFull(req)
        .flatMap(resp -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("cotizaciones", resp.getCabecera().getId() != null ? Long.valueOf(resp.getCabecera().getId()) : null, "CREATE", "Cotización creada: id=" + resp.getCabecera().getId(), actorId, actorEmail).thenReturn(resp);
            })
        );
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Cotizacion> update(@PathVariable Integer id, @RequestBody CotizacionUpdateRequest req) {
    return service.updateCotizacion(id, req)
        .flatMap(updated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("cotizaciones", Long.valueOf(updated.getId()), "UPDATE", "Cotización actualizada: id=" + updated.getId(), actorId, actorEmail).thenReturn(updated);
            })
        );
    }

    @PostMapping(path = "/{id}/detalle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<com.aps.hino.model.CotizacionDetalle> addDetalle(@PathVariable Integer id, @RequestBody CotizacionItemRequest item) {
    return service.addDetalle(id, item)
        .flatMap(saved -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("cotizaciones", Long.valueOf(id), "ADD_ITEM", "Ítem añadido a cotización id=" + id, actorId, actorEmail).thenReturn(saved);
            })
        );
    }

    @PutMapping(path = "/detalle/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<com.aps.hino.model.CotizacionDetalle> updateDetalle(@PathVariable Integer itemId, @RequestBody CotizacionItemRequest item) {
    return service.updateDetalle(itemId, item)
        .flatMap(updated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("cotizaciones", Long.valueOf(updated.getCotizacionId()), "UPDATE_ITEM", "Ítem de cotización actualizado: itemId=" + updated.getId(), actorId, actorEmail).thenReturn(updated);
            })
        );
    }

    @DeleteMapping(path = "/detalle/{itemId}")
    public Mono<Void> deleteDetalle(@PathVariable Integer itemId) {
    return service.deleteDetalle(itemId)
        .flatMap(v -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("cotizaciones", null, "DELETE_ITEM", "Ítem de cotización eliminado: itemId=" + itemId, actorId, actorEmail);
            })
            .thenReturn((Void) null)
        );
    }
}
