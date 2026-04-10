package www.hamilton.com.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Email String email,
        @Size(min = 8, max = 100) String password,
        @Pattern(regexp = "\\+[0-9]{10,15}") String phoneNumber,
        @Size(max = 200) String displayName
) {
} 