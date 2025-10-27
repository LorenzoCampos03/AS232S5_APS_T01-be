package com.aps.hino.dto;

import java.math.BigDecimal;

public class CotizacionItemRequest {
    private Integer vehiculoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private String notas;

    public Integer getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Integer vehiculoId) { this.vehiculoId = vehiculoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
