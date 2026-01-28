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
public class ScheduledTaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    
    private UserResponse assignedUser;
    private CompanyResponse company;
    private TaskCategoryResponse category;
    
    @JsonSerialize(using = InstantSerializer.class)
    private Instant executionTime;
    
    private String scheduledBy;
    private Long delayMinutes;
    private Long delayHours;
    private Long delayDays;
    private Long delayWeeks;
    
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;
    
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;
    
    @JsonSerialize(using = InstantSerializer.class)
    private Instant executedAt;
    
    private String executionNotes;
    
    // Əlavə məlumatlar
    private String timeUntilExecution; // İcra vaxtına qədər qalan vaxt
    private boolean isOverdue; // Vaxtı keçibmi
    private String statusText; // Status mətn şəklində
}
