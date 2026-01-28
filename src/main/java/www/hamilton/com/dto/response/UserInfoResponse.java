package www.hamilton.com.dto.response;

import java.util.Set;

public record UserInfoResponse(
    String username,
    String email,
    String phoneNumber,
    Set<String> roles
) {} 