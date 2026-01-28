package www.hamilton.com.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class InstantSerializer extends JsonSerializer<Instant> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(ZoneId.of("UTC"));

    @Override
    public void serialize(Instant instant, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (instant == null) {
            gen.writeNull();
        } else {
            gen.writeString(FORMATTER.format(instant));
        }
    }
}
