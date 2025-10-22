package com.aps.hino.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data // genera getters y setters automáticamente
@Table("users")
public class User {

    @Id
    private Integer id;

    private String nombre;
    private String email;
    private String telefono;
    private String rol;
    private String especialidad;
    private String estado;
    private Integer ventas;
    private LocalDateTime fechaIngreso;
    private String avatarUrl;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
