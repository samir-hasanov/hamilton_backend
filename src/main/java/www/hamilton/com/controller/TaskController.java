package www.hamilton.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import www.hamilton.com.dto.request.CreateTaskRequest;
import www.hamilton.com.dto.request.UpdateTaskStatusRequest;
import www.hamilton.com.dto.request.StartTaskRequest;
import www.hamilton.com.dto.response.OverdueTask;
import www.hamilton.com.dto.response.TaskResponse;
import www.hamilton.com.entity.TaskStatus;
import www.hamilton.com.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Tapşırıq idarəetmə API-ləri")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Yeni tapşırıq yarat", description = "Yeni tapşırıq yaratmaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @Operation(summary = "Bütün tapşırıqları al", description = "Sistemdəki bütün tapşırıqları siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @Operation(summary = "Səhifələnmiş tapşırıq siyahısı", description = "Böyük məlumatlar üçün səhifələnmiş tapşırıq siyahısı (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public ResponseEntity<Page<TaskResponse>> getAllTasksPage(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }

    @Operation(summary = "Mənim tapşırıqlarım", description = "Cari istifadəçinin tapşırıqlarını siyahıya almaq")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskResponse>> getMyTasks() {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ResponseEntity.ok(taskService.getTasksByUser(currentUsername));
    }

    @Operation(summary = "Səhifələnmiş mənim tapşırıqlarım", description = "Cari istifadəçinin səhifələnmiş tapşırıq siyahısı")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my-tasks/page")
    public ResponseEntity<Page<TaskResponse>> getMyTasksPage(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ResponseEntity.ok(taskService.getTasksByUser(currentUsername, pageable));
    }

    @Operation(summary = "İstifadəçiyə görə tapşırıqlar", description = "Müəyyən istifadəçinin tapşırıqlarını siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{username}")
    public ResponseEntity<List<TaskResponse>> getTasksByUser(@PathVariable String username) {
        return ResponseEntity.ok(taskService.getTasksByUser(username));
    }

    @Operation(summary = "Statusa görə tapşırıqlar", description = "Müəyyən statusa malik tapşırıqları siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }

    @Operation(summary = "Şirkətə görə tapşırıqlar", description = "Müəyyən şirkətə aid tapşırıqları siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<TaskResponse>> getTasksByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(taskService.getTasksByCompany(companyId));
    }

    @Operation(summary = "Tapşırıq məlumatlarını al", description = "ID-yə görə tapşırıq məlumatlarını almaq")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(summary = "Tapşırıq statusunu yenilə", description = "Tapşırıq statusunu yeniləmək")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, request));
    }

    @Operation(summary = "Tapşırığı istifadəçiyə təyin et", description = "Tapşırığı müəyyən istifadəçiyə təyin etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/assign/{username}")
    public ResponseEntity<TaskResponse> assignTaskToUser(
            @PathVariable Long id,
            @PathVariable String username
    ) {
        return ResponseEntity.ok(taskService.assignTaskToUser(id, username));
    }

    @Operation(summary = "Tapşırığı sil", description = "Tapşırığı silmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Tapşırığı başlat", description = "Tapşırığı başlatmaq (işçi üçün)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/start")
    public ResponseEntity<TaskResponse> startTask(
            @PathVariable Long id,
            @Valid @RequestBody StartTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.startTask(id, request));
    }

    @Operation(summary = "Tapşırığı tamamla", description = "Tapşırığı tamamlamaq və fayl yükləmək (işçi üçün)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/{id}/complete", consumes = "multipart/form-data")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable Long id,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(taskService.completeTask(id, comment, file));
    }

    @Operation(summary = "Fayl yüklə", description = "Tapşırıq faylını yükləmək")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/files/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable String fileName) {
        return taskService.downloadFile(fileName);
    }

    @Operation(summary = "Gecikmiş tapşırıqlar", description = "Müddəti keçmiş tapşırıqları siyahıya almaq")
    @GetMapping("/overdue")
    public ResponseEntity<List<OverdueTask>> overdueTasks() {
        return taskService.overdueTasks();
    }
}
