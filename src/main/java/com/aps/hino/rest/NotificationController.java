package com.aps.hino.rest;

import com.aps.hino.dto.NotificationDto;
import com.aps.hino.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Flux<NotificationDto> list() {
        return notificationService.listAll();
    }

    @GetMapping("/count-unread")
    public Mono<Long> countUnread() {
        return notificationService.countUnread();
    }

    @PostMapping("/{id}/read")
    public Mono<ResponseEntity<Void>> markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    /**
     * SSE stream endpoint that emits new notifications in real-time.
     * For simplicity in development the client may pass token as query param: /api/notifications/stream?token=xxx
     * In production prefer cookie-based auth or a proxy that sets Authorization header.
     */
    @GetMapping(path = "/stream", produces = "text/event-stream")
    public Flux<org.springframework.http.codec.ServerSentEvent<NotificationDto>> stream(@RequestParam(value = "token", required = false) String token,
                                               org.springframework.http.server.reactive.ServerHttpRequest request) {

    // Note: JwtAuthenticationFilter handles Authorization header normally. If client passed token as query param,
    // we won't validate it here; for production validate token before allowing stream.

    return notificationService.stream()
        .map(n -> org.springframework.http.codec.ServerSentEvent.<NotificationDto>builder()
            .id(String.valueOf(n.getId()))
            .event("notification")
            .data(n)
            .build());
    }
}

