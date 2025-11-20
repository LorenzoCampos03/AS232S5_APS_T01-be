package com.aps.hino.rest;

import com.aps.hino.dto.ContactMessageRequest;
import com.aps.hino.dto.ContactMessageResponse;
import com.aps.hino.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/public/contact")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements() // Removes the lock icon in Swagger
    public Mono<ContactMessageResponse> createMessage(@Valid @RequestBody ContactMessageRequest request) {
        return contactService.createMessage(request);
    }

    @GetMapping("/contact")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR', 'SUPERVISOR', 'MECANICO')")
    public Flux<ContactMessageResponse> getAllMessages(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("");
        return contactService.getAllMessages(role);
    }

    @GetMapping("/contact/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR', 'SUPERVISOR', 'MECANICO')")
    public Mono<ContactMessageResponse> getMessageById(@PathVariable Long id) {
        return contactService.getMessageById(id);
    }

    @PatchMapping("/contact/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR', 'SUPERVISOR', 'MECANICO')")
    public Mono<ContactMessageResponse> markAsAttended(@PathVariable Long id) {
        return contactService.markAsAttended(id);
    }
}
