package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BearerTokenInterceptorNoTokenTests {

    @Test
    void intercept_withoutToken_andNotRequired_doesNotSetHeader() throws Exception {
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
        try (resp) {
            ClientHttpResponse out = it.intercept(req, new byte[0], exec);
            assertEquals("", headers.getFirst(HttpHeaders.AUTHORIZATION) == null ? "" : headers.getFirst(HttpHeaders.AUTHORIZATION));
            assertSame(resp, out);
        }
    }

    @Test
    void intercept_withoutToken_andRequired_throwsIOException() {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn("");
        BearerTokenInterceptor it = new BearerTokenInterceptor(mgr);
        org.springframework.test.util.ReflectionTestUtils.setField(it, "tokenRequired", true);
        HttpRequest req = mock(HttpRequest.class);
        when(req.getHeaders()).thenReturn(new HttpHeaders());
        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        assertThrows(java.io.IOException.class, () -> it.intercept(req, new byte[0], exec));
    }
}
