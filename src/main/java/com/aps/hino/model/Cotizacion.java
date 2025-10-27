package com.aps.hino.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("cotizaciones")
public class Cotizacion {
    @Id
    private Integer id;

    @Column("cliente_nombre")
    private String clienteNombre;

    @Column("cliente_email")
    private String clienteEmail;

    @Column("cliente_telefono")
    private String clienteTelefono;

    @Column("empresa")
    private String empresa;

    @Column("estado")
    private String estado; // pendiente | en-proceso | enviada | cerrada

    @Column("prioridad")
    private String prioridad; // baja | media | alta

    @Column("status")
    private String status; // activo | inactivo

    @Column("asesor_asignado_id")
    private Integer asesorAsignadoId;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAsesorAsignadoId() { return asesorAsignadoId; }
    public void setAsesorAsignadoId(Integer asesorAsignadoId) { this.asesorAsignadoId = asesorAsignadoId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
