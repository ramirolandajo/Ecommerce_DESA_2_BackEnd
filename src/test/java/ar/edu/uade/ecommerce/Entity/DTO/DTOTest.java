package ar.edu.uade.ecommerce.Entity.DTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DTOTest {
    @Test
    void testReviewRequestGettersSetters() {
        ReviewRequest review = new ReviewRequest();
        review.setCalification(4.5f);
        review.setDescription("Muy buen producto");
        assertEquals(4.5f, review.getCalification());
        assertEquals("Muy buen producto", review.getDescription());
    }

    @Test
    void testReviewRequestConstructor() {
        ReviewRequest review = new ReviewRequest();
        review.setCalification(5.0f);
        review.setDescription("Excelente");
        assertEquals(5.0f, review.getCalification());
        assertEquals("Excelente", review.getDescription());
    }

    @Test
    void testReviewRequestEqualsAndHashCode() {
        ReviewRequest r1 = new ReviewRequest();
        r1.setCalification(3.0f);
        r1.setDescription("comentario");
        ReviewRequest r2 = new ReviewRequest();
        r2.setCalification(3.0f);
        r2.setDescription("comentario");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testReviewRequestToString() {
        ReviewRequest review = new ReviewRequest();
        review.setCalification(2.0f);
        review.setDescription("comentario");
        String str = review.toString();
        assertNotNull(str);
        assertTrue(str.contains("comentario"));
    }
}
