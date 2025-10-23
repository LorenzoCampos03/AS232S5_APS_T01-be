package com.aps.hino.repository;

import com.aps.hino.model.Maintenance;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MaintenanceRepository extends R2dbcRepository<Maintenance, Integer> {
    
    Flux<Maintenance> findByVehicleId(Integer vehicleId);
    
    Flux<Maintenance> findByUserId(Integer userId);
    
    Flux<Maintenance> findByEstado(String estado);
    
    Flux<Maintenance> findByTipo(String tipo);
    
    @Query("SELECT * FROM maintenance WHERE vehicle_id = :vehicleId AND estado = :estado")
    Flux<Maintenance> findByVehicleIdAndEstado(Integer vehicleId, String estado);
    
    @Query("SELECT * FROM maintenance WHERE user_id = :userId AND estado = :estado")
    Flux<Maintenance> findByUserIdAndEstado(Integer userId, String estado);
    
    @Query("SELECT COUNT(*) FROM maintenance WHERE estado = :estado")
    Mono<Long> countByEstado(String estado);
    
    @Query("""
           SELECT * FROM maintenance 
           WHERE fecha_programada BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days' 
           ORDER BY fecha_programada ASC
           """)
    Flux<Maintenance> findUpcomingMaintenance();

    // 🔹 Nuevos métodos agregados

    // Obtiene todos los mantenimientos que NO están cancelados
    @Query("SELECT * FROM maintenance WHERE estado != 'cancelado' ORDER BY updated_at DESC")
    Flux<Maintenance> findAllActive();

    // Obtiene todos los mantenimientos que están cancelados
    @Query("SELECT * FROM maintenance WHERE estado = 'cancelado' ORDER BY updated_at DESC")
    Flux<Maintenance> findAllCancelled();
}
