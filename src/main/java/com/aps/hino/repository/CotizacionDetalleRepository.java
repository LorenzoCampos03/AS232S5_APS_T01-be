package com.aps.hino.repository;

import com.aps.hino.model.CotizacionDetalle;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CotizacionDetalleRepository extends ReactiveCrudRepository<CotizacionDetalle, Integer> {
    Flux<CotizacionDetalle> findByCotizacionId(Integer cotizacionId);
}
