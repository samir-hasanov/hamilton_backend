package www.hamilton.com.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import www.hamilton.com.entity.*;
import www.hamilton.com.repository.NotificationRepository;
import www.hamilton.com.repository.TaskRepository;
import www.hamilton.com.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Hər 5 dəqiqədə gecikmiş tapşırıqlar üçün bildiriş yaradın (ADMIN istifadəçilər üçün)
    @Scheduled(fixedDelay = 300_000)
    public void generateOverdueNotifications() {
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName())))
                .toList();

        List<Task> overdue = taskRepository.findOverdueTasks(Instant.now());

        for (User admin : admins) {
            for (Task task : overdue) {
                List<Notification> existing = notificationRepository.findByUserAndTask(admin, task.getId());
                if (existing.isEmpty()) {
                    Notification n = Notification.builder()
                            .user(admin)
                            .type(NotificationType.OVERDUE_TASK)
                            .title("Gecikmiş tapşırıq")
                            .message(buildOverdueMessage(task))
                            .relatedTaskId(task.getId())
                            .build();
                    notificationRepository.save(n);
                } else {
                    // Mövcud bildirişi güncəllə, oxunma statusunu olduğu kimi saxla
                    for (Notification n : existing) {
                        n.setMessage(buildOverdueMessage(task));
                        // n.setRead(n.isRead())  // no-op; kept for clarity
                    }
                    notificationRepository.saveAll(existing);
                }
            }
        }
    }

    private String buildOverdueMessage(Task task) {
        String company = task.getCompany() != null ? task.getCompany().getName() : "-";
        String assignee = task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : "-";
        Instant due = task.getDueDate();
        long days = 0;
        if (due != null) {
            days = ChronoUnit.DAYS.between(due, Instant.now());
            if (days < 0) days = 0;
        }
        return String.format("%s • %s • %d gün gecikib", company, assignee, days);
    }
}


