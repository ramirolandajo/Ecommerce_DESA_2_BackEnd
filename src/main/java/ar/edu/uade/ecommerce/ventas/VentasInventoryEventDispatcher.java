package ar.edu.uade.ecommerce.ventas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VentasInventoryEventDispatcher {
    private static final Logger log = LoggerFactory.getLogger(VentasInventoryEventDispatcher.class);

    private final VentasInventorySyncService syncService;

    public VentasInventoryEventDispatcher(VentasInventorySyncService syncService) {
        this.syncService = syncService;
    }

    public void process(EventMessage msg) {
        String normalizedType = msg.getNormalizedEventType();
        if (normalizedType == null) {
            log.warn("[Inventario->Ventas] eventType nulo, se ignora: {}", msg);
            return;
        }
        switch (normalizedType) {
            case "put: actualizar stock" -> syncService.actualizarStock(msg.getPayload());
            case "post: agregar un producto" -> syncService.crearProducto(msg.getPayload());
            // Alias que estaba faltando y llega desde Inventario
            case "post: producto creado" -> syncService.crearProducto(msg.getPayload());
            case "patch: modificar un producto" -> syncService.modificarProducto(msg.getPayload());
            case "patch: producto desactivado" -> syncService.desactivarProducto(msg.getPayload());
            case "patch: producto activado" -> syncService.activarProducto(msg.getPayload());
            // Alias para variación de texto usada por Inventario
            case "patch: activar producto" -> syncService.activarProducto(msg.getPayload());
            case "put: producto actualizado" -> syncService.upsertProducto(msg.getPayload());
            case "post: marca creada" -> syncService.crearMarca(msg.getPayload());
            case "patch: marca activada" -> syncService.activarMarca(msg.getPayload());
            case "patch: marca desactivada" -> syncService.desactivarMarca(msg.getPayload());
            case "post: categoria creada" -> syncService.crearCategoria(msg.getPayload());
            case "patch: categoria activada" -> syncService.activarCategoria(msg.getPayload());
            case "patch: categoria desactivada" -> syncService.desactivarCategoria(msg.getPayload());
            case "post: agregar productos (batch)" -> syncService.crearProductosBatch(msg.getPayload());
            // alias sin paréntesis por si viene sin ellos
            case "post: agregar productos batch" -> syncService.crearProductosBatch(msg.getPayload());
            default -> log.info("[Inventario->Ventas] Evento no reconocido, ignorado: {}", msg.getEventType());
        }
    }
}
