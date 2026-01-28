package www.hamilton.com.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import www.hamilton.com.dto.response.RoleResponse;
import www.hamilton.com.entity.Permission;
import www.hamilton.com.entity.Role;
import www.hamilton.com.exception.RoleNotFoundException;
import www.hamilton.com.repository.RoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional
    public Role createRole(String name) {
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRole(String name) {
        Role role = findByName(name)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + name));
        roleRepository.delete(role);
    }

    @SuppressWarnings("unused")
    private RoleResponse convertToDto(Role role) {
        return new RoleResponse(
                role.getName(),
                role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet())
        );
    }
}
