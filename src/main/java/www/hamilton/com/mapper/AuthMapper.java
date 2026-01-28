package www.hamilton.com.mapper;



import www.hamilton.com.dto.request.RegisterRequest;
import www.hamilton.com.dto.response.AuthResponse;
import www.hamilton.com.dto.response.RoleResponse;
import www.hamilton.com.dto.response.UserInfoResponse;
import www.hamilton.com.entity.Permission;
import www.hamilton.com.entity.Role;
import www.hamilton.com.entity.User;

import java.util.stream.Collectors;

public class AuthMapper {

    public static AuthResponse toAuthResponse(
            String accessToken,
            String refreshToken,
            User user
    ) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserInfoResponse(user))
                .build();
    }

    public static UserInfoResponse toUserInfoResponse(User user) {
        return new UserInfoResponse(
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    public static RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getName(),
                role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet())
        );
    }

    public static User toUserEntity(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setPhoneNumber(request.phoneNumber());
        return user;
    }
}
