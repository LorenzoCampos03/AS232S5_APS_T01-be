package com.aps.hino.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("cotizacion_detalle")
public class CotizacionDetalle {
    @Id
    private Integer id;

    @Column("cotizacion_id")
    private Integer cotizacionId;

    @Column("vehiculo_id")
    private Integer vehiculoId;

    @Column("cantidad")
    private Integer cantidad;

    @Column("precio_unitario")
    private java.math.BigDecimal precioUnitario;

    @Column("notas")
    private String notas;

    @Column("subtotal")
    private java.math.BigDecimal subtotal; // generado por la BD

    @Column("created_at")
    private OffsetDateTime createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCotizacionId() { return cotizacionId; }
    public void setCotizacionId(Integer cotizacionId) { this.cotizacionId = cotizacionId; }

    public Integer getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Integer vehiculoId) { this.vehiculoId = vehiculoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public java.math.BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(java.math.BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public java.math.BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(java.math.BigDecimal subtotal) { this.subtotal = subtotal; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
