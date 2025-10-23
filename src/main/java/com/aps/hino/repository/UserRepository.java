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

    /**
     * Update user estado (soft delete/restore)
     * Using CAST to handle PostgreSQL ENUM type
     */
    @Query("UPDATE users SET estado = CAST($2 AS user_status), updated_at = $3 WHERE id = $1")
    Mono<Void> updateEstado(Long id, String estado, java.time.LocalDateTime updatedAt);
    
    /**
     * Update user with all fields using CAST for ENUM types
     */
    @Query("UPDATE users SET nombre = $2, email = $3, telefono = $4, rol = CAST($5 AS user_role), " +
           "especialidad = $6, estado = CAST($7 AS user_status), ventas = $8, fecha_ingreso = $9, " +
           "avatar_url = $10, password_hash = $11, updated_at = $12 WHERE id = $1")
    Mono<Void> updateUser(Long id, String nombre, String email, String telefono, String rol,
                          String especialidad, String estado, Integer ventas, 
                          java.time.LocalDate fechaIngreso, String avatarUrl, 
                          String passwordHash, java.time.LocalDateTime updatedAt);
    
    /**
     * Insert user with CAST for ENUM types
     */
    @Query("INSERT INTO users (nombre, email, telefono, rol, especialidad, estado, ventas, " +
           "fecha_ingreso, avatar_url, password_hash, created_at, updated_at) " +
           "VALUES ($1, $2, $3, CAST($4 AS user_role), $5, CAST($6 AS user_status), $7, $8, $9, $10, $11, $12) " +
           "RETURNING *")
    Mono<User> insertUser(String nombre, String email, String telefono, String rol,
                          String especialidad, String estado, Integer ventas,
                          java.time.LocalDate fechaIngreso, String avatarUrl,
                          String passwordHash, java.time.LocalDateTime createdAt,
                          java.time.LocalDateTime updatedAt);
}
