package com.aps.hino.rest;

import com.aps.hino.dto.MaintenanceDto;
import com.aps.hino.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Maintenance management endpoints")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final com.aps.hino.service.NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Create new maintenance record")
    public Mono<ResponseEntity<MaintenanceDto>> createMaintenance(@Valid @RequestBody MaintenanceDto dto) {
    return maintenanceService.createMaintenance(dto)
        .flatMap(created -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("maintenance", Long.valueOf(created.getId()), "CREATE", "Mantenimiento creado: " + created.getDescripcion(), actorId, actorEmail).thenReturn(created);
            })
        )
        .map(m -> ResponseEntity.status(HttpStatus.CREATED).body(m))
        .doOnError(e -> log.error("Error creating maintenance", e));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get maintenance by ID")
    public Mono<ResponseEntity<MaintenanceDto>> getMaintenance(@PathVariable Integer id) {
        return maintenanceService.getMaintenance(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get all maintenance records")
    public Flux<MaintenanceDto> getAllMaintenance() {
        return maintenanceService.getAllMaintenance();
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get maintenance by vehicle ID")
    public Flux<MaintenanceDto> getMaintenanceByVehicle(@PathVariable Integer vehicleId) {
        return maintenanceService.getMaintenanceByVehicle(vehicleId);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Get maintenance by user ID")
    public Flux<MaintenanceDto> getMaintenanceByUser(@PathVariable Integer userId) {
        return maintenanceService.getMaintenanceByUser(userId);
    }

    @GetMapping("/status/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get maintenance by status")
    public Flux<MaintenanceDto> getMaintenanceByStatus(@PathVariable String estado) {
        return maintenanceService.getMaintenanceByStatus(estado);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Get upcoming maintenance (next 7 days)")
    public Flux<MaintenanceDto> getUpcomingMaintenance() {
        return maintenanceService.getUpcomingMaintenance();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Update maintenance record")
    public Mono<ResponseEntity<MaintenanceDto>> updateMaintenance(
            @PathVariable Integer id,
            @Valid @RequestBody MaintenanceDto dto) {
    return maintenanceService.updateMaintenance(id, dto)
        .flatMap(updated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("maintenance", Long.valueOf(updated.getId()), "UPDATE", "Mantenimiento actualizado: " + updated.getDescripcion(), actorId, actorEmail).thenReturn(updated);
            })
        )
        .map(ResponseEntity::ok)
        .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Delete maintenance record")
    public Mono<ResponseEntity<Void>> deleteMaintenance(@PathVariable Integer id) {
    return maintenanceService.deleteMaintenance(id)
        .flatMap(deleted -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("maintenance", Long.valueOf(deleted.getId()), "DELETE", "Mantenimiento cancelado: " + deleted.getId(), actorId, actorEmail);
            })
            .thenReturn(ResponseEntity.noContent().<Void>build())
        )
        .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/reactivate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Reactivate cancelled maintenance")
    public Mono<ResponseEntity<MaintenanceDto>> reactivateMaintenance(@PathVariable Integer id) {
    return maintenanceService.reactivateMaintenance(id)
        .flatMap(reactivated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .defaultIfEmpty(null)
            .flatMap(a -> {
                Long actorId = null; String actorEmail = null;
                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                return notificationService.createNotification("maintenance", Long.valueOf(reactivated.getId()), "RESTORE", "Mantenimiento reactivado: " + reactivated.getId(), actorId, actorEmail).thenReturn(reactivated);
            })
        )
        .map(ResponseEntity::ok)
        .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get all active (non-cancelled) maintenance records")
    public Flux<MaintenanceDto> getActiveMaintenance() {
        return maintenanceService.getActiveMaintenance();
    }

    @GetMapping("/cancelled")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Get all cancelled maintenance records")
    public Flux<MaintenanceDto> getCancelledMaintenance() {
        return maintenanceService.getCancelledMaintenance();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Get maintenance statistics")
    public Mono<ResponseEntity<Map<String, Long>>> getStatistics() {
        return maintenanceService.getMaintenanceStatistics()
                .map(ResponseEntity::ok);
    }
}
