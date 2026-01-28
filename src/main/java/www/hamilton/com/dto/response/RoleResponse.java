package www.hamilton.com.dto.response;

import java.util.Set;

public record RoleResponse(
    String name,
    Set<String> permissions
) {}
 