package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BearerTokenInterceptorBlankTokenTests {

    @Test
    void intercept_requiredAndBlankToken_throws() {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn("   ");
        BearerTokenInterceptor it = new BearerTokenInterceptor(mgr);
        org.springframework.test.util.ReflectionTestUtils.setField(it, "tokenRequired", true);
        HttpRequest req = mock(HttpRequest.class);
        when(req.getHeaders()).thenReturn(new HttpHeaders());
        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        assertThrows(IOException.class, () -> it.intercept(req, new byte[0], exec));
    }
}

