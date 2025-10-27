package com.aps.hino.service;

import com.aps.hino.dto.CotizacionCreateFullRequest;
import com.aps.hino.dto.CotizacionDetailResponse;
import com.aps.hino.dto.CotizacionItemRequest;
import com.aps.hino.dto.CotizacionUpdateRequest;
import com.aps.hino.model.Cotizacion;
import com.aps.hino.model.CotizacionDetalle;
import com.aps.hino.repository.CotizacionDetalleRepository;
import com.aps.hino.repository.CotizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final CotizacionDetalleRepository detalleRepository;

    public CotizacionService(CotizacionRepository cotizacionRepository,
                             CotizacionDetalleRepository detalleRepository) {
        this.cotizacionRepository = cotizacionRepository;
        this.detalleRepository = detalleRepository;
    }

    public Flux<Cotizacion> findAll() {
        return cotizacionRepository.findAll();
    }

    public Mono<Cotizacion> findById(Integer id) {
        return cotizacionRepository.findById(id);
    }

    public Flux<CotizacionDetalle> findDetalle(Integer cotizacionId) {
        return detalleRepository.findByCotizacionId(cotizacionId);
    }

    @Transactional
    public Mono<CotizacionDetailResponse> createFull(CotizacionCreateFullRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Debe incluir al menos un ítem en items"));
        }

        Cotizacion c = new Cotizacion();
        c.setClienteNombre(req.getClienteNombre());
        c.setClienteEmail(req.getClienteEmail());
        c.setClienteTelefono(req.getClienteTelefono());
        c.setEmpresa(req.getEmpresa());
        c.setAsesorAsignadoId(req.getAsesorAsignadoId());
        // Dejar defaults de BD para estado/prioridad/status
        c.setEstado(null);
        c.setPrioridad(null);
        c.setStatus(null);
        // Dejar created_at/updated_at a la BD; si quieres registrar aquí, comenta las líneas
        c.setCreatedAt(null);
        c.setUpdatedAt(null);

        return cotizacionRepository.save(c)
                .flatMap(saved -> {
                    Integer cotizacionId = saved.getId();

                    Flux<CotizacionDetalle> guardarDetalles = Flux.fromIterable(req.getItems())
                            .map(item -> toDetalleEntity(cotizacionId, item))
                            .flatMap(detalleRepository::save);

                    return guardarDetalles.collectList()
                            .flatMap(savedItems -> calcularTotalDesdeDetalles(savedItems)
                                    .map(total -> {
                                        CotizacionDetailResponse resp = new CotizacionDetailResponse();
                                        resp.setCabecera(saved);
                                        resp.setItems(savedItems);
                                        resp.setTotalItems(total);
                                        return resp;
                                    }));
                });
    }

    public Mono<CotizacionDetailResponse> getDetail(Integer id) {
        return cotizacionRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cotización no encontrada")))
                .flatMap(cab -> detalleRepository.findByCotizacionId(id).collectList()
                        .flatMap(list -> calcularTotalDesdeDetalles(list)
                                .map(total -> {
                                    CotizacionDetailResponse resp = new CotizacionDetailResponse();
                                    resp.setCabecera(cab);
                                    resp.setItems(list);
                                    resp.setTotalItems(total);
                                    return resp;
                                }))
                );
    }

    public Mono<Cotizacion> updateCotizacion(Integer id, CotizacionUpdateRequest req) {
        return cotizacionRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cotización no encontrada")))
                .flatMap(c -> {
                    if (req.getClienteNombre() != null) c.setClienteNombre(req.getClienteNombre());
                    if (req.getClienteEmail() != null) c.setClienteEmail(req.getClienteEmail());
                    if (req.getClienteTelefono() != null) c.setClienteTelefono(req.getClienteTelefono());
                    if (req.getEmpresa() != null) c.setEmpresa(req.getEmpresa());
                    if (req.getEstado() != null) c.setEstado(req.getEstado());
                    if (req.getPrioridad() != null) c.setPrioridad(req.getPrioridad());
                    if (req.getAsesorAsignadoId() != null) c.setAsesorAsignadoId(req.getAsesorAsignadoId());
                    c.setUpdatedAt(OffsetDateTime.now());
                    return cotizacionRepository.save(c);
                });
    }

    public Mono<CotizacionDetalle> addDetalle(Integer cotizacionId, CotizacionItemRequest item) {
        return cotizacionRepository.existsById(cotizacionId)
                .flatMap(exists -> {
                    if (!exists) return Mono.error(new IllegalArgumentException("Cotización no encontrada"));
                    CotizacionDetalle d = toDetalleEntity(cotizacionId, item);
                    return detalleRepository.save(d);
                });
    }

    public Mono<CotizacionDetalle> updateDetalle(Integer itemId, CotizacionItemRequest item) {
        return detalleRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Ítem no encontrado")))
                .flatMap(d -> {
                    if (item.getVehiculoId() != null) d.setVehiculoId(item.getVehiculoId());
                    if (item.getCantidad() != null) d.setCantidad(item.getCantidad());
                    if (item.getPrecioUnitario() != null) d.setPrecioUnitario(item.getPrecioUnitario());
                    if (item.getNotas() != null) d.setNotas(item.getNotas());
                    return detalleRepository.save(d);
                });
    }

    public Mono<Void> deleteDetalle(Integer itemId) {
        return detalleRepository.deleteById(itemId);
    }

    private CotizacionDetalle toDetalleEntity(Integer cotizacionId, CotizacionItemRequest item) {
        CotizacionDetalle d = new CotizacionDetalle();
        d.setCotizacionId(cotizacionId);
        d.setVehiculoId(item.getVehiculoId());
        d.setCantidad(item.getCantidad() == null ? 1 : item.getCantidad());
        d.setPrecioUnitario(item.getPrecioUnitario() == null ? BigDecimal.ZERO : item.getPrecioUnitario());
        d.setNotas(item.getNotas());
        d.setCreatedAt(OffsetDateTime.now());
        return d;
    }

    private Mono<BigDecimal> calcularTotalDesdeDetalles(List<CotizacionDetalle> detalles) {
        BigDecimal total = BigDecimal.ZERO;
        for (CotizacionDetalle d : detalles) {
            BigDecimal subtotal;
            if (d.getSubtotal() != null) {
                subtotal = d.getSubtotal();
            } else {
                BigDecimal precio = d.getPrecioUnitario() == null ? BigDecimal.ZERO : d.getPrecioUnitario();
                int cantidad = d.getCantidad() == null ? 0 : d.getCantidad();
                subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
            }
            total = total.add(subtotal);
        }
        return Mono.just(total);
    }
}
