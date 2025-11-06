package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BearerTokenInterceptorHeaderTests {

    @Test
    void intercept_withToken_setsAuthorizationHeader() throws Exception {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn("tok");
        BearerTokenInterceptor it = new BearerTokenInterceptor(mgr);
        org.springframework.test.util.ReflectionTestUtils.setField(it, "tokenRequired", true);
        HttpRequest req = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(req.getHeaders()).thenReturn(headers);
        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse resp = mock(ClientHttpResponse.class);
        when(exec.execute(any(), any())).thenReturn(resp);
        ClientHttpResponse out = it.intercept(req, new byte[0], exec);
        assertEquals("Bearer tok", headers.getFirst(HttpHeaders.AUTHORIZATION));
        assertNotNull(out);
    }

    @Test
    void intercept_withoutToken_whenNotRequired_noHeaderAndProceeds() throws Exception {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn(null); // no token disponible
        BearerTokenInterceptor it = new BearerTokenInterceptor(mgr);
        // token no requerido -> no debe lanzar excepción ni setear header
        org.springframework.test.util.ReflectionTestUtils.setField(it, "tokenRequired", false);
        HttpRequest req = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(req.getHeaders()).thenReturn(headers);
        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse resp = mock(ClientHttpResponse.class);
        when(exec.execute(any(), any())).thenReturn(resp);
        ClientHttpResponse out = it.intercept(req, new byte[0], exec);
        assertNull(headers.getFirst(HttpHeaders.AUTHORIZATION));
        assertNotNull(out);
        verify(exec, times(1)).execute(any(), any());
    }
}
