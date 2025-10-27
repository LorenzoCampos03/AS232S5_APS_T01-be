package com.aps.hino.dto;

import java.util.List;

public class CotizacionCreateFullRequest {
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String empresa;
    private Integer asesorAsignadoId;
    private String mensaje; // opcional, no mapeado a tabla cabecera por ahora

    private List<CotizacionItemRequest> items;

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public Integer getAsesorAsignadoId() { return asesorAsignadoId; }
    public void setAsesorAsignadoId(Integer asesorAsignadoId) { this.asesorAsignadoId = asesorAsignadoId; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public List<CotizacionItemRequest> getItems() { return items; }
    public void setItems(List<CotizacionItemRequest> items) { this.items = items; }
}
