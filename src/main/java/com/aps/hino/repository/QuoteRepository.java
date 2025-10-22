package com.aps.hino.repository;

import com.aps.hino.model.Quote;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends ReactiveCrudRepository<Quote, Long> {
}
