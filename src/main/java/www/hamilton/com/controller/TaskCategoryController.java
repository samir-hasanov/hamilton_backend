package www.hamilton.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import www.hamilton.com.dto.request.CreateTaskCategoryRequest;
import www.hamilton.com.dto.response.TaskCategoryResponse;
import www.hamilton.com.entity.TaskCategory;
import www.hamilton.com.exception.ResourceNotFoundException;
import www.hamilton.com.repository.TaskCategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/task-categories")
@RequiredArgsConstructor
@Tag(name = "Task Categories", description = "Tapşırıq kateqoriyası idarəetmə API-ləri")
public class TaskCategoryController {

    private final TaskCategoryRepository taskCategoryRepository;

    @Operation(summary = "Yeni kateqoriya yarat", description = "Yeni tapşırıq kateqoriyası yaratmaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TaskCategoryResponse> createCategory(@Valid @RequestBody CreateTaskCategoryRequest request) {
        TaskCategory category = TaskCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        TaskCategory savedCategory = taskCategoryRepository.save(category);
        return ResponseEntity.ok(mapToResponse(savedCategory));
    }

    @Operation(summary = "Bütün kateqoriyaları al", description = "Sistemdəki bütün tapşırıq kateqoriyalarını siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TaskCategoryResponse>> getAllCategories() {
        List<TaskCategoryResponse> categories = taskCategoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Kateqoriya məlumatlarını al", description = "ID-yə görə kateqoriya məlumatlarını almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskCategoryResponse> getCategoryById(@PathVariable Long id) {
        TaskCategory category = taskCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kateqoriya tapılmadı: " + id));
        return ResponseEntity.ok(mapToResponse(category));
    }

    @Operation(summary = "Kateqoriya məlumatlarını yenilə", description = "Kateqoriya məlumatlarını yeniləmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TaskCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateTaskCategoryRequest request
    ) {
        TaskCategory category = taskCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kateqoriya tapılmadı: " + id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        TaskCategory updatedCategory = taskCategoryRepository.save(category);
        return ResponseEntity.ok(mapToResponse(updatedCategory));
    }

    @Operation(summary = "Kateqoriyanı sil", description = "Kateqoriyanı silmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!taskCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Kateqoriya tapılmadı: " + id);
        }
        taskCategoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private TaskCategoryResponse mapToResponse(TaskCategory category) {
        return TaskCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
