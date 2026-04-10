package www.hamilton.com.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import www.hamilton.com.dto.request.AddPermissionRequest;
import www.hamilton.com.dto.request.UpdateRoleRequest;
import www.hamilton.com.dto.response.PermissionResponse;
import www.hamilton.com.entity.Permission;
import www.hamilton.com.entity.Role;
import www.hamilton.com.repository.PermissionRepository;
import www.hamilton.com.repository.RoleRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public List<PermissionResponse> getAllPermissions() {

        List<Permission> permissions=permissionRepository.findAll();

        return permissions.stream()
                .map(p -> PermissionResponse.builder()
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());

    }

    @Transactional
    public void createRoleWithPermissions(AddPermissionRequest request) {

        if (roleRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bu adda role artıq mövcuddur"
            );

        }

        Set<Permission> permissions =
                permissionRepository.findByNameIn(request.getPermissions());

        if (permissions.size() != request.getPermissions().size()) {
            throw new RuntimeException("Bəzi permission-lar tapılmadı");
        }

        Role role = Role.builder()
                .name(request.getName().toUpperCase())
                .permissions(permissions)
                .build();

        roleRepository.save(role);
    }

    @Transactional
    public void updateRolePermissions(String roleName, UpdateRoleRequest request) {
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol tapılmadı: " + roleName
                ));

        Set<Permission> permissions = permissionRepository.findByNameIn(request.getPermissions());
        if (permissions.size() != request.getPermissions().size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bəzi permission-lar tapılmadı"
            );
        }

        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}
