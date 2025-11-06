package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BearerTokenInterceptorTests {

    @Test
    void intercept_whenTokenAvailable_setsHeader() throws Exception {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn("tok");
        BearerTokenInterceptor it = new BearerTokenInterceptor(mgr);
        HttpRequest req = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(req.getHeaders()).thenReturn(headers);
        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse resp = mock(ClientHttpResponse.class);
        when(exec.execute(any(), any())).thenReturn(resp);
        ClientHttpResponse out = it.intercept(req, new byte[0], exec);
        assertNotNull(out);
        assertEquals("Bearer tok", headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void intercept_whenRequiredAndNoToken_throwsIOException() {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn(null);
        BearerTokenInterceptor it = new BearerTokenInterceptor(mgr);
        // activar flag tokenRequired por reflexión
        org.springframework.test.util.ReflectionTestUtils.setField(it, "tokenRequired", true);
        HttpRequest req = mock(HttpRequest.class);
        when(req.getHeaders()).thenReturn(new HttpHeaders());
        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        assertThrows(IOException.class, () -> it.intercept(req, new byte[0], exec));
    }
}

