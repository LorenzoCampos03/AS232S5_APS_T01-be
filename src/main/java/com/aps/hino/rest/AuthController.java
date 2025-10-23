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

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email y contraseña, retorna un token JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticación exitosa", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("POST /api/auth/login - User: {}", loginRequest.getEmail());

        return authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword())
                .map(loginResponse -> {
                    ApiResponse<LoginResponse> response = ApiResponse.success(
                            "Autenticación exitosa",
                            loginResponse);
                    return ResponseEntity.ok(response);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Authentication failed for user: {}", loginRequest.getEmail());
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
                    .then(Mono.just(ResponseEntity.ok(
                            ApiResponse.<Void>success("Sesión cerrada exitosamente", null))));
        }

        return Mono.just(ResponseEntity.ok(
                ApiResponse.<Void>success("Sesión cerrada exitosamente", null)));
    }
}
