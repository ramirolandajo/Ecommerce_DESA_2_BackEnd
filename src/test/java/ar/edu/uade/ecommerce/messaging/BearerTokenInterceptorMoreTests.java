package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BearerTokenInterceptorMoreTests {

    @Test
    void intercept_notRequiredAndNoToken_continuesWithoutHeader() throws Exception {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn(null);
        BearerTokenInterceptor it = new BearerTokenInterceptor(mgr);
        org.springframework.test.util.ReflectionTestUtils.setField(it, "tokenRequired", false);
        HttpRequest req = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(req.getHeaders()).thenReturn(headers);
        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse resp = mock(ClientHttpResponse.class);
        when(exec.execute(any(), any())).thenReturn(resp);
        ClientHttpResponse out = it.intercept(req, new byte[0], exec);
        assertNotNull(out);
        assertNull(headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void intercept_exceptionWithNotRequired_doesNotThrow() throws Exception {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenThrow(new RuntimeException("x"));
        BearerTokenInterceptor it = new BearerTokenInterceptor(mgr);
        org.springframework.test.util.ReflectionTestUtils.setField(it, "tokenRequired", false);
        HttpRequest req = mock(HttpRequest.class);
        when(req.getHeaders()).thenReturn(new HttpHeaders());
        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse resp = mock(ClientHttpResponse.class);
        when(exec.execute(any(), any())).thenReturn(resp);
        assertDoesNotThrow(() -> it.intercept(req, new byte[0], exec));
    }
}

