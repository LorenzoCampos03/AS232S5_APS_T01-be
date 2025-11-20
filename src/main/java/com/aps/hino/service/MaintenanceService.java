package com.aps.hino.service;

import com.aps.hino.dto.MaintenanceDto;
import com.aps.hino.model.Maintenance;
import com.aps.hino.repository.MaintenanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;

    // TODO: Replace with actual vehicles endpoint when available
    private static final Map<Integer, Map<String, Object>> VEHICLES_IN_MEMORY = new HashMap<>();

    static {
        VEHICLES_IN_MEMORY.put(1, Map.of("id", 1, "placa", "ABC-123", "modelo", "Hino 500", "año", 2020));
        VEHICLES_IN_MEMORY.put(2, Map.of("id", 2, "placa", "DEF-456", "modelo", "Hino 300", "año", 2021));
        VEHICLES_IN_MEMORY.put(3, Map.of("id", 3, "placa", "GHI-789", "modelo", "Hino 700", "año", 2019));
        VEHICLES_IN_MEMORY.put(4, Map.of("id", 4, "placa", "JKL-012", "modelo", "Hino 500", "año", 2022));
        VEHICLES_IN_MEMORY.put(5, Map.of("id", 5, "placa", "MNO-345", "modelo", "Hino 300", "año", 2020));
    }

    public Mono<MaintenanceDto> createMaintenance(MaintenanceDto dto) {
        return validateVehicleExists(dto.getVehicleId())
                .flatMap(vehicleExists -> {
                    if (!vehicleExists) {
                        return Mono.error(new IllegalArgumentException("Vehicle not found"));
                    }

                    Maintenance maintenance = Maintenance.builder()
                            .vehicleId(dto.getVehicleId())
                            .userId(dto.getUserId())
                            .tipo(dto.getTipo())
                            .descripcion(dto.getDescripcion())
                            .fechaProgramada(dto.getFechaProgramada())
                            .fechaRealizada(dto.getFechaRealizada())
                            .costo(dto.getCosto())
                            .estado(dto.getEstado() != null ? dto.getEstado() : "pendiente")
                            .observaciones(dto.getObservaciones())
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();

                    return maintenanceRepository.save(maintenance)
                            .map(this::toDto)
                            .doOnSuccess(m -> log.info("Maintenance created: {}", m.getId()))
                            .doOnError(e -> log.error("Error creating maintenance", e));
                });
    }

    public Mono<MaintenanceDto> getMaintenance(Integer id) {
        return maintenanceRepository.findById(id)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Maintenance not found")));
    }

    public Flux<MaintenanceDto> getAllMaintenance() {
        return maintenanceRepository.findAll()
                .map(this::toDto);
    }

    public Flux<MaintenanceDto> getMaintenanceByVehicle(Integer vehicleId) {
        return maintenanceRepository.findByVehicleId(vehicleId)
                .map(this::toDto);
    }

    public Flux<MaintenanceDto> getMaintenanceByUser(Integer userId) {
        return maintenanceRepository.findByUserId(userId)
                .map(this::toDto);
    }

    public Flux<MaintenanceDto> getMaintenanceByStatus(String estado) {
        return maintenanceRepository.findByEstado(estado)
                .map(this::toDto);
    }

    public Flux<MaintenanceDto> getUpcomingMaintenance() {
        return maintenanceRepository.findUpcomingMaintenance()
                .map(this::toDto);
    }

    public Flux<MaintenanceDto> getActiveMaintenance() {
        return maintenanceRepository.findAllActive()
                .map(this::toDto);
    }

    public Flux<MaintenanceDto> getCancelledMaintenance() {
        return maintenanceRepository.findAllCancelled()
                .map(this::toDto);
    }

    public Mono<MaintenanceDto> updateMaintenance(Integer id, MaintenanceDto dto) {
        return maintenanceRepository.findById(id)
                .flatMap(existing -> {
                    existing.setTipo(dto.getTipo() != null ? dto.getTipo() : existing.getTipo());
                    existing.setDescripcion(
                            dto.getDescripcion() != null ? dto.getDescripcion() : existing.getDescripcion());
                    existing.setFechaProgramada(dto.getFechaProgramada() != null ? dto.getFechaProgramada()
                            : existing.getFechaProgramada());
                    existing.setFechaRealizada(
                            dto.getFechaRealizada() != null ? dto.getFechaRealizada() : existing.getFechaRealizada());
                    existing.setCosto(dto.getCosto() != null ? dto.getCosto() : existing.getCosto());
                    existing.setEstado(dto.getEstado() != null ? dto.getEstado() : existing.getEstado());
                    existing.setObservaciones(
                            dto.getObservaciones() != null ? dto.getObservaciones() : existing.getObservaciones());
                    existing.setUpdatedAt(OffsetDateTime.now());

                    return maintenanceRepository.save(existing)
                            .map(this::toDto)
                            .doOnSuccess(m -> log.info("Maintenance updated: {}", m.getId()));
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Maintenance not found")));
    }

    public Mono<MaintenanceDto> deleteMaintenance(Integer id) {
        return maintenanceRepository.findById(id)
                .flatMap(existing -> {
                    existing.setEstado("cancelado");
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return maintenanceRepository.save(existing)
                            .map(this::toDto)
                            .doOnSuccess(m -> log.info("Maintenance logically deleted (cancelled): {}", m.getId()));
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Maintenance not found")));
    }

    public Mono<MaintenanceDto> reactivateMaintenance(Integer id) {
        return maintenanceRepository.findById(id)
                .flatMap(existing -> {
                    if (!"cancelado".equalsIgnoreCase(existing.getEstado())) {
                        return Mono
                                .error(new IllegalArgumentException("Only cancelled maintenance can be reactivated"));
                    }
                    existing.setEstado("pendiente");
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return maintenanceRepository.save(existing)
                            .map(this::toDto)
                            .doOnSuccess(m -> log.info("Maintenance reactivated: {}", m.getId()));
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Maintenance not found")));
    }

    public Mono<Map<String, Long>> getMaintenanceStatistics() {
        return Mono.zip(
                maintenanceRepository.countByEstado("pendiente"),
                maintenanceRepository.countByEstado("en-proceso"),
                maintenanceRepository.countByEstado("completado"),
                maintenanceRepository.countByEstado("cancelado")).map(tuple -> {
                    Map<String, Long> stats = new HashMap<>();
                    stats.put("pendiente", tuple.getT1());
                    stats.put("en-proceso", tuple.getT2());
                    stats.put("completado", tuple.getT3());
                    stats.put("cancelado", tuple.getT4());
                    return stats;
                });
    }

    private Mono<Boolean> validateVehicleExists(Integer vehicleId) {
        // TODO: Replace with actual call to vehicles endpoint
        // return webClient.get()
        // .uri("http://localhost:8080/api/vehicles/{id}", vehicleId)
        // .retrieve()
        // .bodyToMono(Void.class)
        // .map(v -> true)
        // .onErrorReturn(false);

        return Mono.just(VEHICLES_IN_MEMORY.containsKey(vehicleId));
    }

    private MaintenanceDto toDto(Maintenance maintenance) {
        return MaintenanceDto.builder()
                .id(maintenance.getId())
                .vehicleId(maintenance.getVehicleId())
                .userId(maintenance.getUserId())
                .tipo(maintenance.getTipo())
                .descripcion(maintenance.getDescripcion())
                .fechaProgramada(maintenance.getFechaProgramada())
                .fechaRealizada(maintenance.getFechaRealizada())
                .costo(maintenance.getCosto())
                .estado(maintenance.getEstado())
                .observaciones(maintenance.getObservaciones())
                .createdAt(maintenance.getCreatedAt())
                .updatedAt(maintenance.getUpdatedAt())
                .build();
    }
}
