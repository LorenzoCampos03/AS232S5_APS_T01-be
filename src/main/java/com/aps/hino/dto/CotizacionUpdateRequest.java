package com.aps.hino.dto;

public class CotizacionUpdateRequest {
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String empresa;
    private String estado;     // opcional
    private String prioridad;  // opcional
    private Integer asesorAsignadoId;

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

    public Integer getAsesorAsignadoId() { return asesorAsignadoId; }
    public void setAsesorAsignadoId(Integer asesorAsignadoId) { this.asesorAsignadoId = asesorAsignadoId; }
}
