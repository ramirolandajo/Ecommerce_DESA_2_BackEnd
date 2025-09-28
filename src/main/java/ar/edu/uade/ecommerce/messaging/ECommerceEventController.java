package ar.edu.uade.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ecommerce/listener")
public class ECommerceEventController {
    private static final Logger logger = LoggerFactory.getLogger(ECommerceEventController.class);

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public void receiveEvent(@RequestBody CoreEvent event, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Evento recibido desde API de Comunicación/Core: type='{}' originModule='{}' authHeader='{}'", event.type, event.originModule, authHeader);
        if (event == null || event.type == null) {
            logger.warn("Evento inválido recibido: {}", event);
            return;
        }

        switch (event.type) {
            case "UpdateStock":
            case "PATCH: Actualizar stock":
                handleStockUpdate(event.payload);
                break;
            case "AddProduct":
            case "POST: Agregar producto":
            case "EditProductFull":
            case "EditProductSimple":
            case "DeleteProduct":
            case "ActivateProduct":
            case "DeactivateProduct":
                handleProductEvent(event.type, event.payload);
                break;
            case "BrandSync":
            case "CategorySync":
            case "POST: Categoria agregada":
            case "DELETE: Categoria eliminada":
                handleCatalogEvent(event.type, event.payload);
                break;
            default:
                logger.info("Tipo de evento no manejado localmente: {}", event.type);
        }
    }

    private void handleStockUpdate(Object payload) {
        logger.info("Procesando actualización de stock: {}", payload);
        // TODO: parsear payload y actualizar la entidad Product / stock en BD local
    }

    private void handleProductEvent(String type, Object payload) {
        logger.info("Procesando evento de producto ({}): {}", type, payload);
        // TODO: implementar lógica para agregar/modificar/eliminar/activar/desactivar productos
    }

    private void handleCatalogEvent(String type, Object payload) {
        logger.info("Procesando evento de catálogo ({}): {}", type, payload);
        // TODO: implementar lógica para sync de marcas y categorías
    }
}

