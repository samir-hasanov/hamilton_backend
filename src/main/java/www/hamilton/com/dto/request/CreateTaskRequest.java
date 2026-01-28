package www.hamilton.com.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import www.hamilton.com.serializer.InstantDeserializer;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Tapşırıq başlığı məcburidir")
    @Size(max = 200, message = "Tapşırıq başlığı 200 simvoldan çox ola bilməz")
    private String title;

    @Size(max = 1000, message = "Təsvir 1000 simvoldan çox ola bilməz")
    private String description;

    @NotNull(message = "Şirkət ID məcburidir")
    private Long companyId;

    @NotNull(message = "Kateqoriya ID məcburidir")
    private Long categoryId;

    private String assignedUsername;

    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant dueDate;
}
