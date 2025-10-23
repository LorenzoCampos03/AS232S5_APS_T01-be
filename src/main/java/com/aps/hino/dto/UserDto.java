package com.aps.hino.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.aps.hino.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    private String telefono;

    @NotBlank(message = "El rol es requerido")
    @Pattern(regexp = "^(admin|asesor|mecanico|supervisor)$", message = "El rol debe ser 'admin', 'asesor', 'mecanico' o 'supervisor'")
    private String rol;

    private String especialidad;

    @Pattern(regexp = "activo|inactivo", message = "El estado debe ser 'activo' o 'inactivo'")
    private String estado;

    private Integer ventas;

    private LocalDate fechaIngreso;

    private String avatarUrl;

    // Password is only used for creation/update, never returned
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Mapper methods
    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setNombre(user.getNombre());
        dto.setEmail(user.getEmail());
        dto.setTelefono(user.getTelefono());
        dto.setRol(user.getRol());
        dto.setEspecialidad(user.getEspecialidad());
        dto.setEstado(user.getEstado());
        dto.setVentas(user.getVentas());
        dto.setFechaIngreso(user.getFechaIngreso());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        // Never include password hash in DTO
        return dto;
    }

    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setNombre(this.nombre);
        user.setEmail(this.email);
        user.setTelefono(this.telefono);
        user.setRol(this.rol);
        user.setEspecialidad(this.especialidad);
        user.setEstado(this.estado != null ? this.estado : "activo");
        user.setVentas(this.ventas);
        user.setFechaIngreso(this.fechaIngreso);
        user.setAvatarUrl(this.avatarUrl);
        // createdAt y updatedAt se manejan en el servicio
        // Password hash is set separately in service layer
        return user;
    }
}
