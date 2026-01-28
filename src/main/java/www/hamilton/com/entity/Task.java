package www.hamilton.com.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import www.hamilton.com.serializer.InstantSerializer;

import java.time.Instant;

@Entity
@Table(name = "lombard_tasks", indexes = {
    @Index(name = "idx_tasks_status", columnList = "status"),
    @Index(name = "idx_tasks_assigned_user", columnList = "assigned_user_id"),
    @Index(name = "idx_tasks_company", columnList = "company_id"),
    @Index(name = "idx_tasks_category", columnList = "category_id"),
    @Index(name = "idx_tasks_created", columnList = "created_at"),
    @Index(name = "idx_tasks_due_date", columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

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

    @Column(name = "due_date")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant dueDate;

    @Column(name = "started_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant startedAt;

    @Column(name = "completed_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant completedAt;

    @Column(name = "created_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;

    @Column(name = "updated_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;

    // İşçi şərhləri və fayllar
    @Column(name = "worker_comment", length = 2000)
    private String workerComment;

    @Column(name = "completion_comment", length = 2000)
    private String completionComment;

    @Column(name = "completion_file_path", length = 500)
    private String completionFilePath;

    @Column(name = "completion_file_name", length = 200)
    private String completionFileName;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void startTask() {
        this.status = TaskStatus.ACTIVE;
        this.startedAt = Instant.now();
    }

    public void completeTask() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = Instant.now();
    }
}
