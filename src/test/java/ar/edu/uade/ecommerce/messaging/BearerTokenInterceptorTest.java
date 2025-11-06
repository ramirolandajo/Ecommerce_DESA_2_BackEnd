package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BearerTokenInterceptorTest {

    @Test
    void whenTokenMissingAndRequired_thenIOException() throws Exception {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn(null);
        BearerTokenInterceptor interceptor = new BearerTokenInterceptor(mgr);
        // Force tokenRequired true via reflection
        java.lang.reflect.Field f = BearerTokenInterceptor.class.getDeclaredField("tokenRequired");
        f.setAccessible(true);
        f.set(interceptor, true);

        HttpRequest req = new HttpRequest() {
            @Override
            public HttpMethod getMethod() { return HttpMethod.GET; }

            @Override
            public URI getURI() { return URI.create("http://example.com"); }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }

            @Override
            public HttpHeaders getHeaders() { return new HttpHeaders(); }
        };

        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);

        assertThrows(IOException.class, () -> interceptor.intercept(req, new byte[0], exec));
    }

    @Test
    void whenTokenPresent_setsAuthorizationHeader() throws Exception {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn("tok-xyz");
        BearerTokenInterceptor interceptor = new BearerTokenInterceptor(mgr);
        java.lang.reflect.Field f = BearerTokenInterceptor.class.getDeclaredField("tokenRequired");
        f.setAccessible(true);
        f.set(interceptor, true);

        HttpRequest req = new HttpRequest() {
            private final HttpHeaders headers = new HttpHeaders();
            @Override public HttpMethod getMethod() { return HttpMethod.POST; }
            @Override public URI getURI() { return URI.create("http://example.com/api"); }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }

            @Override public HttpHeaders getHeaders() { return headers; }
        };

        ClientHttpRequestExecution exec = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse resp = mock(ClientHttpResponse.class);
        when(exec.execute(any(), any())).thenReturn(resp);

        ClientHttpResponse out = interceptor.intercept(req, new byte[0], exec);
        assertSame(resp, out);
        assertTrue(req.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
        assertEquals("Bearer tok-xyz", req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }
}

