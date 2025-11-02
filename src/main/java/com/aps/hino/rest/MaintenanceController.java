package com.aps.hino.rest;

import com.aps.hino.dto.MaintenanceDto;
import com.aps.hino.service.MaintenanceService;
import com.aps.hino.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Maintenance management endpoints")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final NotificationService notificationService;

    /** 🔹 Crear nuevo mantenimiento */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Create new maintenance record")
    public Mono<ResponseEntity<MaintenanceDto>> createMaintenance(@Valid @RequestBody MaintenanceDto dto) {
        return maintenanceService.createMaintenance(dto)
                .flatMap(created -> getAuthenticatedUser()
                        .flatMap(user -> notificationService.createNotification(
                                "maintenance",
                                Long.valueOf(created.getId()),
                                "CREATE",
                                "Mantenimiento creado: " + created.getDescripcion(),
                                user.actorId(),
                                user.actorEmail()
                        ).thenReturn(created))
                )
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                .doOnError(e -> log.error("❌ Error creating maintenance", e));
    }

    /** 🔹 Obtener mantenimiento por ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get maintenance by ID")
    public Mono<ResponseEntity<MaintenanceDto>> getMaintenance(@PathVariable Integer id) {
        return maintenanceService.getMaintenance(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /** 🔹 Listar todos los mantenimientos */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get all maintenance records")
    public Flux<MaintenanceDto> getAllMaintenance() {
        return maintenanceService.getAllMaintenance();
    }

    /** 🔹 Listar mantenimientos por vehículo */
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get maintenance by vehicle ID")
    public Flux<MaintenanceDto> getMaintenanceByVehicle(@PathVariable Integer vehicleId) {
        return maintenanceService.getMaintenanceByVehicle(vehicleId);
    }

    /** 🔹 Listar mantenimientos por usuario */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Get maintenance by user ID")
    public Flux<MaintenanceDto> getMaintenanceByUser(@PathVariable Integer userId) {
        return maintenanceService.getMaintenanceByUser(userId);
    }

    /** 🔹 Filtrar por estado */
    @GetMapping("/status/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get maintenance by status")
    public Flux<MaintenanceDto> getMaintenanceByStatus(@PathVariable String estado) {
        return maintenanceService.getMaintenanceByStatus(estado);
    }

    /** 🔹 Mantenimientos próximos (7 días) */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Get upcoming maintenance (next 7 days)")
    public Flux<MaintenanceDto> getUpcomingMaintenance() {
        return maintenanceService.getUpcomingMaintenance();
    }

    /** 🔹 Actualizar mantenimiento */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Update maintenance record")
    public Mono<ResponseEntity<MaintenanceDto>> updateMaintenance(
            @PathVariable Integer id,
            @Valid @RequestBody MaintenanceDto dto) {
        return maintenanceService.updateMaintenance(id, dto)
                .flatMap(updated -> getAuthenticatedUser()
                        .flatMap(user -> notificationService.createNotification(
                                "maintenance",
                                Long.valueOf(updated.getId()),
                                "UPDATE",
                                "Mantenimiento actualizado: " + updated.getDescripcion(),
                                user.actorId(),
                                user.actorEmail()
                        ).thenReturn(updated))
                )
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .doOnError(e -> log.error("❌ Error updating maintenance", e));
    }

    /** 🔹 Eliminar mantenimiento */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete maintenance record")
    public Mono<ResponseEntity<Void>> deleteMaintenance(@PathVariable Integer id) {
        return maintenanceService.deleteMaintenance(id)
                .flatMap(deleted -> getAuthenticatedUser()
                        .flatMap(user -> notificationService.createNotification(
                                "maintenance",
                                Long.valueOf(deleted.getId()),
                                "DELETE",
                                "Mantenimiento eliminado: " + deleted.getId(),
                                user.actorId(),
                                user.actorEmail()
                        ).thenReturn(ResponseEntity.noContent().<Void>build()))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .doOnError(e -> log.error("❌ Error deleting maintenance", e));
    }

    /** 🔹 Reactivar mantenimiento */
    @PutMapping("/reactivate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Reactivate cancelled maintenance")
    public Mono<ResponseEntity<MaintenanceDto>> reactivateMaintenance(@PathVariable Integer id) {
        return maintenanceService.reactivateMaintenance(id)
                .flatMap(reactivated -> getAuthenticatedUser()
                        .flatMap(user -> notificationService.createNotification(
                                "maintenance",
                                Long.valueOf(reactivated.getId()),
                                "RESTORE",
                                "Mantenimiento reactivado: " + reactivated.getId(),
                                user.actorId(),
                                user.actorEmail()
                        ).thenReturn(reactivated))
                )
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()))
                .doOnError(e -> log.error("❌ Error reactivating maintenance", e));
    }

    /** 🔹 Activos */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR', 'USER')")
    @Operation(summary = "Get all active (non-cancelled) maintenance records")
    public Flux<MaintenanceDto> getActiveMaintenance() {
        return maintenanceService.getActiveMaintenance();
    }

    /** 🔹 Cancelados */
    @GetMapping("/cancelled")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Get all cancelled maintenance records")
    public Flux<MaintenanceDto> getCancelledMaintenance() {
        return maintenanceService.getCancelledMaintenance();
    }

    /** 🔹 Estadísticas */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'SUPERVISOR')")
    @Operation(summary = "Get maintenance statistics")
    public Mono<ResponseEntity<Map<String, Long>>> getStatistics() {
        return maintenanceService.getMaintenanceStatistics()
                .map(ResponseEntity::ok);
    }

    /** 🔹 Método auxiliar para obtener usuario autenticado */
    private Mono<AuthUser> getAuthenticatedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(a -> {
                    String email = null;
                    Long id = null;
                    try {
                        email = (String) a.getPrincipal();
                        Object det = a.getDetails();
                        if (det instanceof Long) id = (Long) det;
                    } catch (Exception ignored) {}
                    return new AuthUser(id, email);
                })
                .defaultIfEmpty(new AuthUser(null, null));
    }

    /** 🔹 Record simple para transportar usuario */
    private record AuthUser(Long actorId, String actorEmail) {}
}
