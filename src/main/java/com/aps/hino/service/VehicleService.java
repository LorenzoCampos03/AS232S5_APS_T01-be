package com.aps.hino.service;

import com.aps.hino.dto.VehicleDto;
import com.aps.hino.model.Vehicle;
import com.aps.hino.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public Mono<VehicleDto> createVehicle(VehicleDto dto) {
        LocalDateTime now = LocalDateTime.now();
        return vehicleRepository.insertVehicleReturnId(
                        dto.getModelo(),
                        dto.getTipo(),
                        dto.getCategoria(),
                        dto.getPrecio(),
                        dto.getCapacidad(),
                        dto.getMotor(),
                        dto.getAnio(),
                        dto.getEstado(),
                        dto.getStock(),
                        dto.getImagenUrl(),
                        dto.getDescripcion(),
                        now,
                        now
                )
                .flatMap(id -> vehicleRepository.findById(id))
                .map(VehicleDto::fromEntity);
    }

    public Mono<VehicleDto> getVehicle(Integer id) {
        return vehicleRepository.findById(id)
                .map(VehicleDto::fromEntity);
    }

    public Flux<VehicleDto> getAll() {
        return vehicleRepository.findAll()
                .map(VehicleDto::fromEntity);
    }

    public Mono<VehicleDto> updateVehicle(Integer id, VehicleDto dto) {
        LocalDateTime now = LocalDateTime.now();
        return vehicleRepository.updateVehicleWithEnums(
                        id,
                        dto.getModelo(),
                        dto.getTipo(),
                        dto.getCategoria(),
                        dto.getPrecio(),
                        dto.getCapacidad(),
                        dto.getMotor(),
                        dto.getAnio(),
                        dto.getEstado(),
                        dto.getStock(),
                        dto.getImagenUrl(),
                        dto.getDescripcion(),
                        now
                )
                .then(vehicleRepository.findById(id))
                .map(VehicleDto::fromEntity);
    }

    public Mono<Void> updateEstado(Integer id, String estado) {
        return vehicleRepository.updateEstado(id, estado, LocalDateTime.now());
    }
}
