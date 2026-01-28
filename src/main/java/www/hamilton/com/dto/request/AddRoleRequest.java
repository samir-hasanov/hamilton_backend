package www.hamilton.com.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddRoleRequest(
    @NotBlank String username,
    @NotBlank String roleName
) {} 