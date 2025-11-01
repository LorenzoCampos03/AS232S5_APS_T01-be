package com.aps.hino.repository;

import com.aps.hino.model.Notification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRepository extends R2dbcRepository<Notification, Long> {

    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    Flux<Notification> findAllOrderByCreatedAtDesc();
}

