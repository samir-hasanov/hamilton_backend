package www.hamilton.com.dto.response;

import java.util.Set;

public record UserInfoResponse(
        String username,
        String email,
        String phoneNumber,
        String displayName,
        boolean profileImagePresent,
        Set<RoleResponse> roles
) {
} 