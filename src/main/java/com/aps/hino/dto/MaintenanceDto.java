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
    @Pattern(regexp = "(?i)preventivo|correctivo|PREVENTIVO|CORRECTIVO", message = "Type must be 'preventivo' or 'correctivo'")
    private String tipo;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String descripcion;
    
    private LocalDate fechaProgramada;
    
    private LocalDate fechaRealizada;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be 0 or greater")
    @DecimalMax(value = "999999.99", message = "Cost cannot exceed 999999.99")
    private BigDecimal costo;
    
    @Pattern(regexp = "(?i)pendiente|en-proceso|completado|cancelado|PENDIENTE|EN-PROCESO|COMPLETADO|CANCELADO", message = "Status must be one of: pendiente, en-proceso, completado, cancelado")
    private String estado;
    
    @Size(max = 500, message = "Observations cannot exceed 500 characters")
    private String observaciones;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
