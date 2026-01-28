package www.hamilton.com.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {

    private Long totalTasks;
    private Long activeTasks;
    private Long completedTasks;
    private Long overdueTasks;
    private Long totalCompanies;
    private Long totalUsers;

    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByCategory;
    private Map<String, Long> tasksByCompany;

    private List<PerformanceData> userPerformance;
    private List<RecentActivity> recentActivities;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerformanceData {
        private String username;
        private Long completedTasks;
        private Long activeTasks;
        private Long overdueTasks;
        private Double averageCompletionTime; // günlərdə
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentActivity {
        private String type; // TASK_CREATED, TASK_STARTED, TASK_COMPLETED
        private String description;
        private String username;
        private String taskTitle;
        private String companyName;
        private String timestamp;
    }
}
