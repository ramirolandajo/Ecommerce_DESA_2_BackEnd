package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import ar.edu.uade.ecommerce.messaging.KeycloakClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KeycloakTestController.class)
public class KeycloakTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakClient keycloakClient;

    @MockBean
    private ECommerceEventService ecommerceEventService;

    @Autowired
    private ObjectMapper objectMapper;


}
