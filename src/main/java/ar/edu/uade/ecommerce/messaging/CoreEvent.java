package ar.edu.uade.ecommerce.messaging;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class CoreEvent {
    public String type;
    public Object payload;
    // Cambiamos a String para garantizar formato estable en JSON
    public String timestamp;
    public String originModule;

    public CoreEvent(String type, Object payload, String originModule) {
        this.type = type;
        this.payload = payload;
        // Usamos OffsetDateTime para evitar el sufijo [Region] de ZonedDateTime y garantizar ISO-8601
        this.timestamp = OffsetDateTime.now(ZoneId.systemDefault()).toString();
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
