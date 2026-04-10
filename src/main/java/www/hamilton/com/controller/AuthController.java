package www.hamilton.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import www.hamilton.com.dto.request.*;
import www.hamilton.com.dto.response.AuthResponse;
import www.hamilton.com.dto.response.PermissionResponse;
import www.hamilton.com.dto.response.RoleResponse;
import www.hamilton.com.dto.response.UserInfoResponse;
import www.hamilton.com.service.AuthService;
import www.hamilton.com.service.PermissionService;
import www.hamilton.com.service.RoleService;


import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "İstifadəçi autentifikasiya və idarəetmə API-ləri")
public class AuthController {

    private final AuthService authService;
    private final RoleService roleService;
    private final PermissionService permissionService;

    @Operation(summary = "Yeni istifadəçi qeydiyyatı", description = "Sistemə yeni istifadəçi qeydiyyatı")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "İstifadəçi girişi", description = "İstifadəçi adı və şifrə ilə giriş")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(summary = "Cari profil", description = "JWT ilə daxil olmuş istifadəçinin məlumatları")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMe() {
        return ResponseEntity.ok(authService.getCurrentUserProfile());
    }

    @Operation(summary = "Cari profili yenilə", description = "Görünən ad, email, telefon, istəyə görə şifrə")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public ResponseEntity<UserInfoResponse> updateMe(@Valid @RequestBody MeProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.updateCurrentUserProfile(request));
    }

    @Operation(summary = "Cari istifadəçinin avatarı", description = "Şəkil axını (Authorization: Bearer tələb olunur)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(
            value = "/me/avatar",
            produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp", MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public ResponseEntity<Resource> getMyAvatar() {
        return authService.getCurrentUserAvatarView()
                .map(v -> ResponseEntity.ok().contentType(v.mediaType()).body(v.resource()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Avatar yüklə", description = "jpg/png/webp, max 2MB")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserInfoResponse> uploadMyAvatar(@RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(authService.uploadCurrentUserAvatar(file));
    }

    @Operation(summary = "Token yeniləmə", description = "Refresh token ilə yeni access token almaq")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "Çıxış", description = "İstifadəçi çıxışı və token silinməsi")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "İstifadəçiyə rol əlavə et", description = "İstifadəçiyə yeni rol təyin etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{username}/roles")
    public ResponseEntity<Void> addRoleToUser(
            @PathVariable String username,
            @RequestParam String roleName
    ) {
        authService.addRoleToUser(username, roleName);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "İstifadəçidən rol sil", description = "İstifadəçidən rol silmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{username}/roles")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable String username,
            @RequestParam String roleName
    ) {
        authService.removeRoleFromUser(username, roleName);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "İstifadəçi məlumatlarını yenilə", description = "İstifadəçi məlumatlarını yeniləmək")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/users/{username}")
    public ResponseEntity<Void> updateUser(
            @PathVariable String username,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        authService.updateUser(username, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "İstifadəçini sil", description = "İstifadəçini sistemdən silmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        authService.deleteUser(username);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Bütün rolları al", description = "Sistemdəki bütün rolları siyahıya almaq")
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @Operation(summary = "Bütün istifadəçiləri al", description = "Sistemdəki bütün istifadəçiləri siyahıya almaq")
    @GetMapping("/users")
    public ResponseEntity<List<UserInfoResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @Operation(summary = "Rola görə istifadəçiləri al", description = "Müəyyən rola malik istifadəçiləri siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/role/{roleName}")
    public ResponseEntity<List<UserInfoResponse>> getUsersByRole(@PathVariable String roleName) {
        return ResponseEntity.ok(authService.getUsersByRole(roleName));
    }

    @Operation(summary = "İstifadəçi şifrəsini sıfırla", description = "İstifadəçi şifrəsini sıfırlamaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{username}/reset-password")
    public ResponseEntity<Void> resetUserPassword(
            @PathVariable String username,
            @RequestParam String newPassword
    ) {
        authService.resetUserPassword(username, newPassword);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "Bütün permissionlari al", description = "Sistemdəki bütün permission siyahıya almaq")
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @Operation(summary = "Rol yarat (permission-larla)", description = "Yeni rol yaratmaq və ona permission təyin etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = { "/roles", "addPermissionsToRole" })
    public ResponseEntity<Void> createRoleWithPermissions(
            @RequestBody @Valid AddPermissionRequest request
    ) {
        permissionService.createRoleWithPermissions(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rolun permission-larını yenilə", description = "Mövcud rolun permission siyahısını dəyişmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/roles/{roleName}")
    public ResponseEntity<Void> updateRolePermissions(
            @PathVariable String roleName,
            @RequestBody @Valid UpdateRoleRequest request
    ) {
        permissionService.updateRolePermissions(roleName, request);
        return ResponseEntity.ok().build();
    }
}


