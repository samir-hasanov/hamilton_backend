package www.hamilton.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import www.hamilton.com.dto.request.BulkTaskAssignmentRequest;
import www.hamilton.com.dto.response.BulkTaskAssignmentResponse;
import www.hamilton.com.dto.response.ScheduledTaskResponse;
import www.hamilton.com.service.ScheduledTaskService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scheduled-tasks")
@RequiredArgsConstructor
@Tag(name = "Scheduled Tasks", description = "Planlaşdırılmış tapşırıq idarəetmə API-ləri")
public class ScheduledTaskController {

    private final ScheduledTaskService scheduledTaskService;

    @Operation(summary = "Çoxlu şirkətlərə vaxtlı tapşırıq təyin et", description = "Çoxlu şirkətlərə vaxtlı tapşırıq təyin etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-assign")
    public ResponseEntity<BulkTaskAssignmentResponse> assignBulkTasks(
            @Valid @RequestBody BulkTaskAssignmentRequest request,
            Authentication authentication
    ) {
        String scheduledBy = authentication.getName();
        BulkTaskAssignmentResponse response = scheduledTaskService.assignBulkTasks(request, scheduledBy);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Bütün planlaşdırılmış tapşırıqları al", description = "Sistemdəki bütün planlaşdırılmış tapşırıqları siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ScheduledTaskResponse>> getAllScheduledTasks() {
        return ResponseEntity.ok(scheduledTaskService.getAllScheduledTasks());
    }

    @Operation(summary = "İstifadəçiyə təyin edilmiş planlaşdırılmış tapşırıqlar", description = "Müəyyən istifadəçiyə təyin edilmiş planlaşdırılmış tapşırıqları siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{username}")
    public ResponseEntity<List<ScheduledTaskResponse>> getScheduledTasksByUser(@PathVariable String username) {
        return ResponseEntity.ok(scheduledTaskService.getScheduledTasksByUser(username));
    }

    @Operation(summary = "Gələcək planlaşdırılmış tapşırıqlar", description = "Gələcəkdə planlaşdırılmış tapşırıqları siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/upcoming")
    public ResponseEntity<List<ScheduledTaskResponse>> getUpcomingScheduledTasks() {
        return ResponseEntity.ok(scheduledTaskService.getUpcomingScheduledTasks());
    }

    @Operation(summary = "Bugünkü planlaşdırılmış tapşırıqlar", description = "Bugün planlaşdırılmış tapşırıqları siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/today")
    public ResponseEntity<List<ScheduledTaskResponse>> getTodayScheduledTasks() {
        return ResponseEntity.ok(scheduledTaskService.getTodayScheduledTasks());
    }

    @Operation(summary = "Planlaşdırılmış tapşırığı ləğv et", description = "Planlaşdırılmış tapşırığı ləğv etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> cancelScheduledTask(@PathVariable Long taskId) {
        scheduledTaskService.cancelScheduledTask(taskId);
        return ResponseEntity.ok().build();
    }
}
