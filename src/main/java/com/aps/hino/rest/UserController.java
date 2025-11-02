package com.aps.hino.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.aps.hino.dto.ApiResponse;
import com.aps.hino.dto.UserDto;
import com.aps.hino.model.User;
import com.aps.hino.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (admins, asesores, mecánicos, supervisores)")
public class UserController {

        private final UserService userService;
        private final com.aps.hino.service.NotificationService notificationService;

        @GetMapping
        @Operation(summary = "Obtener todos los usuarios", description = "Retorna una lista paginada de todos los usuarios del sistema")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @SecurityRequirement(name = "Bearer Authentication")
        public Mono<ApiResponse<Map<String, Object>>> getAllUsers(
                        @Parameter(description = "Número de página (inicia en 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
                log.info("GET /api/users - page: {}, size: {}", page, size);
                return userService.getAllUsers(page, size)
                                .map(result -> ApiResponse.success("Usuarios obtenidos exitosamente", result));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener usuario por ID", description = "Retorna los detalles de un usuario específico")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        @SecurityRequirement(name = "Bearer Authentication")
        public Mono<ApiResponse<UserDto>> getUserById(
                        @Parameter(description = "ID del usuario") @PathVariable Long id) {
                log.info("GET /api/users/{}", id);
                return userService.getUserById(id)
                                .map(UserDto::fromEntity)
                                .map(dto -> ApiResponse.success("Usuario encontrado", dto));
        }

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Crear nuevo usuario", description = "Crea un nuevo usuario en el sistema. Solo accesible para administradores.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email ya está registrado")
        })
        @SecurityRequirement(name = "Bearer Authentication")
        public Mono<org.springframework.http.ResponseEntity<ApiResponse<UserDto>>> createUser(
                        @Parameter(description = "Datos del nuevo usuario") @Valid @RequestBody UserDto userDTO) {
                log.info("POST /api/users - Creating user: {}", userDTO.getEmail());

                User user = userDTO.toEntity();
                // Set password hash from password field
                if (userDTO.getPassword() != null) {
                        user.setPasswordHash(userDTO.getPassword());
                }

                return userService.createUser(user)
                                .flatMap(created -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                                        .map(ctx -> ctx.getAuthentication())
                                        .switchIfEmpty(Mono.empty())
                                        .flatMap(a -> {
                                                Long actorId = null; String actorEmail = null;
                                                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                                                return notificationService.createNotification("users", created.getId(), "CREATE", "Usuario creado: " + created.getEmail(), actorId, actorEmail).thenReturn(created);
                                        })
                                )
                                .map(UserDto::fromEntity)
                                .map(dto -> ApiResponse.success("Usuario creado exitosamente", dto))
                                .map(response -> org.springframework.http.ResponseEntity.status(HttpStatus.CREATED)
                                                .body(response));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente. Solo accesible para administradores.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email ya está registrado")
        })
        @SecurityRequirement(name = "Bearer Authentication")
        public Mono<ApiResponse<UserDto>> updateUser(
                        @Parameter(description = "ID del usuario") @PathVariable Long id,
                        @Parameter(description = "Datos actualizados del usuario") @Valid @RequestBody UserDto userDTO) {

                log.info("PUT /api/users/{} - Updating user", id);

                User user = userDTO.toEntity();
                // Set password hash from password field if provided
                if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                        user.setPasswordHash(userDTO.getPassword());
                }

                return userService.updateUser(id, user)
                                .flatMap(updated -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                                        .map(ctx -> ctx.getAuthentication())
                                        .switchIfEmpty(Mono.empty())
                                        .flatMap(a -> {
                                                Long actorId = null; String actorEmail = null;
                                                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                                                return notificationService.createNotification("users", updated.getId(), "UPDATE", "Usuario actualizado: " + updated.getEmail(), actorId, actorEmail).thenReturn(updated);
                                        })
                                )
                                .map(UserDto::fromEntity)
                                .map(dto -> ApiResponse.success("Usuario actualizado exitosamente", dto));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Eliminar usuario (soft delete)", description = "Elimina lógicamente un usuario del sistema. El usuario puede ser restaurado posteriormente. Solo accesible para administradores.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El usuario ya está eliminado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No tiene permisos de administrador")
        })
        @SecurityRequirement(name = "Bearer Authentication")
        public Mono<org.springframework.http.ResponseEntity<ApiResponse<Void>>> deleteUser(
                        @Parameter(description = "ID del usuario") @PathVariable Long id) {
                log.info("DELETE /api/users/{}", id);
                return userService.deleteUser(id)
                                .then(org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                                        .map(ctx -> ctx.getAuthentication())
                                        .switchIfEmpty(Mono.empty())
                                        .flatMap(a -> {
                                                Long actorId = null; String actorEmail = null;
                                                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                                                return notificationService.createNotification("users", id, "DELETE", "Usuario eliminado: ID " + id, actorId, actorEmail);
                                        })
                                )
                                .then(Mono.just(org.springframework.http.ResponseEntity.ok(
                                                ApiResponse.<Void>success("Usuario eliminado exitosamente", null))));
        }

        @PatchMapping("/{id}/restore")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Restaurar usuario eliminado", description = "Restaura un usuario que fue eliminado lógicamente. Solo accesible para administradores.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario restaurado exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El usuario no está eliminado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No tiene permisos de administrador")
        })
        @SecurityRequirement(name = "Bearer Authentication")
        public Mono<ApiResponse<UserDto>> restoreUser(
                        @Parameter(description = "ID del usuario") @PathVariable Long id) {
                log.info("PATCH /api/users/{}/restore", id);
                return userService.restoreUser(id)
                                .flatMap(restored -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                                        .map(ctx -> ctx.getAuthentication())
                                        .switchIfEmpty(Mono.empty())
                                        .flatMap(a -> {
                                                Long actorId = null; String actorEmail = null;
                                                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                                                return notificationService.createNotification("users", restored.getId(), "RESTORE", "Usuario restaurado: " + restored.getEmail(), actorId, actorEmail).thenReturn(restored);
                                        })
                                )
                                .map(UserDto::fromEntity)
                                .map(dto -> ApiResponse.success("Usuario restaurado exitosamente", dto));
        }

        // ELIMINACIÓN FÍSICA REMOVIDA - Solo se permite eliminación lógica (soft delete)

        @GetMapping("/stats")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
        @Operation(summary = "Obtener estadísticas de usuarios", description = "Retorna estadísticas agregadas de usuarios (totales por rol, estado, ventas). Solo accesible para administradores y supervisores.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No tiene permisos suficientes")
        })
        @SecurityRequirement(name = "Bearer Authentication")
        public Mono<ApiResponse<Map<String, Object>>> getUserStats() {
                log.info("GET /api/users/stats");
                return userService.getUserStats()
                                .map(stats -> ApiResponse.success("Estadísticas obtenidas exitosamente", stats));
        }

        // ========== ENDPOINTS DE TESTING (SIN AUTENTICACIÓN) ==========
        
        @PostMapping("/test-create")
        @Operation(summary = "TESTING: Crear usuario sin autenticación")
        public Mono<org.springframework.http.ResponseEntity<ApiResponse<UserDto>>> testCreateUser(
                        @RequestBody UserDto userDTO) {
                log.info("🧪 TEST: Creating user without auth: {}", userDTO.getEmail());

                User user = userDTO.toEntity();
                if (userDTO.getPassword() != null) {
                        user.setPasswordHash(userDTO.getPassword());
                }

                return userService.createUser(user)
                                .map(UserDto::fromEntity)
                                .map(dto -> ApiResponse.success("Usuario creado exitosamente (TEST)", dto))
                                .map(response -> org.springframework.http.ResponseEntity.status(HttpStatus.CREATED)
                                                .body(response))
                                .doOnSuccess(r -> log.info("✅ TEST: User created successfully"))
                                .doOnError(e -> log.error("❌ TEST: Error creating user", e));
        }

        @PutMapping("/test-update/{id}")
        @Operation(summary = "TESTING: Actualizar usuario sin autenticación")
        public Mono<ApiResponse<UserDto>> testUpdateUser(
                        @PathVariable Long id,
                        @RequestBody UserDto userDTO) {

                log.info("🧪 TEST: Updating user without auth: {}", id);

                User user = userDTO.toEntity();
                if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                        user.setPasswordHash(userDTO.getPassword());
                }

                return userService.updateUser(id, user)
                                .map(UserDto::fromEntity)
                                .map(dto -> ApiResponse.success("Usuario actualizado exitosamente (TEST)", dto))
                                .doOnSuccess(r -> log.info("✅ TEST: User updated successfully"))
                                .doOnError(e -> log.error("❌ TEST: Error updating user", e));
        }

        @DeleteMapping("/test-delete/{id}")
        @Operation(summary = "TESTING: Eliminar usuario sin autenticación")
        public Mono<org.springframework.http.ResponseEntity<ApiResponse<Void>>> testDeleteUser(
                        @PathVariable Long id) {
                log.info("🧪 TEST: Deleting user without auth: {}", id);
                return userService.deleteUser(id)
                                .then(Mono.just(org.springframework.http.ResponseEntity.ok(
                                                ApiResponse.<Void>success("Usuario eliminado exitosamente (TEST)", null))))
                                .doOnSuccess(r -> log.info("✅ TEST: User deleted successfully"))
                                .doOnError(e -> log.error("❌ TEST: Error deleting user", e));
        }
}
