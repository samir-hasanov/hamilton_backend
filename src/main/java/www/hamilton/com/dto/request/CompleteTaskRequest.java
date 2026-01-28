package www.hamilton.com.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompleteTaskRequest {

    @Size(max = 2000, message = "Şərh 2000 simvoldan çox ola bilməz")
    private String comment;

    private MultipartFile completionFile;
}
