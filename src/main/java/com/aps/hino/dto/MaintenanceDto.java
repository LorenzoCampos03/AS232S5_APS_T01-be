package com.aps.hino.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceDto {
    private Integer id;
    
    @NotNull(message = "Vehicle ID is required")
    private Integer vehicleId;
    
    private Integer userId;
    
    @NotBlank(message = "Type is required")
    @Pattern(regexp = "preventivo|correctivo", message = "Type must be 'preventivo' or 'correctivo'")
    private String tipo;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String descripcion;
    
    @NotNull(message = "Scheduled date is required")
    @FutureOrPresent(message = "Scheduled date must be today or in the future")
    private LocalDate fechaProgramada;
    
    private LocalDate fechaRealizada;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Cost cannot exceed 999999.99")
    private BigDecimal costo;
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "pendiente|en-proceso|completado|cancelado", message = "Status must be one of: pendiente, en-proceso, completado, cancelado")
    private String estado;
    
    @Size(max = 500, message = "Observations cannot exceed 500 characters")
    private String observaciones;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
