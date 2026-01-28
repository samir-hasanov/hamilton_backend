package www.hamilton.com.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskCategoryRequest {

    @NotBlank(message = "Kateqoriya adı məcburidir")
    @Size(max = 100, message = "Kateqoriya adı 100 simvoldan çox ola bilməz")
    private String name;

    @Size(max = 500, message = "Təsvir 500 simvoldan çox ola bilməz")
    private String description;
}
