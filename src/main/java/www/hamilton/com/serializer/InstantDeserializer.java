package www.hamilton.com.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantDeserializer extends JsonDeserializer<Instant> {
    
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_INSTANT,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    };

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // First try parsing as ISO instant
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            // Continue to other formats
        }
        
        // Try other formats
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                if (formatter == DateTimeFormatter.ISO_INSTANT) {
                    continue; // Already tried
                }
                
                // Parse as LocalDateTime and convert to Instant
                LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
                return localDateTime.toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }
        
        // If none of the formats work, throw an error
        throw new IOException("Unable to parse date: " + value + ". Expected formats: ISO-8601, yyyy-MM-dd'T'HH:mm:ss.SSS'Z', yyyy-MM-dd'T'HH:mm:ss'Z', yyyy-MM-dd'T'HH:mm:ss, yyyy-MM-dd HH:mm:ss");
    }
}
