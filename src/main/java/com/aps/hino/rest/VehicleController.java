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
        log.info("🚛 Creating vehicle: {}", dto.getModelo());
        return vehicleService.createVehicle(dto)
                .doOnSuccess(created -> log.info("✅ Vehicle created successfully: ID={}, Model={}", created.getId(),
                        created.getModelo()))
                .flatMap(created -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> ctx.getAuthentication())
                        .switchIfEmpty(Mono.empty())
                        .flatMap(a -> {
                            Long actorId = null;
                            String actorEmail = null;
                            if (a != null) {
                                try {
                                    actorEmail = (String) a.getPrincipal();
                                    Object det = a.getDetails();
                                    if (det instanceof Long)
                                        actorId = (Long) det;
                                    log.info("🔐 Authentication found: actorId={}, actorEmail={}", actorId, actorEmail);
                                } catch (Exception e) {
                                    log.warn("⚠️ Error extracting authentication details: {}", e.getMessage());
                                }
                            } else {
                                log.warn("⚠️ No authentication context found");
                            }
                            log.info("📝 About to create notification for vehicle creation");
                            return notificationService
                                    .createNotification("vehicles",
                                            created.getId() != null ? Long.valueOf(created.getId()) : null, "CREATE",
                                            "Vehículo creado: " + created.getModelo(), actorId, actorEmail)
                                    .thenReturn(created);
                        }))
                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
                .doOnError(e -> log.error("❌ Error creating vehicle", e));
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
        log.info("🔄 Updating vehicle ID: {}, Model: {}", id, dto.getModelo());
        return vehicleService.updateVehicle(id, dto)
                .doOnSuccess(updated -> log.info("✅ Vehicle updated successfully: ID={}, Model={}", updated.getId(),
                        updated.getModelo()))
                .flatMap(updated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> ctx.getAuthentication())
                        .switchIfEmpty(Mono.empty())
                        .flatMap(a -> {
                            Long actorId = null;
                            String actorEmail = null;
                            if (a != null) {
                                try {
                                    actorEmail = (String) a.getPrincipal();
                                    Object det = a.getDetails();
                                    if (det instanceof Long)
                                        actorId = (Long) det;
                                    log.info("🔐 Authentication found: actorId={}, actorEmail={}", actorId, actorEmail);
                                } catch (Exception e) {
                                    log.warn("⚠️ Error extracting authentication details: {}", e.getMessage());
                                }
                            } else {
                                log.warn("⚠️ No authentication context found");
                            }
                            log.info("📝 About to create notification for vehicle update");
                            return notificationService
                                    .createNotification("vehicles",
                                            updated.getId() != null ? Long.valueOf(updated.getId()) : null, "UPDATE",
                                            "Vehículo actualizado: " + updated.getModelo(), actorId, actorEmail)
                                    .thenReturn(updated);
                        }))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(e -> log.error("❌ Error updating vehicle with id: {}", id, e));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Update vehicle estado")
    public Mono<ResponseEntity<Void>> updateEstado(@PathVariable Integer id, @RequestParam String estado) {
        log.info("🔄 Updating vehicle status: ID={}, newStatus={}", id, estado);
        return vehicleService.updateEstado(id, estado)
                .then(org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> ctx.getAuthentication())
                        .switchIfEmpty(Mono.empty())
                        .flatMap(a -> {
                            Long actorId = null;
                            String actorEmail = null;
                            if (a != null) {
                                try {
                                    actorEmail = (String) a.getPrincipal();
                                    Object det = a.getDetails();
                                    if (det instanceof Long)
                                        actorId = (Long) det;
                                    log.info("🔐 Authentication found: actorId={}, actorEmail={}", actorId, actorEmail);
                                } catch (Exception e) {
                                    log.warn("⚠️ Error extracting authentication details: {}", e.getMessage());
                                }
                            } else {
                                log.warn("⚠️ No authentication context found");
                            }
                            log.info("📝 About to create notification for vehicle status update");
                            return notificationService.createNotification("vehicles", Long.valueOf(id), "UPDATE_STATUS",
                                    "Estado de vehículo actualizado a: " + estado, actorId, actorEmail);
                        }))
                .thenReturn(ResponseEntity.noContent().<Void>build())
                .doOnError(e -> log.error("❌ Error updating vehicle status for id: {}", id, e));
    }

    // Endpoint de prueba simple
    @GetMapping("/test-simple")
    @Operation(summary = "Simple test")
    public Mono<ResponseEntity<String>> testSimple() {
        log.info("🧪 Simple test endpoint called");
        return Mono.just(ResponseEntity.ok("Test endpoint working!"));
    }

    // Endpoint de prueba para verificar notificaciones
    @PostMapping("/test-notification")
    @Operation(summary = "Test notification creation")
    public Mono<ResponseEntity<String>> testNotification() {
        log.info("🧪 Testing notification creation");

        try {
            return notificationService
                    .createNotification("vehicles", 999L, "TEST", "Esta es una notificación de prueba", 1L,
                            "test@example.com")
                    .map(notification -> {
                        log.info("✅ Test notification saved with ID: {}", notification.getId());
                        return ResponseEntity.ok("Notification created with ID: " + notification.getId());
                    })
                    .doOnError(e -> {
                        log.error("❌ Error creating test notification: {}", e.getMessage(), e);
                    })
                    .onErrorReturn(ResponseEntity.status(500).body("Error: Check server logs for details"));
        } catch (Exception e) {
            log.error("❌ Exception in test notification: {}", e.getMessage(), e);
            return Mono.just(ResponseEntity.status(500).body("Exception: " + e.getMessage()));
        }
    }
}
