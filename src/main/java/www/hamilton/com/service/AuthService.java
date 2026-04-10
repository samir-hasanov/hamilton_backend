package www.hamilton.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import www.hamilton.com.dto.request.AuthRequest;
import www.hamilton.com.dto.request.LogoutRequest;
import www.hamilton.com.dto.request.MeProfileUpdateRequest;
import www.hamilton.com.dto.request.RefreshTokenRequest;
import www.hamilton.com.dto.request.RegisterRequest;
import www.hamilton.com.dto.request.UserUpdateRequest;
import www.hamilton.com.dto.response.AuthResponse;
import www.hamilton.com.dto.response.AvatarView;
import www.hamilton.com.dto.response.RoleResponse;
import www.hamilton.com.dto.response.UserInfoResponse;
import www.hamilton.com.entity.*;
import www.hamilton.com.mapper.AuthMapper;
import www.hamilton.com.repository.TokenRepository;
import www.hamilton.com.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
    private final FileService fileService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    @Value("${hamilton.minio.bucket:hamilton}")
    private String profileBucket;

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
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserRefreshTokens(savedUser);
        saveRefreshToken(savedUser, refreshToken);

        return AuthMapper.toAuthResponse(accessToken, refreshToken, savedUser);
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
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserRefreshTokens(user);
        saveRefreshToken(user, refreshToken);

        return AuthMapper.toAuthResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        var storedToken = tokenRepository.findByToken(request.token()).orElse(null);

        if (storedToken != null) {
            var user = storedToken.getUser();
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            if (user != null) {
                revokeAllUserRefreshTokens(user);
            }
            return;
        }

        // Refresh token DB-də yoxdursa (köhnə session): JWT-dən username götürüb bütün refresh tokenləri ləğv et
        jwtService.extractUsernameIgnoringExpiration(request.token())
                .flatMap(userRepository::findByUsername)
                .ifPresent(this::revokeAllUserRefreshTokens);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String refreshTokenValue = request.refreshToken();

        var storedRefresh = tokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (storedRefresh.isExpired() || storedRefresh.isRevoked()) {
            throw new RuntimeException("Refresh token revoked or expired");
        }
        if (storedRefresh.getExpiresAt() != null && storedRefresh.getExpiresAt().isBefore(Instant.now())) {
            storedRefresh.setExpired(true);
            tokenRepository.save(storedRefresh);
            throw new RuntimeException("Refresh token expired");
        }

        var user = storedRefresh.getUser();
        if (user == null) {
            throw new RuntimeException("User not found for refresh token");
        }

        if (!jwtService.isTokenValid(refreshTokenValue, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        var newAccessToken = jwtService.generateAccessToken(user);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue)
                .build();
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

        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName().isBlank() ? null : request.displayName().trim());
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

        revokeAllUserRefreshTokens(user);
        userRepository.delete(user);


    }

    @Transactional
    public void resetUserPassword(String username, String newPassword) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));

        revokeAllUserRefreshTokens(user);
        userRepository.save(user);
        
        log.info("Password reset for user: {}", username);
    }

    /** Refresh token DB-də saxlanılır (beynəlxalq standart): user_id, token, issued_at, expires_at, revoked. */
    private void saveRefreshToken(User user, String refreshTokenValue) {
        var now = Instant.now();
        var expiresAt = now.plusMillis(refreshTokenExpirationMs);
        var token = Token.builder()
                .user(user)
                .token(refreshTokenValue)
                .tokenType(TokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserRefreshTokens(User user) {
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

    private final Function<User, UserInfoResponse> convertUserToUserInfoResponse = user -> {
        String profileFile = user.getProfileImageFile();
        boolean hasAvatar = profileFile != null && !profileFile.isBlank();
        return new UserInfoResponse(
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getDisplayName(),
                hasAvatar,
                user.getRoles() == null
                        ? Set.of()
                        : user.getRoles().stream()
                        .map(role -> new RoleResponse(
                                role.getName(),
                                role.getPermissions() == null
                                        ? Set.of()
                                        : role.getPermissions().stream()
                                        .map(Permission::getName)
                                        .collect(Collectors.toSet())
                        ))
                        .collect(Collectors.toSet())
        );
    };

    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUserProfile() {
        return convertUserToUserInfoResponse.apply(currentUserEntity());
    }

    @Transactional
    public UserInfoResponse updateCurrentUserProfile(MeProfileUpdateRequest request) {
        User user = currentUserEntity();
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName().isBlank() ? null : request.displayName().trim());
        }
        if (request.email() != null) {
            String newEmail = request.email().trim();
            userRepository.findByEmail(newEmail).ifPresent(other -> {
                if (!other.getId().equals(user.getId())) {
                    throw new RuntimeException("Email already in use");
                }
            });
            user.setEmail(newEmail);
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().isBlank() ? null : request.phoneNumber().trim());
        }
        boolean wantPw = request.newPassword() != null && !request.newPassword().isBlank();
        if (wantPw) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new RuntimeException("Current password required");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.newPassword()));
            revokeAllUserRefreshTokens(user);
        }
        userRepository.save(user);
        return convertUserToUserInfoResponse.apply(user);
    }

    @Transactional(readOnly = true)
    public Optional<AvatarView> getCurrentUserAvatarView() {
        User user = currentUserEntity();
        String objectKey = user.getProfileImageFile();
        if (objectKey == null || objectKey.isBlank()) {
            return Optional.empty();
        }
        return fileService.getBytesForProfileImage(objectKey, profileBucket)
                .map(bytes -> new AvatarView(
                        new ByteArrayResource(bytes),
                        mediaTypeForAvatarFile(objectKey)
                ));
    }

    @Transactional
    public UserInfoResponse uploadCurrentUserAvatar(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File required");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".webp")) {
            throw new RuntimeException("Only jpg, png, webp allowed");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("Max file size is 2MB");
        }
        User user = currentUserEntity();
        String oldStored = user.getProfileImageFile();
        if (oldStored != null && !oldStored.isBlank()) {
            fileService.deleteProfileImage(oldStored, profileBucket);
        }
        try {
            String publicUrl = fileService.uploadToMinio(file, profileBucket);
            user.setProfileImageFile(publicUrl);
            userRepository.save(user);
        } catch (RuntimeException e) {
            log.error("Avatar yükləmə xətası: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Avatar yükləmə xətası: {}", e.getMessage(), e);
            throw new RuntimeException("Avatar yüklənmədi: " + e.getMessage(), e);
        }
        return convertUserToUserInfoResponse.apply(user);
    }

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User u) {
            return userRepository.findByUsername(u.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private static MediaType mediaTypeForAvatarFile(String filenameOrUrl) {
        String path = filenameOrUrl;
        int q = path.indexOf('?');
        if (q >= 0) {
            path = path.substring(0, q);
        }
        int slash = path.lastIndexOf('/');
        if (slash >= 0) {
            path = path.substring(slash + 1);
        }
        String lower = path.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private final Function<List<User>, List<UserInfoResponse>> convertUserListToUserInfoRespList = users ->
            (users == null || users.isEmpty())
                    ? new ArrayList<>()
                    : users.stream()
                    .map(convertUserToUserInfoResponse)
                    .collect(Collectors.toList());


}
