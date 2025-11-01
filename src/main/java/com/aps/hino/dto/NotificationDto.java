package com.aps.hino.dto;

import com.aps.hino.model.Notification;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String entity;
    private Long entityId;
    private String action;
    private String description;
    private Long actorId;
    private String actorEmail;
    private Boolean read;
    private OffsetDateTime createdAt;

    public static NotificationDto from(Notification n) {
        NotificationDto d = new NotificationDto();
        d.setId(n.getId());
        d.setEntity(n.getEntity());
        d.setEntityId(n.getEntityId());
        d.setAction(n.getAction());
        d.setDescription(n.getDescription());
        d.setActorId(n.getActorId());
        d.setActorEmail(n.getActorEmail());
        d.setRead(n.getRead());
        d.setCreatedAt(n.getCreatedAt());
        return d;
    }
}

