package ar.edu.uade.ecommerce.messaging;

import ar.edu.uade.ecommerce.Repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ECommerceEventServiceReviewNullRateTests {

    @Test
    void emitReviewCreated_withNullRate_sendsAndPersists() {
        CoreApiClient client = mock(CoreApiClient.class);
        EventRepository repo = mock(EventRepository.class);
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        var svc = new ECommerceEventService(client, null, mgr, repo, new ObjectMapper());
        svc.emitReviewCreated(10, "hola", null);
        verify(client).sendEvent(any());
        verify(repo).save(any());
    }
}
