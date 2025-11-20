package com.aps.hino.service;

import com.aps.hino.dto.ContactMessageRequest;
import com.aps.hino.dto.ContactMessageResponse;
import com.aps.hino.exception.ResourceNotFoundException;
import com.aps.hino.model.ContactMessage;
import com.aps.hino.model.enums.ContactStatus;
import com.aps.hino.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public Mono<ContactMessageResponse> createMessage(ContactMessageRequest request) {
        ContactMessage message = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(ContactStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return contactMessageRepository.save(message)
                .map(ContactMessageResponse::fromEntity);
    }

    public Flux<ContactMessageResponse> getAllMessages(String role) {
        Flux<ContactMessage> messages;

        if ("admin".equalsIgnoreCase(role) || "supervisor".equalsIgnoreCase(role)) {
            messages = contactMessageRepository.findAll();
        } else if ("asesor".equalsIgnoreCase(role)) {
            messages = contactMessageRepository.findAll() // In a real app, use a custom query for better performance
                    .filter(m -> "cotizacion".equalsIgnoreCase(m.getSubject())
                            || "ventas".equalsIgnoreCase(m.getSubject())
                            || "otro".equalsIgnoreCase(m.getSubject())
                            || "reclamo".equalsIgnoreCase(m.getSubject()));
        } else if ("mecanico".equalsIgnoreCase(role)) {
            messages = contactMessageRepository.findAll()
                    .filter(m -> "soporte".equalsIgnoreCase(m.getSubject())
                            || "repuestos".equalsIgnoreCase(m.getSubject()));
        } else {
            messages = Flux.empty();
        }

        return messages.map(ContactMessageResponse::fromEntity);
    }

    public Mono<ContactMessageResponse> markAsAttended(Long id) {
        return contactMessageRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Message not found with id: " + id)))
                .flatMap(message -> {
                    message.setStatus(ContactStatus.ATTENDED);
                    message.setUpdatedAt(LocalDateTime.now());
                    return contactMessageRepository.save(message);
                })
                .map(ContactMessageResponse::fromEntity);
    }

    public Mono<ContactMessageResponse> getMessageById(Long id) {
        return contactMessageRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Message not found with id: " + id)))
                .map(ContactMessageResponse::fromEntity);
    }
}
