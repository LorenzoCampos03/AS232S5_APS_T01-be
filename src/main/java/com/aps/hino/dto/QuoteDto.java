package com.aps.hino.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteDto {

    private Long id;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String empresa;
    private String vehicleType;
    private String mensaje;
    private String estado;
    private String prioridad;
    private Integer assignedAdvisorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
