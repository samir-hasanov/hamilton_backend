package www.hamilton.com.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * Rolun permission-larını yeniləmək üçün request DTO.
 */
@Data
@Builder
public class UpdateRoleRequest {

    @NotEmpty(message = "Ən azı bir permission seçilməlidir")
    private Set<String> permissions;
}
