package com.aps.hino.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("quotes")
public class Quote {

    @Id
    private Long id;

    @Column("cliente_nombre")
    private String clienteNombre;

    @Column("cliente_email")
    private String clienteEmail;

    @Column("cliente_telefono")
    private String clienteTelefono;

    @Column("empresa")
    private String empresa;

    @Column("tipo_vehiculo")
    private String tipoVehiculo;

    @Column("mensaje")
    private String mensaje;

    // Enum quote_status en la BD
    @Column("estado")
    private QuoteStatus estado;

    // Enum quote_priority en la BD
    @Column("prioridad")
    private QuotePriority prioridad;

    @Column("asesor_asignado_id")
    private Integer asesorAsignadoId;

    // NUEVO: campo status adicional
    @Column("status")
    private String status;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
