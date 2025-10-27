package com.aps.hino.dto;

import com.aps.hino.model.Cotizacion;
import com.aps.hino.model.CotizacionDetalle;

import java.math.BigDecimal;
import java.util.List;

public class CotizacionDetailResponse {
    private Cotizacion cabecera;
    private List<CotizacionDetalle> items;
    private BigDecimal totalItems;

    public Cotizacion getCabecera() { return cabecera; }
    public void setCabecera(Cotizacion cabecera) { this.cabecera = cabecera; }

    public List<CotizacionDetalle> getItems() { return items; }
    public void setItems(List<CotizacionDetalle> items) { this.items = items; }

    public BigDecimal getTotalItems() { return totalItems; }
    public void setTotalItems(BigDecimal totalItems) { this.totalItems = totalItems; }
}
