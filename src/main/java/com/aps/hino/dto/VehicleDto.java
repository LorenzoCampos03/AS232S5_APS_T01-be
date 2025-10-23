package com.aps.hino.dto;

import com.aps.hino.model.Vehicle;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDto {
    private Integer id;

    @NotBlank(message = "El modelo es requerido")
    private String modelo;

    @NotBlank(message = "El tipo es requerido")
    @Pattern(regexp = "^(camion|bus)$", message = "El tipo debe ser 'camion' o 'bus'")
    private String tipo;

    @NotBlank(message = "La categoría es requerida")
    private String categoria;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;

    @NotBlank(message = "La capacidad es requerida")
    private String capacidad;

    @NotBlank(message = "El motor es requerido")
    private String motor;

    @NotNull(message = "El año es requerido")
    @Min(value = 2000, message = "El año mínimo es 2000")
    private Integer anio;

    @NotBlank(message = "El estado es requerido")
    @Pattern(regexp = "^(disponible|reservado|vendido)$", message = "El estado debe ser 'disponible', 'reservado' o 'vendido'")
    private String estado;

    @NotNull(message = "El stock es requerido")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private String imagenUrl;

    @NotBlank(message = "La descripción es requerida")
    private String descripcion;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VehicleDto fromEntity(Vehicle v) {
        VehicleDto dto = new VehicleDto();
        dto.setId(v.getId());
        dto.setModelo(v.getModelo());
        dto.setTipo(v.getTipo());
        dto.setCategoria(v.getCategoria());
        dto.setPrecio(v.getPrecio());
        dto.setCapacidad(v.getCapacidad());
        dto.setMotor(v.getMotor());
        dto.setAnio(v.getAnio());
        dto.setEstado(v.getEstado());
        dto.setStock(v.getStock());
        dto.setImagenUrl(v.getImagenUrl());
        dto.setDescripcion(v.getDescripcion());
        dto.setCreatedAt(v.getCreatedAt());
        dto.setUpdatedAt(v.getUpdatedAt());
        return dto;
    }

    public Vehicle toEntity() {
        Vehicle v = new Vehicle();
        v.setId(this.id);
        v.setModelo(this.modelo);
        v.setTipo(this.tipo);
        v.setCategoria(this.categoria);
        v.setPrecio(this.precio);
        v.setCapacidad(this.capacidad);
        v.setMotor(this.motor);
        v.setAnio(this.anio);
        v.setEstado(this.estado);
        v.setStock(this.stock);
        v.setImagenUrl(this.imagenUrl);
        v.setDescripcion(this.descripcion);
        // createdAt/updatedAt se gestionan en el servicio
        return v;
    }
}
