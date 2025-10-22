package com.aps.hino.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    private Long id;
    
    private String nombre;
    
    private String email;
    
    private String telefono;
    
    private String rol;
    
    private String especialidad;
    
    private String estado;
    
    private Integer ventas;
    
    @Column("fecha_ingreso")
    private LocalDate fechaIngreso;
    
    @Column("avatar_url")
    private String avatarUrl;
    
    @Column("password_hash")
    private String passwordHash;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
