package com.aps.hino.rest;

import com.aps.hino.model.Quote;
import com.aps.hino.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Quotes", description = "Gestión de cotizaciones")
public class QuoteController {

    private final QuoteService service;

    @GetMapping
    @Operation(summary = "Obtener todas las cotizaciones")
    public Flux<Quote> getAllQuotes() {
        return service.getAllQuotes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cotización por ID")
    public Mono<ResponseEntity<Quote>> getQuoteById(@PathVariable Long id) {
        return service.getQuoteById(id)
                .map(quote -> ResponseEntity.ok(quote))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva cotización")
    public Mono<ResponseEntity<Quote>> createQuote(@RequestBody Quote quote) {
        return service.saveQuote(quote)
                .map(savedQuote -> ResponseEntity.status(HttpStatus.CREATED).body(savedQuote));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cotización existente")
    public Mono<ResponseEntity<Quote>> updateQuote(@PathVariable Long id, @RequestBody Quote quote) {
        return service.getQuoteById(id)
                .flatMap(existingQuote -> {
                    quote.setId(id);
                    return service.saveQuote(quote);
                })
                .map(updatedQuote -> ResponseEntity.ok(updatedQuote))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cotización por ID")
    public Mono<ResponseEntity<Void>> deleteQuote(@PathVariable Long id) {
        return service.getQuoteById(id)
                .flatMap(existingQuote ->
                        service.deleteQuote(id)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
