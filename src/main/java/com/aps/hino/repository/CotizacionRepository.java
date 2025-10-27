package com.aps.hino.repository;

import com.aps.hino.model.Cotizacion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CotizacionRepository extends ReactiveCrudRepository<Cotizacion, Integer> {
}
