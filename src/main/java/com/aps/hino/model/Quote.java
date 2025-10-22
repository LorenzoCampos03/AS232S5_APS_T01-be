package com.aps.hino.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
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

    @Column("estado")
    private String estado = "pendiente";

    @Column("prioridad")
    private String prioridad = "media";

    @Column("asesor_asignado_id")
    private Integer asesorAsignadoId;

    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
