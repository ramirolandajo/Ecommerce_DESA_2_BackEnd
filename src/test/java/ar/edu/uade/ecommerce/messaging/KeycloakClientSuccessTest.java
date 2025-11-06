//package ar.edu.uade.ecommerce.messaging;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpEntity;
//import org.springframework.web.client.RestTemplate;
//
//import java.lang.reflect.Field;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class KeycloakClientSuccessTest {
//    @Test
//    void getClientAccessTokenAndInfo_whenRestTemplateReturnsToken() throws Exception {
//        // Crear cliente y mockear RestTemplate
//        KeycloakClient client = new KeycloakClient();
//        RestTemplate rt = mock(RestTemplate.class);
//
//        // Inyectar restTemplate mock (private final) via reflection
//        Field restField = KeycloakClient.class.getDeclaredField("restTemplate");
//        restField.setAccessible(true);
//        restField.set(client, rt);
//
//        // Configurar tokenUrl y clientId via reflection
//        Field urlField = KeycloakClient.class.getDeclaredField("tokenUrl");
//        urlField.setAccessible(true);
//        urlField.set(client, "http://kc/token");
//        Field idField = KeycloakClient.class.getDeclaredField("clientId");
//        idField.setAccessible(true);
//        idField.set(client, "myclient");
//        Field secretField = KeycloakClient.class.getDeclaredField("clientSecret");
//        secretField.setAccessible(true);
//        secretField.set(client, "mysecret");
//
//        // Simular respuesta del RestTemplate
//        when(rt.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
//                .thenReturn("{\"access_token\":\"tok123\",\"expires_in\":120}");
//
//        KeycloakClient.TokenInfo info = client.getClientAccessTokenInfo();
//        assertNotNull(info);
//        assertEquals("tok123", info.accessToken);
//        assertTrue(info.expiresInSeconds > 0);
//
//        String tok = client.getClientAccessToken();
//        assertEquals("tok123", tok);
//
//        // Llamada posterior debe usar cache y no llamar nuevamente al restTemplate
//        clearInvocations(rt);
//        String tok2 = client.getClientAccessToken();
//        assertEquals("tok123", tok2);
//        verifyNoInteractions(rt);
//    }
//}
