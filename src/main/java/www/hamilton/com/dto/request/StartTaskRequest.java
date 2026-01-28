package www.hamilton.com.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StartTaskRequest {

    @Size(max = 2000, message = "Şərh 2000 simvoldan çox ola bilməz")
    private String comment;
}
