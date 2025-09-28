package ar.edu.uade.ecommerce.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CoreEventSerializationTest {

    @Test
    void timestamp_is_serialized_as_string() throws Exception {
        CoreEvent ev = new CoreEvent("TEST", Map.of("k","v"), "Ecommerce");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(ev);
        // Parse back to map
        Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
        assertTrue(map.containsKey("timestamp"));
        Object ts = map.get("timestamp");
        assertInstanceOf(String.class, ts, "timestamp debe ser String en JSON, no arreglo/numero");
        String tsStr = (String) ts;
        assertFalse(tsStr.startsWith("["), "timestamp no debe serializarse como arreglo");
        assertTrue(tsStr.length() >= 10, "timestamp debe tener formato ISO-8601 razonable");
    }
}

