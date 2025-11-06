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
class ECommerceEventControllerLargePayloadTests {

    @Autowired MockMvc mockMvc;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void large_payload_returnsOk() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"AddProduct\",\"originModule\":\"mod\",\"payload\":{");
        for (int i = 0; i < 200; i++) {
            sb.append("\"k").append(i).append("\":").append(i).append(",");
        }
        sb.append("\"last\":true}});"); // end JSON intentionally valid
        String json = sb.toString().replace(");", "");
        mockMvc.perform(post("/api/ecommerce/listener/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}

