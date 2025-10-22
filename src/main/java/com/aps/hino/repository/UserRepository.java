package com.aps.hino.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import com.aps.hino.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    /**
     * Find user by email (for authentication)
     */
    Mono<User> findByEmail(String email);

    /**
     * Find all users by role (admin, asesor, driver)
     * Using CAST to handle PostgreSQL ENUM type
     */
    @Query("SELECT * FROM users WHERE rol::text = $1")
    Flux<User> findByRol(String rol);

    /**
     * Find all users by status (activo, inactivo)
     * Using CAST to handle PostgreSQL ENUM type
     */
    @Query("SELECT * FROM users WHERE estado::text = $1")
    Flux<User> findByEstado(String estado);

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(*) FROM users WHERE rol::text = $1")
    Mono<Long> countByRol(String rol);

    /**
     * Count users by status
     */
    @Query("SELECT COUNT(*) FROM users WHERE estado::text = $1")
    Mono<Long> countByEstado(String estado);

    /**
     * Get total sales sum
     */
    @Query("SELECT COALESCE(SUM(ventas), 0) FROM users")
    Mono<Integer> sumVentas();

    /**
     * Count total active users
     */
    @Query("SELECT COUNT(*) FROM users WHERE estado::text = 'activo'")
    Mono<Long> countAll();

    /**
     * Find all active users
     */
    @Query("SELECT * FROM users WHERE estado::text = 'activo'")
    Flux<User> findAllActive();

    /**
     * Find all inactive users (soft deleted)
     */
    @Query("SELECT * FROM users WHERE estado::text = 'inactivo'")
    Flux<User> findAllDeleted();

    /**
     * Count users by role (only active)
     */
    @Query("SELECT COUNT(*) FROM users WHERE rol::text = $1 AND estado::text = 'activo'")
    Mono<Long> countByRolActive(String rol);

    /**
     * Count users by status
     */
    @Query("SELECT COUNT(*) FROM users WHERE estado::text = $1")
    Mono<Long> countByEstadoActive(String estado);

    /**
     * Get total sales sum (only active users)
     */
    @Query("SELECT COALESCE(SUM(ventas), 0) FROM users WHERE estado::text = 'activo'")
    Mono<Integer> sumVentasActive();

    /**
     * Find active user by email (for authentication)
     */
    @Query("SELECT * FROM users WHERE email = $1 AND estado::text = 'activo'")
    Mono<User> findActiveByEmail(String email);
}
