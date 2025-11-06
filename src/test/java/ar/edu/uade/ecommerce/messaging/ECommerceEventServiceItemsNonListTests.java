package ar.edu.uade.ecommerce.messaging;

import ar.edu.uade.ecommerce.Repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ECommerceEventServiceItemsNonListTests {

    @Test
    void sanitize_doesNothingWhenItemsNotList() {
        CoreApiClient client = mock(CoreApiClient.class);
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        EventRepository repo = mock(EventRepository.class);
        var svc = new ECommerceEventService(client, null, mgr, repo, new ObjectMapper());
        Map<String,Object> cart = new HashMap<>(); cart.put("items", "x");
        svc.emitPurchasePending(1, java.util.Map.of(), cart);
        ArgumentCaptor<CoreEvent> captor = ArgumentCaptor.forClass(CoreEvent.class);
        verify(client).sendEvent(captor.capture());
        @SuppressWarnings("unchecked")
        Map<String,Object> payload = (Map<String, Object>) captor.getValue().payload;
        assertEquals("x", ((Map<?,?>) payload.get("cart")).get("items"));
    }
}

