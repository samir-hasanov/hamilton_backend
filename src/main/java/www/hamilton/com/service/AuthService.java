package www.hamilton.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import www.hamilton.com.dto.request.*;
import www.hamilton.com.dto.response.AuthResponse;
import www.hamilton.com.dto.response.UserInfoResponse;
import www.hamilton.com.entity.Role;
import www.hamilton.com.entity.Token;
import www.hamilton.com.entity.TokenType;
import www.hamilton.com.entity.User;
import www.hamilton.com.mapper.AuthMapper;
import www.hamilton.com.repository.TokenRepository;
import www.hamilton.com.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameOrEmail(request.username(), request.email())) {
            throw new RuntimeException("Username or email already exists");
        }

        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .active(true)
                .createdAt(Instant.now())
                .build();

        // Default role assignment
        Role userRole = roleService.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.addRole(userRole);

        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(savedUser, jwtToken);


        return AuthMapper.toAuthResponse(jwtToken, refreshToken, savedUser);
    }

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        var user = (User) authentication.getPrincipal();
        var jwtToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthMapper.toAuthResponse(jwtToken, refreshToken, user);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        var storedToken = tokenRepository.findByToken(request.token())
                .orElse(null);

        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);


        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.refreshToken();
        final String username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateAccessToken(user);

                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        }
        throw new RuntimeException("Invalid refresh token");
    }

    @Transactional
    public void addRoleToUser(String username, String roleName) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var role = roleService.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.addRole(role);
        userRepository.save(user);


    }

    @Transactional
    public void removeRoleFromUser(String username, String roleName) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var role = roleService.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.removeRole(role);
        userRepository.save(user);


    }

    @Transactional
    public void updateUser(String username, UserUpdateRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.email() != null) {
            user.setEmail(request.email());
        }

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        userRepository.save(user);

    }

    @Transactional
    public void deleteUser(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        revokeAllUserTokens(user);
        userRepository.delete(user);


    }

    @Transactional
    public void resetUserPassword(String username, String newPassword) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));

        revokeAllUserTokens(user);
        
        userRepository.save(user);
        
        log.info("Password reset for user: {}", username);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllByUser_IdAndExpiredFalseAndRevokedFalse(user.getId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }


    public List<UserInfoResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return convertUserListToUserInfoRespList.apply(users);
    }

    @Transactional(readOnly = true)
    public List<UserInfoResponse> getUsersByRole(String roleName) {
        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        List<User> users = userRepository.findByRolesContaining(role);
        return convertUserListToUserInfoRespList.apply(users);
    }

    private final Function<User, UserInfoResponse> convertUserToUserInfoResponse = user ->
            new UserInfoResponse(
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRoles() == null
                            ? Set.of()
                            : user.getRoles()
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet())
            );

    private final Function<List<User>, List<UserInfoResponse>> convertUserListToUserInfoRespList = users ->
            (users == null || users.isEmpty())
                    ? new ArrayList<>()
                    : users.stream()
                    .map(convertUserToUserInfoResponse)
                    .collect(Collectors.toList());


}
