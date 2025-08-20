package ar.edu.uade.ecommerce.Entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Event {
    private String type;
    private Object payload;
    private LocalDateTime timestamp;

    public Event(String type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }
}

