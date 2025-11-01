package com.aps.hino.rest;

import com.aps.hino.dto.VehicleDto;
import com.aps.hino.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle management endpoints")
public class VehicleController {

    private final VehicleService vehicleService;
    private final com.aps.hino.service.NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create new vehicle")
    public Mono<ResponseEntity<VehicleDto>> create(@Valid @RequestBody VehicleDto dto) {
    return vehicleService.createVehicle(dto)
        .flatMap(created -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("vehicles", Long.valueOf(created.getId()), "CREATE", "Vehículo creado: " + created.getModelo(), actorId, actorEmail).thenReturn(created);
            })
        )
        .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
        .doOnError(e -> log.error("Error creating vehicle", e));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public Mono<ResponseEntity<VehicleDto>> getById(@PathVariable Integer id) {
        return vehicleService.getVehicle(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all vehicles")
    public Flux<VehicleDto> getAll() {
        return vehicleService.getAll();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle")
    public Mono<ResponseEntity<VehicleDto>> update(@PathVariable Integer id, @Valid @RequestBody VehicleDto dto) {
    return vehicleService.updateVehicle(id, dto)
        .flatMap(updated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("vehicles", Long.valueOf(updated.getId()), "UPDATE", "Vehículo actualizado: " + updated.getModelo(), actorId, actorEmail).thenReturn(updated);
            })
        )
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Update vehicle estado")
    public Mono<ResponseEntity<Void>> updateEstado(@PathVariable Integer id, @RequestParam String estado) {
        return vehicleService.updateEstado(id, estado)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
