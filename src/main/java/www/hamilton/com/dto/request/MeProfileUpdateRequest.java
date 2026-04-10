package www.hamilton.com.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record MeProfileUpdateRequest(
        @Size(max = 200) String displayName,
        @Email String email,
        @Size(max = 32) String phoneNumber,
        @Size(min = 8, max = 100) String currentPassword,
        @Size(min = 8, max = 100) String newPassword
) {
}
