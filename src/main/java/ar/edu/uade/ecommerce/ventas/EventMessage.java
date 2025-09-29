package ar.edu.uade.ecommerce.ventas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EventMessage {
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("payload")
    private Object payload;

    @JsonProperty("originModule")
    private String originModule;

    // Puede venir como ISO-8601 String o epoch (entero/doble). Lo modelamos como Object.
    @JsonProperty("timestamp")
    private Object timestamp;

    public EventMessage() {}

    // Getters y setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    public String getOriginModule() { return originModule; }
    public void setOriginModule(String originModule) { this.originModule = originModule; }

    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }

    @JsonIgnore
    public OffsetDateTime getTimestampAsOffsetDateTime() {
        if (timestamp == null) return OffsetDateTime.now();
        try {
            if (timestamp instanceof Number num) {
                // Si viene como epoch en segundos (con fracción)
                double seconds = num.doubleValue();
                long secs = (long) seconds;
                long nanos = (long) Math.round((seconds - secs) * 1_000_000_000L);
                return OffsetDateTime.ofInstant(Instant.ofEpochSecond(secs, nanos), ZoneId.systemDefault());
            }
            if (timestamp instanceof String s) {
                // Intentar ISO-8601 con Offset
                try {
                    return OffsetDateTime.parse(s);
                } catch (DateTimeParseException ignore) {
                }
                // Intentar Instant.parse (Z)
                try {
                    return OffsetDateTime.ofInstant(Instant.parse(s), ZoneId.systemDefault());
                } catch (DateTimeParseException ignore) {
                }
                // Intentar epoch como string
                try {
                    if (s.contains(".")) {
                        double seconds = Double.parseDouble(s);
                        long secs = (long) seconds;
                        long nanos = (long) Math.round((seconds - secs) * 1_000_000_000L);
                        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(secs, nanos), ZoneId.systemDefault());
                    } else {
                        long secs = Long.parseLong(s);
                        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(secs), ZoneId.systemDefault());
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        } catch (Exception e) {
            // Fallback: ahora
        }
        return OffsetDateTime.now();
    }

    @JsonIgnore
    public String getNormalizedEventType() {
        if (eventType == null) return null;
        String lower = eventType.toLowerCase();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // También normalizar espacios extra
        return normalized.trim().replaceAll("\\s+", " ");
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", originModule='" + originModule + '\'' +
                ", timestamp=" + (timestamp != null ? timestamp.toString() : "null") +
                '}';
    }
}

