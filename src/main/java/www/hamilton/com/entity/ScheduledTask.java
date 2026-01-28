package www.hamilton.com.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import www.hamilton.com.serializer.InstantSerializer;

import java.time.Instant;

@Entity
@Table(name = "lombard_scheduled_tasks", indexes = {
    @Index(name = "idx_scheduled_tasks_execution_time", columnList = "execution_time"),
    @Index(name = "idx_scheduled_tasks_status", columnList = "status"),
    @Index(name = "idx_scheduled_tasks_created", columnList = "created_at"),
    @Index(name = "idx_scheduled_tasks_assigned_user", columnList = "assigned_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TaskCategory category;

    @Column(name = "execution_time", nullable = false)
    @JsonSerialize(using = InstantSerializer.class)
    private Instant executionTime;

    @Column(name = "scheduled_by")
    private String scheduledBy; // Kim təyin etdi

    @Column(name = "delay_minutes")
    private Long delayMinutes; // Dəqiqə ilə gecikmə

    @Column(name = "delay_hours")
    private Long delayHours; // Saat ilə gecikmə

    @Column(name = "delay_days")
    private Long delayDays; // Gün ilə gecikmə

    @Column(name = "delay_weeks")
    private Long delayWeeks; // Həftə ilə gecikmə

    @Column(name = "created_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;

    @Column(name = "updated_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;

    @Column(name = "executed_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant executedAt;

    @Column(name = "execution_notes", length = 1000)
    private String executionNotes;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void execute() {
        this.status = TaskStatus.ACTIVE;
        this.executedAt = Instant.now();
    }

    public void complete() {
        this.status = TaskStatus.COMPLETED;
    }

    public void fail(String notes) {
        this.status = TaskStatus.FAILED;
        this.executionNotes = notes;
    }
}
