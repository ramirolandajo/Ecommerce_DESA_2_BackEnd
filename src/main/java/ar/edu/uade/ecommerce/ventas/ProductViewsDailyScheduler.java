package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import ar.edu.uade.ecommerce.Service.ProductViewServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductViewsDailyScheduler {
    private static final Logger log = LoggerFactory.getLogger(ProductViewsDailyScheduler.class);

    @Autowired
    private ProductViewServiceImpl productViewServiceImpl;

    @Autowired
    private ECommerceEventService ecommerceEventService;

    // Emite el evento cada 2 minutos (cron por defecto) y permite override por property ventas.views.cron
    // Ejecuta el método una vez cada 24 horas (a las 00:00)
    // @Scheduled(cron = "0 0 0 * * *")

    @Scheduled(cron = "${ventas.views.cron:0 */2 * * * *}")
    public void emitDailyViewsEvent() {
        try {
            // Construcción del listado: [{id, nombre, productCode}, ...]
            List<Map<String, Object>> summaries = productViewServiceImpl.getAllViewsSummary();
            List<Map<String, Object>> productsForEvent = summaries.stream().map(s -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", s.get("productId"));
                m.put("nombre", s.get("productTitle"));
                Object code = s.get("productCode");
                m.put("productCode", code != null ? String.valueOf(code) : null);
                return m;
            }).toList();

            // El middleware espera un objeto en payload, no un array: envolver en {"views": [...]}
            Map<String, Object> payload = new HashMap<>();
            payload.put("views", productsForEvent);

            log.info("[Scheduler][ViewsDaily] Preparando envío de 'GET: Vista diaria de productos' ({} ítems)", productsForEvent.size());
            ecommerceEventService.emitRawEvent("GET: Vista diaria de productos", payload);
            log.info("[Scheduler][ViewsDaily] Evento enviado OK: type='GET: Vista diaria de productos', items={} ", productsForEvent.size());
        } catch (Exception ex) {
            log.error("[Scheduler][ViewsDaily] Error al emitir 'GET: Vista diaria de productos': {}", ex.getMessage(), ex);
        }
    }
}
