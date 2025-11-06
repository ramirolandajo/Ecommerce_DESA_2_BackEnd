package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpRequest;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BearerTokenInterceptorNotRequiredTest {
    @Test
    void whenTokenMissingAndNotRequired_requestProceedsWithoutAuthHeader() throws Exception {
        BackendTokenManager mgr = mock(BackendTokenManager.class);
        when(mgr.getToken()).thenReturn(null);
        BearerTokenInterceptor interceptor = new BearerTokenInterceptor(mgr);
        // Force tokenRequired false via reflection
        java.lang.reflect.Field f = BearerTokenInterceptor.class.getDeclaredField("tokenRequired");
        f.setAccessible(true);
        f.set(interceptor, false);

        HttpRequest req = new HttpRequest() {
            private final HttpHeaders headers = new HttpHeaders();
            @Override public HttpMethod getMethod() { return HttpMethod.GET; }
            @Override public URI getURI() { return URI.create("http://example.com"); }

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
        assertFalse(req.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
        verify(exec, times(1)).execute(any(), any());
    }
}

