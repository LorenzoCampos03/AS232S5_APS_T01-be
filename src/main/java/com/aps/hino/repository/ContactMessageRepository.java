package com.aps.hino.repository;

import com.aps.hino.model.ContactMessage;
import com.aps.hino.model.enums.ContactStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ContactMessageRepository extends R2dbcRepository<ContactMessage, Long> {
    Flux<ContactMessage> findByStatus(ContactStatus status);
}
