package com.aps.hino.service;

import com.aps.hino.model.Notification;
import com.aps.hino.dto.NotificationDto;
import com.aps.hino.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Sink para emitir notificaciones SSE en tiempo real
    private final Sinks.Many<NotificationDto> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Crear una notificación, sin bloquear el flujo principal.
     */
    public Mono<Notification> createNotification(String entity, Long entityId, String action,
                                                 String description, Long actorId, String actorEmail) {
        log.info("🔔 Creando notificación: entity={}, entityId={}, action={}, description={}, actorId={}, actorEmail={}",
                entity, entityId, action, description, actorId, actorEmail);

        Notification n = new Notification();
        n.setEntity(entity);
        n.setEntityId(entityId);
        n.setAction(action);
        n.setDescription(description);
        n.setActorId(actorId);
        n.setActorEmail(actorEmail);
        n.setRead(false);
        n.setCreatedAt(OffsetDateTime.now());

        // Intentar guardar, pero si falla, registrar el error sin interrumpir el flujo
        return notificationRepository.save(n)
                .doOnSuccess(saved -> {
                    log.info("✅ Notificación guardada con ID: {}", saved.getId());
                    try {
                        notificationSink.tryEmitNext(NotificationDto.from(saved));
                    } catch (Exception e) {
                        log.warn("⚠️ Error al emitir SSE: {}", e.getMessage());
                    }
                })
                .onErrorResume(e -> {
                    log.error("❌ Error al guardar notificación: {}", e.getMessage());
                    return Mono.just(n); // Continuar sin detener el flujo
                });
    }

    public Flux<NotificationDto> listAll() {
        return notificationRepository.findAllOrderByCreatedAtDesc()
                .map(NotificationDto::from);
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

    // Solo para pruebas
    public NotificationRepository getNotificationRepository() {
        return notificationRepository;
    }
}
