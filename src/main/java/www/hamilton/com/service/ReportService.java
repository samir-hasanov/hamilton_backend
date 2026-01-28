package www.hamilton.com.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import www.hamilton.com.dto.response.DashboardStatsResponse;
import www.hamilton.com.entity.Task;
import www.hamilton.com.entity.TaskStatus;
import www.hamilton.com.entity.User;
import www.hamilton.com.repository.TaskRepository;
import www.hamilton.com.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public List<DashboardStatsResponse.PerformanceData> getUserPerformance() {
        Instant now = Instant.now();
        return userRepository.findAll().stream()
                // yalnız işçilər: ADMIN rolu olanlar istisna edilir
                .filter(user -> user.getRoles() == null || user.getRoles().stream().noneMatch(r -> "ADMIN".equalsIgnoreCase(r.getName())))
                .map(user -> buildPerformance(user, now))
                .sorted(Comparator.comparing(DashboardStatsResponse.PerformanceData::getCompletedTasks).reversed())
                .collect(Collectors.toList());
    }

    private DashboardStatsResponse.PerformanceData buildPerformance(User user, Instant now) {
        List<Task> tasks = taskRepository.findByAssignedUser(user);

        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long active = tasks.stream().filter(t -> t.getStatus() == TaskStatus.ACTIVE).count();
        long overdue = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now) && t.getStatus() != TaskStatus.COMPLETED).count();

        // Average completion time in days
        double avgDays = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED && t.getCompletedAt() != null)
                .mapToDouble(t -> {
                    Instant start = t.getStartedAt() != null ? t.getStartedAt() : (t.getCreatedAt() != null ? t.getCreatedAt() : t.getCompletedAt());
                    long minutes = ChronoUnit.MINUTES.between(start, t.getCompletedAt());
                    return minutes / 1440.0; // minutes to days
                })
                .average()
                .orElse(0.0);

        return DashboardStatsResponse.PerformanceData.builder()
                .username(user.getUsername())
                .completedTasks(completed)
                .activeTasks(active)
                .overdueTasks(overdue)
                .averageCompletionTime(avgDays)
                .build();
    }
}


