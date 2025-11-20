package com.aps.hino.model;

import com.aps.hino.model.enums.ContactStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("contact_messages")
public class ContactMessage {
    @Id
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;

    private ContactStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
