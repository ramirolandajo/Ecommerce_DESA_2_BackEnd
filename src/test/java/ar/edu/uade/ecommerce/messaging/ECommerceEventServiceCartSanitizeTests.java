package ar.edu.uade.ecommerce.messaging;

import ar.edu.uade.ecommerce.Repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ECommerceEventServiceCartSanitizeTests {

    CoreApiClient coreApiClient;
    BackendTokenManager backendTokenManager;
    EventRepository eventRepository;
    ECommerceEventService service;

    @BeforeEach
    void setup() {
        coreApiClient = mock(CoreApiClient.class);
        backendTokenManager = mock(BackendTokenManager.class);
        eventRepository = mock(EventRepository.class);
        service = new ECommerceEventService(coreApiClient, null, backendTokenManager, eventRepository, new ObjectMapper());
    }

    @Test
    void sanitize_cartItemsKey_isNormalizedToItemsAndRemovesProductId() {
        when(backendTokenManager.getToken()).thenReturn("t");
        Map<String,Object> user = Map.of("id", 1);
        Map<String,Object> item = new HashMap<>();
        item.put("productId", 50);
        item.put("productCode", 500);
        Map<String,Object> cart = new HashMap<>();
        cart.put("cartItems", List.of(item));
        service.emitPurchasePending(1, user, cart);
        ArgumentCaptor<CoreEvent> captor = ArgumentCaptor.forClass(CoreEvent.class);
        verify(coreApiClient).sendEvent(captor.capture());
        CoreEvent ev = captor.getValue();
        @SuppressWarnings("unchecked")
        Map<String,Object> payload = (Map<String, Object>) ev.payload;
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> items = (List<Map<String, Object>>) ((Map<String,Object>) payload.get("cart")).get("items");
        assertNotNull(items);
        assertFalse(items.get(0).containsKey("productId"));
        assertEquals(500, items.get(0).get("productCode"));
    }
}

