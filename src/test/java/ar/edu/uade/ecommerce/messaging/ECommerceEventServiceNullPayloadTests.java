package ar.edu.uade.ecommerce.messaging;

import ar.edu.uade.ecommerce.Entity.Event;
import ar.edu.uade.ecommerce.Repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ECommerceEventServiceNullPayloadTests {

    @Test
    void emitRawEvent_withNullPayload_persistsNull() throws Exception {
        CoreApiClient client = mock(CoreApiClient.class);
        EventRepository repo = mock(EventRepository.class);
        var svc = new ECommerceEventService(client, null, mock(BackendTokenManager.class), repo, new ObjectMapper());
        svc.emitRawEvent("T", null);
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(repo).save(captor.capture());
        Event saved = captor.getValue();
        var f = saved.getClass().getDeclaredField("payload");
        f.setAccessible(true);
        Object payload = f.get(saved);
        assertNull(payload);
    }
}

