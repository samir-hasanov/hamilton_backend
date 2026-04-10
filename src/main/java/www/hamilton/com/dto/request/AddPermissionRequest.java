package www.hamilton.com.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Set;
@Data
@Builder
public class AddPermissionRequest {
    @NotBlank(message = "Role adı boş ola bilməz")
    @Size(max = 50, message = "Role adı maksimum 50 simvol ola bilər")
    private String name;

    @NotEmpty(message = "Ən azı bir permission seçilməlidir")
    private Set<String> permissions;
}
