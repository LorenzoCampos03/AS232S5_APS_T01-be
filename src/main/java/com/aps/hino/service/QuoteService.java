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

    // Devuelve todos los registros como Flux<Quote>
    public Flux<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    // Devuelve un registro como Mono<Quote>
    public Mono<Quote> getQuoteById(Long id) {
        return quoteRepository.findById(id);
    }

    // Guardar o actualizar una cotización
    public Mono<Quote> saveQuote(Quote quote) {
        return quoteRepository.save(quote);
    }

    // Eliminar una cotización
    public Mono<Void> deleteQuote(Long id) {
        return quoteRepository.deleteById(id);
    }
}
