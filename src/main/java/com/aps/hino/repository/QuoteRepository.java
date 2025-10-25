package com.aps.hino.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.aps.hino.model.Quote;

@Repository
public interface QuoteRepository extends ReactiveCrudRepository<Quote, Long> {
    // Aquí puedes agregar consultas personalizadas luego si quieres
}
