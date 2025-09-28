package ar.edu.uade.ecommerce.messaging;

import java.time.LocalDateTime;

public class CoreEvent {
    public String type;
    public Object payload;
    public LocalDateTime timestamp;
    public String originModule;

    public CoreEvent(String type, Object payload, String originModule) {
        this.type = type;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
        this.originModule = originModule;
    }

    @Override
    public String toString() {
        return "CoreEvent{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", originModule='" + originModule + '\'' +
                '}';
    }
}
