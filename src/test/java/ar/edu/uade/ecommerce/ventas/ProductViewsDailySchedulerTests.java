package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Service.ProductViewServiceImpl;
import ar.edu.uade.ecommerce.messaging.ECommerceEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductViewsDailySchedulerTests {

    private ProductViewServiceImpl productViewService;
    private ECommerceEventService eventService;
    private ProductViewsDailyScheduler scheduler;

    @BeforeEach
    void setUp() {
        productViewService = mock(ProductViewServiceImpl.class);
        eventService = mock(ECommerceEventService.class);
        scheduler = new ProductViewsDailyScheduler();
        // inyectar mocks vía reflexión ya que los campos son @Autowired
        org.springframework.test.util.ReflectionTestUtils.setField(scheduler, "productViewServiceImpl", productViewService);
        org.springframework.test.util.ReflectionTestUtils.setField(scheduler, "ecommerceEventService", eventService);
    }

    @Test
    void emitDailyViewsEvent_buildsPayload_and_callsEmitRawEvent() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        Map<String, Object> s1 = new HashMap<>();
        s1.put("productId", 10L);
        s1.put("productTitle", "Prod A");
        s1.put("productCode", 123); // numérico -> se espera String "123"
        summaries.add(s1);
        Map<String, Object> s2 = new HashMap<>();
        s2.put("productId", 11L);
        s2.put("productTitle", "Prod B");
        s2.put("productCode", "ABC"); // string -> queda igual
        summaries.add(s2);
        Map<String, Object> s3 = new HashMap<>();
        s3.put("productId", 12L);
        s3.put("productTitle", "Prod C");
        s3.put("productCode", null); // null -> permanece null
        summaries.add(s3);
        when(productViewService.getAllViewsSummary()).thenReturn(summaries);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = (ArgumentCaptor<Map<String, Object>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Map.class);

        scheduler.emitDailyViewsEvent();

        verify(eventService).emitRawEvent(eq("GET: Vista diaria de productos"), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        assertNotNull(payload);
        assertTrue(payload.containsKey("views"));
        Object viewsObj = payload.get("views");
        assertInstanceOf(List.class, viewsObj);
        @SuppressWarnings("unchecked") List<Map<String, Object>> views = (List<Map<String, Object>>) viewsObj;
        assertEquals(3, views.size());

        Map<String, Object> p1 = views.get(0);
        assertEquals(10L, p1.get("id"));
        assertEquals("Prod A", p1.get("nombre"));
        assertEquals("123", p1.get("productCode"));

        Map<String, Object> p2 = views.get(1);
        assertEquals(11L, p2.get("id"));
        assertEquals("Prod B", p2.get("nombre"));
        assertEquals("ABC", p2.get("productCode"));

        Map<String, Object> p3 = views.get(2);
        assertEquals(12L, p3.get("id"));
        assertEquals("Prod C", p3.get("nombre"));
        assertNull(p3.get("productCode"));
    }

    @Test
    void emitDailyViewsEvent_whenServiceThrows_isCaught_and_noEmit() {
        when(productViewService.getAllViewsSummary()).thenThrow(new RuntimeException("boom"));
        // No debe lanzar
        assertDoesNotThrow(() -> scheduler.emitDailyViewsEvent());
        // Y no debe emitir evento
        verify(eventService, never()).emitRawEvent(anyString(), any());
    }
}
