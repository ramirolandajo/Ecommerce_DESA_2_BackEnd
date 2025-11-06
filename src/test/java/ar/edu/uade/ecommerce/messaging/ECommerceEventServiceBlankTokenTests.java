package ar.edu.uade.ecommerce.messaging;

import ar.edu.uade.ecommerce.Repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ECommerceEventServiceBlankTokenTests {

    @Test
    void emitRawEvent_withBlankToken_stillPersistsAndSends() {
        CoreApiClient client = mock(CoreApiClient.class);
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn("   ");
        EventRepository repo = mock(EventRepository.class);
        var svc = new ECommerceEventService(client, null, mgr, repo, new ObjectMapper());
        assertDoesNotThrow(() -> svc.emitRawEvent("T", java.util.Map.of("x",1)));
        verify(repo).save(any());
        verify(client).sendEvent(any());
    }
}

