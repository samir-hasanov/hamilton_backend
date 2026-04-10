package www.hamilton.com.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import www.hamilton.com.entity.Permission;
import www.hamilton.com.entity.Role;
import www.hamilton.com.entity.TaskCategory;
import www.hamilton.com.entity.User;
import www.hamilton.com.repository.CompanyRepository;
import www.hamilton.com.repository.PermissionRepository;
import www.hamilton.com.repository.RoleRepository;
import www.hamilton.com.repository.TaskCategoryRepository;
import www.hamilton.com.repository.UserRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final TaskCategoryRepository taskCategoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            CompanyRepository companyRepository,
            TaskCategoryRepository taskCategoryRepository,
            @Qualifier("customPasswordEncoderForDefaultAdminUser") PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.taskCategoryRepository = taskCategoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        try {
            log.info("Starting database initialization...");
            createDefaultPermissions();
            createDefaultRoles();
            createDefaultAdminUser();
            createDefaultTaskCategories();
            // Real mühitdə Excel import istifadə olunacaq. Default companies yaradılmasın.
            log.info("Database initialization completed successfully");
        } catch (Exception e) {
            log.error("Database initialization failed: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
        }
    }

    private void createDefaultPermissions() {
        try {
            log.info("Creating default permissions...");
            String[] permissions = {
                    "USER_READ", "USER_WRITE", "USER_DELETE","USER_UPDATE",
                    "WORKER_READ", "WORKER_WRITE", "WORKER_DELETE","WORKER_UPDATE",
                    "MANAGER_READ", "MANAGER_WRITE", "MANAGER_DELETE","MANAGER_UPDATE",
            };

            for (String permissionName : permissions) {
                if (permissionRepository.findByName(permissionName).isEmpty()) {
                    Permission permission = Permission.builder()
                            .name(permissionName)
                            .build();
                    permissionRepository.save(permission);
                    log.debug("Created permission: {}", permissionName);
                }
            }
            log.info("Default permissions created successfully");
        } catch (Exception e) {
            log.error("Error creating default permissions: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void createDefaultRoles() {
        try {
            log.info("Creating default roles...");

            // USER role
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name("USER")
                                .build();
                        return roleRepository.save(role);
                    });

            // Add basic permissions to USER role
            Set<Permission> userPermissions = new HashSet<Permission>();
            permissionRepository.findByName("USER_READ").ifPresent(userPermissions::add);
            permissionRepository.findByName("WORKER_READ").ifPresent(userPermissions::add);
            userRole.setPermissions(userPermissions);
            userRole = roleRepository.save(userRole);
            log.info("USER role created with {} permissions", userRole.getPermissions().size());

            // ADMIN role
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name("ADMIN")
                                .build();
                        return roleRepository.save(role);
                    });

            // Add all permissions to ADMIN role
            Set<Permission> allPermissions = new HashSet<Permission>(
                    permissionRepository.findAll()
            );
            adminRole.setPermissions(allPermissions);
            adminRole = roleRepository.save(adminRole);
            log.info("ADMIN role created with {} permissions", adminRole.getPermissions().size());

            // WORKER role (READ, WRITE, UPDATE, DELETE)
            Role workerRole = roleRepository.findByName("WORKER")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name("WORKER")
                                .build();
                        return roleRepository.save(role);
                    });

            Set<Permission> workerPermissions = new HashSet<>();
            permissionRepository.findByName("WORKER_READ").ifPresent(workerPermissions::add);
            permissionRepository.findByName("WORKER_WRITE").ifPresent(workerPermissions::add);
            permissionRepository.findByName("WORKER_UPDATE").ifPresent(workerPermissions::add);
            permissionRepository.findByName("WORKER_DELETE").ifPresent(workerPermissions::add);
            workerRole.setPermissions(workerPermissions);
            workerRole = roleRepository.save(workerRole);
            log.info("WORKER role created with {} permissions", workerRole.getPermissions().size());

        } catch (Exception e) {
            log.error("Error creating default roles: {}", e.getMessage(), e);
            throw e;
        }
    }


    private void createDefaultAdminUser() {
        try {
            log.info("Checking for default admin user...");

            String defaultAdminUsername = "s.hasanov";
            String defaultAdminEmail = "samirhasanov18@gmail.com";

            // Əgər admin istifadəçi varsa, heç nə etmirik
            if (userRepository.findByUsername(defaultAdminUsername).isPresent()) {
                log.info("Default admin user already exists.");
                return;
            }

            // ADMIN rolu varsa götürürük, yoxdursa Exception tuluyaq
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            // Default Admini yaradaq!
            User adminUser = User.builder()
                    .username(defaultAdminUsername)
                    .email(defaultAdminEmail)
                    .password(passwordEncoder.encode("Samir1992@"))// şifrəni encode edim yoxluyaq gorek encoder ucun bean yaratmisig
                    .active(true)
                    .createdAt(Instant.now())
                    .build();

            adminUser.addRole(adminRole);
            userRepository.save(adminUser);

            log.info("Default admin user created successfully.");
        } catch (Exception e) {
            log.error("Error creating default admin user: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void createDefaultTaskCategories() {
        try {
            log.info("Creating default task categories...");

            String[][] categories = {
                    {"Maliyyə Hesabatları və Uçot", "Mühasibat uçotunun aparılması, maliyyə hesabatlarının hazırlanması, IFRS hesabatları, vergi bəyannamələrinin hazırlanması"},
                    {"Büdcə və Planlaşdırma", "Büdcə planlaşdırma, maliyyə planlaşdırma, strateji planlaşdırma"},
                    {"Vergi və Hüquqi Məsələlər", "Vergi məsələləri, hüquqi məsləhət, vergi optimallaşdırması"},
                    {"Audit və Daxili Nəzarət", "Audit xidmətləri, daxili nəzarət, risk idarəetməsi"},
                    {"İnvestisiya və Kapital İdarəetməsi", "İnvestisiya layihələri, kapital idarəetməsi, maliyyə təhlili"},
                    {"Xərclərin İdarə Edilməsi", "Xərc idarəetməsi, xərc optimallaşdırması, xərc analizi"},
                    {"Maliyyə Analizi və Konsaltinq", "Maliyyə analizi, konsaltinq xidmətləri, maliyyə təhlili"},
                    {"Maliyyə Texnologiyaları və Rəqəmsallaşma", "Maliyyə texnologiyaları, rəqəmsallaşma, avtomatlaşdırma"}
            };

            for (String[] categoryData : categories) {
                if (!taskCategoryRepository.existsByName(categoryData[0])) {
                    TaskCategory category = TaskCategory.builder()
                            .name(categoryData[0])
                            .description(categoryData[1])
                            .build();
                    taskCategoryRepository.save(category);
                    log.debug("Created task category: {}", categoryData[0]);
                }
            }
            log.info("Default task categories created successfully");
        } catch (Exception e) {
            log.error("Error creating default task categories: {}", e.getMessage(), e);
            throw e;
        }
    }

}