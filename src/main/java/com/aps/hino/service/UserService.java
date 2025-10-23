package com.aps.hino.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.aps.hino.exception.ResourceNotFoundException;
import com.aps.hino.model.User;
import com.aps.hino.repository.UserRepository;
import com.aps.hino.util.PasswordUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get all users with pagination (including both active and inactive)
     */
    public Mono<Map<String, Object>> getAllUsers(int page, int size) {
        log.debug("Getting all users - page: {}, size: {}", page, size);

        long skip = (long) page * size;

        return userRepository.count()
                .flatMap(total -> {
                    return userRepository.findAll()
                            .skip(skip)
                            .take(size)
                            .map(com.aps.hino.dto.UserDto::fromEntity)
                            .collectList()
                            .map(users -> {
                                Map<String, Object> result = new HashMap<>();
                                result.put("content", users);
                                result.put("currentPage", page);
                                result.put("pageSize", size);
                                result.put("totalElements", total);
                                result.put("totalPages", (int) Math.ceil((double) total / size));
                                return result;
                            });
                });
    }



    /**
     * Get user by ID
     */
    public Mono<User> getUserById(Long id) {
        log.debug("Getting user by id: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario", id)));
    }

    /**
     * Get user by email
     */
    public Mono<User> getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Create a new user
     */
    public Mono<User> createUser(User user) {
        log.debug("Creating user: {}", user.getEmail());

        // Check if email already exists
        return userRepository.findByEmail(user.getEmail())
                .flatMap(existingUser -> {
                    log.warn("User with email {} already exists", user.getEmail());
                    return Mono.error(new IllegalArgumentException("El email ya está registrado"));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Encriptar contraseña con BCrypt
                    String passwordHash = null;
                    if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                        passwordHash = PasswordUtil.hashPassword(user.getPasswordHash());
                    }

                    // Set default values
                    LocalDate fechaIngreso = user.getFechaIngreso() != null ? user.getFechaIngreso() : LocalDate.now();
                    Integer ventas = user.getVentas() != null ? user.getVentas() : 0;
                    String estado = user.getEstado() != null ? user.getEstado() : "activo";

                    // createdAt se establece automáticamente, updatedAt debe ser null en creación
                    LocalDateTime createdAt = LocalDateTime.now();

                    return userRepository.insertUser(
                            user.getNombre(),
                            user.getEmail(),
                            user.getTelefono(),
                            user.getRol(),
                            user.getEspecialidad(),
                            estado,
                            ventas,
                            fechaIngreso,
                            user.getAvatarUrl(),
                            passwordHash,
                            createdAt,
                            null  // updatedAt es null al crear
                    );
                }))
                .cast(User.class);
    }

    /**
     * Update an existing user
     */
    public Mono<User> updateUser(Long id, User user) {
        log.debug("Updating user with id: {}", id);

        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario", id)))
                .flatMap(existingUser -> {
                    // Check if email is being changed and if it's already taken
                    if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
                        return userRepository.findByEmail(user.getEmail())
                                .flatMap(emailUser -> {
                                    log.warn("Email {} is already taken", user.getEmail());
                                    return Mono
                                            .<User>error(new IllegalArgumentException("El email ya está registrado"));
                                })
                                .switchIfEmpty(Mono.defer(() -> updateUserFields(existingUser, user)));
                    } else {
                        return updateUserFields(existingUser, user);
                    }
                });
    }

    private Mono<User> updateUserFields(User existingUser, User user) {
        // Update fields
        String nombre = user.getNombre() != null ? user.getNombre() : existingUser.getNombre();
        String email = user.getEmail() != null ? user.getEmail() : existingUser.getEmail();
        String telefono = user.getTelefono() != null ? user.getTelefono() : existingUser.getTelefono();
        String rol = user.getRol() != null ? user.getRol() : existingUser.getRol();
        String especialidad = user.getEspecialidad() != null ? user.getEspecialidad() : existingUser.getEspecialidad();
        String estado = user.getEstado() != null ? user.getEstado() : existingUser.getEstado();
        Integer ventas = user.getVentas() != null ? user.getVentas() : existingUser.getVentas();
        LocalDate fechaIngreso = user.getFechaIngreso() != null ? user.getFechaIngreso() : existingUser.getFechaIngreso();
        String avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : existingUser.getAvatarUrl();

        // Encriptar contraseña con BCrypt si se proporciona
        String passwordHash = existingUser.getPasswordHash();
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            passwordHash = PasswordUtil.hashPassword(user.getPasswordHash());
        }

        LocalDateTime updatedAt = LocalDateTime.now();

        return userRepository.updateUser(
                existingUser.getId(),
                nombre,
                email,
                telefono,
                rol,
                especialidad,
                estado,
                ventas,
                fechaIngreso,
                avatarUrl,
                passwordHash,
                updatedAt
        ).then(userRepository.findById(existingUser.getId()));
    }

    /**
     * Soft delete a user (logical deletion by changing estado to inactivo)
     */
    public Mono<Void> deleteUser(Long id) {
        log.debug("Soft deleting user with id: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario", id)))
                .flatMap(user -> {
                    if ("inactivo".equals(user.getEstado())) {
                        log.warn("User with id {} is already deleted", id);
                        return Mono.error(new IllegalArgumentException("El usuario ya está eliminado"));
                    }
                    return userRepository.updateEstado(id, "inactivo", LocalDateTime.now());
                });
    }

    /**
     * Restore a soft deleted user (change estado back to activo)
     */
    public Mono<User> restoreUser(Long id) {
        log.debug("Restoring user with id: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario", id)))
                .flatMap(user -> {
                    if ("activo".equals(user.getEstado())) {
                        log.warn("User with id {} is not deleted", id);
                        return Mono.error(new IllegalArgumentException("El usuario no está eliminado"));
                    }
                    return userRepository.updateEstado(id, "activo", LocalDateTime.now())
                            .then(userRepository.findById(id));
                });
    }

    // ELIMINACIÓN FÍSICA REMOVIDA - Solo se permite eliminación lógica (soft
    // delete)
    // La eliminación física puede causar problemas de integridad referencial

    /**
     * Get user statistics (optimized with database queries, excluding soft deleted)
     */
    public Mono<Map<String, Object>> getUserStats() {
        log.debug("Getting user statistics");

        return Mono.zip(
                userRepository.countAll(),
                userRepository.countByRolActive("admin"),
                userRepository.countByRolActive("asesor"),
                userRepository.countByRolActive("mecanico"),
                userRepository.countByRolActive("supervisor"),
                userRepository.countByEstadoActive("activo"),
                userRepository.countByEstadoActive("inactivo"),
                userRepository.sumVentasActive()).map(tuple -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("total", tuple.getT1());
                    stats.put("admins", tuple.getT2());
                    stats.put("asesores", tuple.getT3());
                    stats.put("mecanicos", tuple.getT4());
                    stats.put("supervisores", tuple.getT5());
                    stats.put("activos", tuple.getT6());
                    stats.put("inactivos", tuple.getT7());
                    stats.put("ventasTotales", tuple.getT8());
                    return stats;
                });
    }
}
