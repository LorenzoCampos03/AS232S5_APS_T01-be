package com.aps.hino.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("maintenance")
public class Maintenance {
    @Id
    private Integer id;
    
    @Column("vehicle_id")
    private Integer vehicleId;
    
    @Column("user_id")
    private Integer userId;
    
    private String tipo;
    private String descripcion;
    
    @Column("fecha_programada")
    private LocalDate fechaProgramada;
    
    @Column("fecha_realizada")
    private LocalDate fechaRealizada;
    
    private BigDecimal costo;
    private String estado;
    private String observaciones;
    
    @Column("created_at")
    private OffsetDateTime createdAt;
    
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
