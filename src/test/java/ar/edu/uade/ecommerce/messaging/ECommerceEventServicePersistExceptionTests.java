package ar.edu.uade.ecommerce.messaging;

import ar.edu.uade.ecommerce.Repository.EventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ECommerceEventServicePersistExceptionTests {

    static class ThrowingMapper extends ObjectMapper {
        @Override
        public String writeValueAsString(Object value) throws JsonProcessingException {
            throw new JsonProcessingException("boom"){};
        }
    }

    @Test
    void emitRawEvent_whenMapperThrows_stillSends() {
        CoreApiClient client = mock(CoreApiClient.class);
        EventRepository repo = mock(EventRepository.class);
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        var svc = new ECommerceEventService(client, null, mgr, repo, new ThrowingMapper());
        assertDoesNotThrow(() -> svc.emitRawEvent("T", java.util.Map.of("x",1)));
        verify(client).sendEvent(any());
    }
}

