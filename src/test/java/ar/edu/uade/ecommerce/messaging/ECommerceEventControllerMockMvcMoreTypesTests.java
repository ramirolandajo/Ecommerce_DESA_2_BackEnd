package ar.edu.uade.ecommerce.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.uade.ecommerce.Security.JwtAuthenticationFilter;
import ar.edu.uade.ecommerce.Security.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ECommerceEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class ECommerceEventControllerMockMvcMoreTypesTests {

    @Autowired MockMvc mockMvc;

    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void postEvent_productAndCatalogTypes_returnOk() throws Exception {
        String[] types = {"AddProduct","EditProductFull","DeactivateProduct","POST: Categoria agregada"};
        for (String t : types) {
            String json = String.format("{\"type\":\"%s\",\"payload\":{},\"originModule\":\"mod\"}", t);
            mockMvc.perform(post("/api/ecommerce/listener/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk());
        }
    }
}
