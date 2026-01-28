package www.hamilton.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import www.hamilton.com.dto.request.BulkTaskAssignmentRequest;
import www.hamilton.com.dto.response.*;
import www.hamilton.com.entity.*;
import www.hamilton.com.exception.ResourceNotFoundException;
import www.hamilton.com.repository.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduledTaskService {

    private final ScheduledTaskRepository scheduledTaskRepository;
    private final CompanyRepository companyRepository;
    private final TaskCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Çoxlu şirkətlərə vaxtlı tapşırıq təyin etmə
     */
    public BulkTaskAssignmentResponse assignBulkTasks(BulkTaskAssignmentRequest request, String scheduledBy) {
        List<String> errors = new ArrayList<>();
        List<ScheduledTaskResponse> scheduledTasks = new ArrayList<>();
        int successfulAssignments = 0;
        int failedAssignments = 0;

        // Kateqoriyanı tap
        TaskCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kateqoriya tapılmadı: " + request.getCategoryId()));

        // İstifadəçini tap (əgər təyin edilibsə)
        User assignedUser = null;
        if (request.getAssignedUsername() != null && !request.getAssignedUsername().trim().isEmpty()) {
            assignedUser = userRepository.findByUsername(request.getAssignedUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + request.getAssignedUsername()));
        }

        // İcra vaxtını hesabla
        Instant executionTime = calculateExecutionTime(request);

        // Hər şirkət üçün scheduled task yarat
        for (Long companyId : request.getCompanyIds()) {
            try {
                Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Şirkət tapılmadı: " + companyId));

                ScheduledTask scheduledTask = ScheduledTask.builder()
                        .title(request.getTitle() != null ? request.getTitle() : "Şirkət yoxlanışı: " + company.getName())
                        .description(request.getDescription() != null ? request.getDescription() : "Avtomatik təyin edilmiş tapşırıq")
                        .company(company)
                        .category(category)
                        .assignedUser(assignedUser)
                        .executionTime(executionTime)
                        .scheduledBy(scheduledBy)
                        .delayMinutes(request.getDelayMinutes())
                        .delayHours(request.getDelayHours())
                        .delayDays(request.getDelayDays())
                        .delayWeeks(request.getDelayWeeks())
                        .build();

                ScheduledTask savedTask = scheduledTaskRepository.save(scheduledTask);
                scheduledTasks.add(mapToResponse(savedTask));
                successfulAssignments++;

                log.info("Scheduled task yaradıldı: {} üçün {} vaxtında", company.getName(), executionTime);

            } catch (Exception e) {
                String error = String.format("Şirkət ID %d üçün xəta: %s", companyId, e.getMessage());
                errors.add(error);
                failedAssignments++;
                log.error(error, e);
            }
        }

        String delayDescription = generateDelayDescription(request);
        String message = String.format("%d şirkət üçün tapşırıq təyin edildi. %s", 
                successfulAssignments, delayDescription);

        return BulkTaskAssignmentResponse.builder()
                .totalCompanies(request.getCompanyIds().size())
                .successfulAssignments(successfulAssignments)
                .failedAssignments(failedAssignments)
                .errors(errors)
                .scheduledTasks(scheduledTasks)
                .message(message)
                .executionTime(executionTime.toString())
                .delayDescription(delayDescription)
                .build();
    }

    /**
     * İcra vaxtını hesabla
     */
    private Instant calculateExecutionTime(BulkTaskAssignmentRequest request) {
        Instant now = Instant.now();

        // Əgər dəqiq tarix verilibsə
        if (request.getExecutionDate() != null && !request.getExecutionDate().trim().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime localDateTime = LocalDateTime.parse(request.getExecutionDate(), formatter);
                return localDateTime.toInstant(ZoneOffset.UTC);
            } catch (Exception e) {
                log.warn("Tarix parse edilə bilmədi: {}, indiki vaxt istifadə olunur", request.getExecutionDate());
                return now;
            }
        }

        // Gecikmə vaxtlarını hesabla
        Duration delay = Duration.ZERO;
        
        if (request.getDelayWeeks() != null && request.getDelayWeeks() > 0) {
            delay = delay.plusDays(request.getDelayWeeks() * 7);
        }
        if (request.getDelayDays() != null && request.getDelayDays() > 0) {
            delay = delay.plusDays(request.getDelayDays());
        }
        if (request.getDelayHours() != null && request.getDelayHours() > 0) {
            delay = delay.plusHours(request.getDelayHours());
        }
        if (request.getDelayMinutes() != null && request.getDelayMinutes() > 0) {
            delay = delay.plusMinutes(request.getDelayMinutes());
        }

        return now.plus(delay);
    }

    /**
     * Gecikmə təsvirini yarat
     */
    private String generateDelayDescription(BulkTaskAssignmentRequest request) {
        if (request.getExecutionDate() != null && !request.getExecutionDate().trim().isEmpty()) {
            return "Dəqiq vaxt: " + request.getExecutionDate();
        }

        List<String> parts = new ArrayList<>();
        
        if (request.getDelayWeeks() != null && request.getDelayWeeks() > 0) {
            parts.add(request.getDelayWeeks() + " həftə");
        }
        if (request.getDelayDays() != null && request.getDelayDays() > 0) {
            parts.add(request.getDelayDays() + " gün");
        }
        if (request.getDelayHours() != null && request.getDelayHours() > 0) {
            parts.add(request.getDelayHours() + " saat");
        }
        if (request.getDelayMinutes() != null && request.getDelayMinutes() > 0) {
            parts.add(request.getDelayMinutes() + " dəqiqə");
        }

        if (parts.isEmpty()) {
            return "Dərhal icra";
        }

        return String.join(", ", parts) + " sonra";
    }

    /**
     * Hər dəqiqə icra olunur - vaxtı gəlmiş tapşırıqları aktivləşdir
     */
    @Scheduled(fixedRate = 60000) // Hər dəqiqə
    @Transactional
    public void executeScheduledTasks() {
        Instant now = Instant.now();
        List<ScheduledTask> pendingTasks = scheduledTaskRepository.findPendingTasksToExecute(now);

        if (pendingTasks.isEmpty()) {
            return;
        }

        log.info("{} ədəd scheduled task icra olunur", pendingTasks.size());

        for (ScheduledTask scheduledTask : pendingTasks) {
            try {
                // Scheduled task-i aktivləşdir
                scheduledTask.execute();
                scheduledTaskRepository.save(scheduledTask);

                // Normal task yarat
                Task task = Task.builder()
                        .title(scheduledTask.getTitle())
                        .description(scheduledTask.getDescription())
                        .company(scheduledTask.getCompany())
                        .category(scheduledTask.getCategory())
                        .assignedUser(scheduledTask.getAssignedUser())
                        .status(TaskStatus.ACTIVE)
                        .startedAt(Instant.now())
                        .build();

                taskRepository.save(task);

                log.info("Scheduled task icra olundu və normal task yaradıldı: {}", task.getTitle());

            } catch (Exception e) {
                scheduledTask.fail("İcra zamanı xəta: " + e.getMessage());
                scheduledTaskRepository.save(scheduledTask);
                log.error("Scheduled task icra olunarkən xəta: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Bütün scheduled task-ləri al
     */
    @Transactional(readOnly = true)
    public List<ScheduledTaskResponse> getAllScheduledTasks() {
        return scheduledTaskRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * İstifadəçiyə təyin edilmiş scheduled task-lər
     */
    @Transactional(readOnly = true)
    public List<ScheduledTaskResponse> getScheduledTasksByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + username));

        return scheduledTaskRepository.findByAssignedUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gələcək scheduled task-lər
     */
    @Transactional(readOnly = true)
    public List<ScheduledTaskResponse> getUpcomingScheduledTasks() {
        return scheduledTaskRepository.findUpcomingTasks(Instant.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Bugünkü scheduled task-lər
     */
    @Transactional(readOnly = true)
    public List<ScheduledTaskResponse> getTodayScheduledTasks() {
        return scheduledTaskRepository.findTodayTasks(Instant.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Scheduled task-i ləğv et
     */
    public void cancelScheduledTask(Long taskId) {
        ScheduledTask scheduledTask = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled task tapılmadı: " + taskId));

        if (scheduledTask.getStatus() != TaskStatus.PENDING) {
            throw new RuntimeException("Yalnız gözləyən tapşırıqlar ləğv edilə bilər");
        }

        scheduledTaskRepository.delete(scheduledTask);
        log.info("Scheduled task ləğv edildi: {}", scheduledTask.getTitle());
    }

    /**
     * Scheduled task-i response-a çevir
     */
    private ScheduledTaskResponse mapToResponse(ScheduledTask scheduledTask) {
        Instant now = Instant.now();
        boolean isOverdue = scheduledTask.getExecutionTime().isBefore(now) && scheduledTask.getStatus() == TaskStatus.PENDING;
        
        String timeUntilExecution = "";
        if (scheduledTask.getStatus() == TaskStatus.PENDING) {
            Duration duration = Duration.between(now, scheduledTask.getExecutionTime());
            if (duration.isNegative()) {
                timeUntilExecution = "Vaxtı keçib";
            } else {
                long days = duration.toDays();
                long hours = duration.toHoursPart();
                long minutes = duration.toMinutesPart();
                
                if (days > 0) {
                    timeUntilExecution = days + " gün " + hours + " saat " + minutes + " dəqiqə";
                } else if (hours > 0) {
                    timeUntilExecution = hours + " saat " + minutes + " dəqiqə";
                } else {
                    timeUntilExecution = minutes + " dəqiqə";
                }
            }
        }

        String statusText = switch (scheduledTask.getStatus()) {
            case PENDING -> "Gözləyir";
            case ACTIVE -> "Aktiv";
            case COMPLETED -> "Tamamlanmış";
            case FAILED -> "Uğursuz";
        };

        return ScheduledTaskResponse.builder()
                .id(scheduledTask.getId())
                .title(scheduledTask.getTitle())
                .description(scheduledTask.getDescription())
                .status(scheduledTask.getStatus())
                .assignedUser(scheduledTask.getAssignedUser() != null ? 
                        UserResponse.builder()
                                //.id(scheduledTask.getAssignedUser().getId())
                                .username(scheduledTask.getAssignedUser().getUsername())
                                .email(scheduledTask.getAssignedUser().getEmail())
                                .build() : null)
                .company(CompanyResponse.builder()
                        .id(scheduledTask.getCompany().getId())
                        .name(scheduledTask.getCompany().getName())
                        .taxNumber(scheduledTask.getCompany().getTaxNumber())
                        .build())
                .category(TaskCategoryResponse.builder()
                        .id(scheduledTask.getCategory().getId())
                        .name(scheduledTask.getCategory().getName())
                        .build())
                .executionTime(scheduledTask.getExecutionTime())
                .scheduledBy(scheduledTask.getScheduledBy())
                .delayMinutes(scheduledTask.getDelayMinutes())
                .delayHours(scheduledTask.getDelayHours())
                .delayDays(scheduledTask.getDelayDays())
                .delayWeeks(scheduledTask.getDelayWeeks())
                .createdAt(scheduledTask.getCreatedAt())
                .updatedAt(scheduledTask.getUpdatedAt())
                .executedAt(scheduledTask.getExecutedAt())
                .executionNotes(scheduledTask.getExecutionNotes())
                .timeUntilExecution(timeUntilExecution)
                .isOverdue(isOverdue)
                .statusText(statusText)
                .build();
    }
}
