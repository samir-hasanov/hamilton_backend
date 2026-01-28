package www.hamilton.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import www.hamilton.com.dto.request.CreateTaskRequest;
import www.hamilton.com.dto.request.UpdateTaskStatusRequest;
import www.hamilton.com.dto.request.StartTaskRequest;
import www.hamilton.com.dto.response.*;
import www.hamilton.com.entity.*;
import www.hamilton.com.exception.ResourceNotFoundException;
import www.hamilton.com.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskService {

    // Fayl yükləmə üçün konstant
    private static final String UPLOAD_DIR = "D:\\Maliyye_sekiler\\";
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", ".xls", ".xlsx"};

    private final TaskRepository taskRepository;
    private final CompanyRepository companyRepository;
    private final TaskCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TaskLogRepository taskLogRepository;

    public TaskResponse createTask(CreateTaskRequest request) {
        try {
            System.out.println("Creating task with request: " + request);

            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Şirkət tapılmadı: " + request.getCompanyId()));

            TaskCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kateqoriya tapılmadı: " + request.getCategoryId()));

            User assignedUser = null;
            if (request.getAssignedUsername() != null && !request.getAssignedUsername().trim().isEmpty()) {
                assignedUser = userRepository.findByUsername(request.getAssignedUsername())
                        .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + request.getAssignedUsername()));
            }

            Task task = Task.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .company(company)
                    .category(category)
                    .assignedUser(assignedUser)
                    .dueDate(request.getDueDate())
                    .build();

            System.out.println("Built task: " + task);
            Task savedTask = taskRepository.save(task);
            System.out.println("Saved task: " + savedTask);

            return mapToResponse(savedTask);
        } catch (Exception e) {
            System.err.println("Error creating task: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + username));

        return taskRepository.findByAssignedUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + username));

        return taskRepository.findByAssignedUser(user, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByCompany(Long companyId) {
        return taskRepository.findByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tapşırıq tapılmadı: " + id));
        return mapToResponse(task);
    }

    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tapşırıq tapılmadı: " + taskId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + currentUsername));

        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = request.getStatus();

        // Status dəyişikliyi log-lanır
        TaskLog taskLog = TaskLog.builder()
                .task(task)
                .user(currentUser)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .comment(request.getComment())
                .build();
        taskLogRepository.save(taskLog);

        // Status dəyişikliyi
        if (newStatus == TaskStatus.ACTIVE && oldStatus == TaskStatus.PENDING) {
            task.startTask();
        } else if (newStatus == TaskStatus.COMPLETED) {
            task.completeTask();
        } else {
            task.setStatus(newStatus);
        }

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    public TaskResponse assignTaskToUser(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tapşırıq tapılmadı: " + taskId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + username));

        task.setAssignedUser(user);
        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tapşırıq tapılmadı: " + id);
        }
        taskRepository.deleteById(id);
    }

    private TaskResponse mapToResponse(Task task) {
        boolean isOverdue = task.getDueDate() != null &&
                task.getDueDate().isBefore(Instant.now()) &&
                task.getStatus() != TaskStatus.COMPLETED;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .assignedUser(task.getAssignedUser() != null ?
                        new UserResponse(
                                task.getAssignedUser().getUsername(),
                                task.getAssignedUser().getEmail(),
                                task.getAssignedUser().getPhoneNumber(),
                                task.getAssignedUser().getRoles().stream()
                                        .map(role -> role.getName())
                                        .collect(java.util.stream.Collectors.toSet()),
                                task.getAssignedUser().getCreatedAt()
                        ) : null)
                .company(CompanyResponse.builder()
                        .id(task.getCompany().getId())
                        .name(task.getCompany().getName())
                        .taxNumber(task.getCompany().getTaxNumber())
                        .taxType(task.getCompany().getAsanId())
                        .build())
                .category(TaskCategoryResponse.builder()
                        .id(task.getCategory().getId())
                        .name(task.getCategory().getName())
                        .description(task.getCategory().getDescription())
                        .build())
                .dueDate(task.getDueDate())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .workerComment(task.getWorkerComment())
                .completionComment(task.getCompletionComment())
                .completionFilePath(task.getCompletionFilePath())
                .completionFileName(task.getCompletionFileName())
                .isOverdue(isOverdue)
                .build();
    }

    // Tapşırığı başlatma (işçi üçün)
    public TaskResponse startTask(Long taskId, StartTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tapşırıq tapılmadı: " + taskId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + currentUsername));

        // Tapşırığın bu istifadəçiyə təyin edildiyini yoxla
        if (!task.getAssignedUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bu tapşırığı yalnız təyin edilən işçi başlada bilər");
        }

        // Tapşırığı başlat
        task.startTask();
        task.setWorkerComment(request.getComment());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    // Tapşırığı tamamlama (işçi üçün)
    public TaskResponse completeTask(Long taskId, String comment, org.springframework.web.multipart.MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tapşırıq tapılmadı: " + taskId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + currentUsername));

        // Tapşırığın bu istifadəçiyə təyin edildiyini yoxla
        if (!task.getAssignedUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bu tapşırığı yalnız təyin edilən işçi tamamlaya bilər");
        }

        // Fayl yüklə (əgər varsa)
        String filePath = null;
        String fileName = null;
        if (file != null && !file.isEmpty()) {
            try {
                filePath = saveFile(file);
                fileName = file.getOriginalFilename();
            } catch (IOException e) {
                throw new RuntimeException("Fayl yüklənərkən xəta baş verdi: " + e.getMessage());
            }
        }

        // Tapşırığı tamamla
        task.completeTask();
        task.setCompletionComment(comment);
        task.setCompletionFilePath(filePath);
        task.setCompletionFileName(fileName);

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    // Fayl yükləmə
    private String saveFile(org.springframework.web.multipart.MultipartFile file) throws IOException {
        // Fayl uzantısını yoxla
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        boolean isValidExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                isValidExtension = true;
                break;
            }
        }

        if (!isValidExtension) {
            throw new RuntimeException("Bu fayl növü dəstəklənmir. Dəstəklənən formatlar: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Qovluğu yarat (əgər yoxdursa)
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Unikal fayl adı yarat
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        // Faylı yüklə
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    // Fayl yükləmə endpoint-i üçün
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadFile(String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(filePath.toFile());

            if (resource.exists() && resource.isReadable()) {
                return org.springframework.http.ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                throw new ResourceNotFoundException("Fayl tapılmadı: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Fayl yüklənərkən xəta baş verdi: " + e.getMessage());
        }
    }

    public ResponseEntity<List<OverdueTask>> overdueTasks() {
        log.info("get request overdue ...");
        List<Task> tasks = taskRepository.findOverdueTasks(Instant.now());

        List<OverdueTask> response = convertTaskListToOverdueTaskList.apply(tasks);

        return ResponseEntity.ok(response);
    }

    // Task -> OverdueTask
    private final Function<Task, OverdueTask> convertTaskToOverdueTask = task ->
            OverdueTask.builder()
                    .id(task.getId())
                    .assignedUser(task.getAssignedUser().getUsername())
                    .company(task.getCompany().getName())
                    .title(task.getTitle())
                    .status(task.getStatus())
                    .startedAt(task.getCreatedAt())
                    .dueDate(task.getDueDate())
                    .build();

    //  List<Task> -> List<OverdueTask>
    private final Function<List<Task>, List<OverdueTask>> convertTaskListToOverdueTaskList = tasks ->
            tasks.stream().map(convertTaskToOverdueTask).collect(Collectors.toList());


}
