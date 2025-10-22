package com.aps.hino.rest;

import org.springframework.web.bind.annotation.*;
import com.aps.hino.model.Quote;
import com.aps.hino.service.QuoteService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    // 🔹 Listar solo cotizaciones activas
    @GetMapping
    public Flux<Quote> getAllQuotes() {
        return quoteService.getAllQuotes();
    }

    // 🔹 Obtener una cotización por ID
    @GetMapping("/{id}")
    public Mono<Quote> getQuoteById(@PathVariable Long id) {
        return quoteService.getQuoteById(id);
    }

    // 🔹 Crear una nueva cotización
    @PostMapping
    public Mono<Quote> createQuote(@RequestBody Quote quote) {
        return quoteService.createQuote(quote);
    }

    // 🔹 Actualizar una cotización existente
    @PutMapping("/{id}")
    public Mono<Quote> updateQuote(@PathVariable Long id, @RequestBody Quote quote) {
        return quoteService.updateQuote(id, quote);
    }

    // 🔹 Eliminación lógica (status = "inactivo")
    @DeleteMapping("/{id}")
    public Mono<Void> deleteQuote(@PathVariable Long id) {
        return quoteService.deleteQuote(id);
    }

    // 🔹 Restaurar una cotización eliminada (status = "activo")
    @PutMapping("/restore/{id}")
    public Mono<Quote> restoreQuote(@PathVariable Long id) {
        return quoteService.restoreQuote(id);
    }

    // 🔹 (Opcional) Listar todas las inactivas
    @GetMapping("/inactivas")
    public Flux<Quote> getInactiveQuotes() {
        return quoteService.getAllQuotes()
                .filter(quote -> "inactivo".equalsIgnoreCase(quote.getStatus()));
    }
}
