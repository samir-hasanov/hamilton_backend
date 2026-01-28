package www.hamilton.com.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkTaskAssignmentResponse {

    private int totalCompanies;
    private int successfulAssignments;
    private int failedAssignments;
    private List<String> errors;
    private List<ScheduledTaskResponse> scheduledTasks;
    private String message;
    
    // Vaxt məlumatları
    private String executionTime;
    private String delayDescription;
}
