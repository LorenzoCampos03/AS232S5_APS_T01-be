package com.aps.hino.service;

import com.aps.hino.model.Notification;
import com.aps.hino.dto.NotificationDto;
import com.aps.hino.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Multicast sink for server-sent events (real-time notifications)
    private final Sinks.Many<NotificationDto> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    public Mono<Notification> createNotification(String entity, Long entityId, String action, String description, Long actorId, String actorEmail) {
        Notification n = new Notification();
        n.setEntity(entity);
        n.setEntityId(entityId);
        n.setAction(action);
        n.setDescription(description);
        n.setActorId(actorId);
        n.setActorEmail(actorEmail);
        n.setRead(false);
        n.setCreatedAt(OffsetDateTime.now());

        return notificationRepository.save(n)
                .flatMap(saved -> {
                    NotificationDto dto = NotificationDto.from(saved);
                    // emit to sink (best-effort)
                    try { notificationSink.tryEmitNext(dto); } catch (Exception ignored) {}
                    return Mono.just(saved);
                });
    }

    public Flux<NotificationDto> listAll() {
        return notificationRepository.findAllOrderByCreatedAtDesc().map(NotificationDto::from);
    }

    public Flux<NotificationDto> stream() {
        return notificationSink.asFlux();
    }

    public Mono<Long> countUnread() {
        return notificationRepository.findAll()
                .filter(n -> n.getRead() == null || !n.getRead())
                .count();
    }

    public Mono<Void> markAsRead(Long id) {
        return notificationRepository.findById(id)
                .flatMap(n -> {
                    n.setRead(true);
                    return notificationRepository.save(n);
                }).then();
    }
}

