package ar.edu.uade.ecommerce.messaging;

import ar.edu.uade.ecommerce.Repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ECommerceEventServiceOriginTests {

    @Test
    void originModuleName_isIncludedInCoreEvent() {
        CoreApiClient client = mock(CoreApiClient.class);
        var svc = new ECommerceEventService(client, null, mock(BackendTokenManager.class), mock(ar.edu.uade.ecommerce.Repository.EventRepository.class), new ObjectMapper());
        org.springframework.test.util.ReflectionTestUtils.setField(svc, "originModuleName", "ABC");
        svc.emitRawEvent("T", java.util.Map.of("k","v"));
        ArgumentCaptor<CoreEvent> captor = ArgumentCaptor.forClass(CoreEvent.class);
        verify(client).sendEvent(captor.capture());
        assertEquals("ABC", captor.getValue().originModule);
    }
}

