package www.hamilton.com.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import www.hamilton.com.entity.TaskStatus;
import www.hamilton.com.serializer.InstantSerializer;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    
    private UserResponse assignedUser;
    private CompanyResponse company;
    private TaskCategoryResponse category;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant dueDate;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant startedAt;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant completedAt;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;

    // İşçi şərhləri və fayllar
    private String workerComment;
    private String completionComment;
    private String completionFilePath;
    private String completionFileName;

    private boolean isOverdue;
}
