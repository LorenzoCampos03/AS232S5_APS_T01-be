package com.aps.hino.service;

import com.aps.hino.model.Quote;
import com.aps.hino.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;

    public QuoteService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public Flux<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    public Mono<Quote> getQuoteById(Long id) {
        return quoteRepository.findById(id);
    }

    public Mono<Quote> saveQuote(Quote quote) {
        return quoteRepository.save(quote);
    }

    public Mono<Void> deleteQuote(Long id) {
        return quoteRepository.deleteById(id);
    }
}
