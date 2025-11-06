package ar.edu.uade.ecommerce.messaging;

import ar.edu.uade.ecommerce.Repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ECommerceEventServiceSanitizeKeyTypesTests {

    @Test
    void sanitize_stringifiesNonStringKeys() {
        CoreApiClient client = mock(CoreApiClient.class);
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        EventRepository repo = mock(EventRepository.class);
        var svc = new ECommerceEventService(client, null, mgr, repo, new ObjectMapper());
        Map<Object,Object> rawItem = new HashMap<>(); rawItem.put(123, "val");
        Map<String,Object> cart = new HashMap<>(); cart.put("items", List.of(rawItem));
        svc.emitPurchasePending(1, Map.of(), cart);
        ArgumentCaptor<CoreEvent> captor = ArgumentCaptor.forClass(CoreEvent.class);
        verify(client).sendEvent(captor.capture());
        @SuppressWarnings("unchecked")
        Map<String,Object> payload = (Map<String, Object>) captor.getValue().payload;
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> items = (List<Map<String, Object>>) ((Map<String,Object>) payload.get("cart")).get("items");
        assertEquals("val", items.get(0).get("123"));
    }
}

