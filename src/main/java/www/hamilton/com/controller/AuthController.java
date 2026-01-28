package www.hamilton.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import www.hamilton.com.dto.request.*;
import www.hamilton.com.dto.response.AuthResponse;
import www.hamilton.com.dto.response.RoleResponse;
import www.hamilton.com.dto.response.UserInfoResponse;
import www.hamilton.com.service.AuthService;
import www.hamilton.com.service.RoleService;


import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "İstifadəçi autentifikasiya və idarəetmə API-ləri")
public class AuthController {

    private final AuthService authService;
    private final RoleService roleService;

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
}


