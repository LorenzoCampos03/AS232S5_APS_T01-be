package com.aps.hino.repository;

import com.aps.hino.model.Vehicle;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Repository
public interface VehicleRepository extends R2dbcRepository<Vehicle, Integer> {

    @Query("UPDATE vehicles SET estado = CAST($2 AS vehicle_status), updated_at = $3 WHERE id = $1")
    Mono<Void> updateEstado(Integer id, String estado, LocalDateTime updatedAt);

    @Query("INSERT INTO vehicles (modelo, tipo, categoria, precio, capacidad, motor, anio, estado, stock, imagen_url, descripcion, created_at, updated_at) " +
           "VALUES ($1, CAST($2 AS vehicle_type), $3, $4, $5, $6, $7, CAST($8 AS vehicle_status), $9, $10, $11, $12, $13) RETURNING id")
    Mono<Integer> insertVehicleReturnId(
            String modelo,
            String tipo,
            String categoria,
            BigDecimal precio,
            String capacidad,
            String motor,
            Integer anio,
            String estado,
            Integer stock,
            String imagenUrl,
            String descripcion,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    );

    @Query("UPDATE vehicles SET " +
           "modelo = $2, " +
           "tipo = CAST($3 AS vehicle_type), " +
           "categoria = $4, " +
           "precio = $5, " +
           "capacidad = $6, " +
           "motor = $7, " +
           "anio = $8, " +
           "estado = CAST($9 AS vehicle_status), " +
           "stock = $10, " +
           "imagen_url = $11, " +
           "descripcion = $12, " +
           "updated_at = $13 " +
           "WHERE id = $1")
    Mono<Void> updateVehicleWithEnums(
            Integer id,
            String modelo,
            String tipo,
            String categoria,
            BigDecimal precio,
            String capacidad,
            String motor,
            Integer anio,
            String estado,
            Integer stock,
            String imagenUrl,
            String descripcion,
            LocalDateTime updatedAt
    );
}
