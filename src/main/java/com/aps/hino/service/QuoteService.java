package com.aps.hino.service;

import org.springframework.stereotype.Service;
import com.aps.hino.model.Quote;
import com.aps.hino.model.QuoteStatus;
import com.aps.hino.model.QuotePriority;
import com.aps.hino.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;

    // 🔹 Listar solo los que están activos
    public Flux<Quote> getAllQuotes() {
        return quoteRepository.findAll()
                .filter(quote -> "activo".equalsIgnoreCase(quote.getStatus()));
    }

    // 🔹 Buscar una cotización por ID
    public Mono<Quote> getQuoteById(Long id) {
        return quoteRepository.findById(id);
    }

    // 🔹 Crear una nueva cotización
    public Mono<Quote> createQuote(Quote quote) {
        if (quote.getEstado() == null) {
            quote.setEstado(QuoteStatus.pendiente);
        }
        if (quote.getPrioridad() == null) {
            quote.setPrioridad(QuotePriority.media);
        }
        if (quote.getStatus() == null || quote.getStatus().isBlank()) {
            quote.setStatus("activo");
        }
        return quoteRepository.save(quote);
    }

    // 🔹 Actualizar cotización existente
    public Mono<Quote> updateQuote(Long id, Quote quote) {
        return quoteRepository.findById(id)
                .flatMap(existing -> {
                    existing.setClienteNombre(quote.getClienteNombre());
                    existing.setClienteEmail(quote.getClienteEmail());
                    existing.setClienteTelefono(quote.getClienteTelefono());
                    existing.setEmpresa(quote.getEmpresa());
                    existing.setTipoVehiculo(quote.getTipoVehiculo());
                    existing.setMensaje(quote.getMensaje());
                    existing.setEstado(quote.getEstado());
                    existing.setPrioridad(quote.getPrioridad());
                    existing.setAsesorAsignadoId(quote.getAsesorAsignadoId());
                    existing.setUpdatedAt(quote.getUpdatedAt());
                    // Mantiene su estado (activo o inactivo)
                    return quoteRepository.save(existing);
                });
    }

    // 🔹 Eliminación lógica (status = inactivo)
    public Mono<Void> deleteQuote(Long id) {
        return quoteRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus("inactivo");
                    return quoteRepository.save(existing);
                })
                .then();
    }

    // 🔹 Restaurar cotización (status = activo)
    public Mono<Quote> restoreQuote(Long id) {
        return quoteRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus("activo");
                    return quoteRepository.save(existing);
                });
    }
}
