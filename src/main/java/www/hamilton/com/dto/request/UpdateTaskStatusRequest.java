package www.hamilton.com.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import www.hamilton.com.entity.TaskStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskStatusRequest {

    @NotNull(message = "Tapşırıq statusu məcburidir")
    private TaskStatus status;

    private String comment;
}
