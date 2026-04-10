package www.hamilton.com.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank
        @JsonAlias("refreshToken")
        String token
) {
} 