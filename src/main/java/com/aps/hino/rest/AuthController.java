package com.aps.hino.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.aps.hino.dto.ApiResponse;
import com.aps.hino.dto.LoginRequest;
import com.aps.hino.dto.LoginResponse;
import com.aps.hino.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
@io.swagger.v3.oas.annotations.security.SecurityRequirements() // No requiere autenticación
public class AuthController {

    private final AuthService authService;
    private final com.aps.hino.service.NotificationService notificationService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email y contraseña, retorna un token JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticación exitosa", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("POST /api/auth/login - User: {}", loginRequest.getEmail());

        return authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword())
                .flatMap(loginResponse -> {
                    // Registrar notificación de login exitoso
                    Long userId = loginResponse.getUser() != null ? loginResponse.getUser().getId() : null;
                    String userEmail = loginResponse.getUser() != null ? loginResponse.getUser().getEmail() : loginRequest.getEmail();
                    log.info("🔐 User logged in successfully: userId={}, email={}", userId, userEmail);
                    return notificationService.createNotification("auth", userId, "LOGIN", "Usuario inició sesión: " + userEmail, userId, userEmail)
                            .thenReturn(loginResponse);
                })
                .map(loginResponse -> {
                    ApiResponse<LoginResponse> response = ApiResponse.success(
                            "Autenticación exitosa",
                            loginResponse);
                    return ResponseEntity.ok(response);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Authentication failed for user: {}", loginRequest.getEmail());
                    // Registrar intento de login fallido
                    notificationService.createNotification("auth", null, "LOGIN_FAILED", "Intento de login fallido: " + loginRequest.getEmail(), null, loginRequest.getEmail())
                            .subscribe(); // Fire and forget
                    ApiResponse<LoginResponse> response = ApiResponse.error("Credenciales incorrectas");
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
                }));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el token JWT actual del usuario. El cliente debe eliminar el token almacenado.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token inválido o no proporcionado")
    })
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("POST /api/auth/logout");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return authService.logout(token)
                    .then(org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                            .map(ctx -> ctx.getAuthentication())
                            .defaultIfEmpty(null)
                            .flatMap(a -> {
                                Long actorId = null; String actorEmail = null;
                                if (a != null) { try { actorEmail = (String) a.getPrincipal(); Object det = a.getDetails(); if (det instanceof Long) actorId = (Long) det; } catch (Exception ignored) {} }
                                return notificationService.createNotification("auth", actorId, "LOGOUT", "Usuario cerró sesión: " + (actorEmail != null ? actorEmail : "desconocido"), actorId, actorEmail);
                            })
                    )
                    .then(Mono.just(ResponseEntity.ok(
                            ApiResponse.<Void>success("Sesión cerrada exitosamente", null))));
        }

        return Mono.just(ResponseEntity.ok(
                ApiResponse.<Void>success("Sesión cerrada exitosamente", null)));
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario actual", description = "Retorna información del usuario autenticado")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    public Mono<ResponseEntity<ApiResponse<Object>>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autenticado")));
        }

        Object details = authentication.getDetails();
        Long userId = null;
        if (details instanceof Long) {
            userId = (Long) details;
        } else if (details instanceof Integer) {
            userId = ((Integer) details).longValue();
        }

        String email = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        var payload = new java.util.HashMap<String, Object>();
        payload.put("id", userId);
        payload.put("email", email);
        payload.put("roles", roles);

        return Mono.just(ResponseEntity.ok(ApiResponse.success("OK", payload)));
    }
}
