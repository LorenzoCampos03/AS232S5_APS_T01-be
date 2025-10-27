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
public class CotizacionController {

    private final CotizacionService service;

    public CotizacionController(CotizacionService service) {
        this.service = service;
    }

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
        return service.createFull(req);
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Cotizacion> update(@PathVariable Integer id, @RequestBody CotizacionUpdateRequest req) {
        return service.updateCotizacion(id, req);
    }

    @PostMapping(path = "/{id}/detalle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<com.aps.hino.model.CotizacionDetalle> addDetalle(@PathVariable Integer id, @RequestBody CotizacionItemRequest item) {
        return service.addDetalle(id, item);
    }

    @PutMapping(path = "/detalle/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<com.aps.hino.model.CotizacionDetalle> updateDetalle(@PathVariable Integer itemId, @RequestBody CotizacionItemRequest item) {
        return service.updateDetalle(itemId, item);
    }

    @DeleteMapping(path = "/detalle/{itemId}")
    public Mono<Void> deleteDetalle(@PathVariable Integer itemId) {
        return service.deleteDetalle(itemId);
    }
}
