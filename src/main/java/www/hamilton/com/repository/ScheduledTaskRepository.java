package www.hamilton.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import www.hamilton.com.entity.ScheduledTask;
import www.hamilton.com.entity.TaskStatus;
import www.hamilton.com.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    
    // İcra vaxtı gəlmiş tapşırıqları tap
    @Query("SELECT st FROM ScheduledTask st WHERE st.executionTime <= :now AND st.status = 'PENDING'")
    List<ScheduledTask> findPendingTasksToExecute(@Param("now") Instant now);
    
    // İstifadəçiyə təyin edilmiş scheduled tapşırıqlar
    List<ScheduledTask> findByAssignedUser(User assignedUser);
    
    // Status-a görə tapşırıqlar
    List<ScheduledTask> findByStatus(TaskStatus status);
    
    // Şirkətə görə scheduled tapşırıqlar
    List<ScheduledTask> findByCompanyId(Long companyId);
    
    // İstifadəçiyə və status-a görə
    List<ScheduledTask> findByAssignedUserAndStatus(User assignedUser, TaskStatus status);
    
    // Gələcək tapşırıqlar (execution time > now)
    @Query("SELECT st FROM ScheduledTask st WHERE st.executionTime > :now ORDER BY st.executionTime ASC")
    List<ScheduledTask> findUpcomingTasks(@Param("now") Instant now);
    
    // Keçmiş tapşırıqlar (execution time <= now)
    @Query("SELECT st FROM ScheduledTask st WHERE st.executionTime <= :now ORDER BY st.executionTime DESC")
    List<ScheduledTask> findPastTasks(@Param("now") Instant now);
    
    // Bugünkü tapşırıqlar
    @Query("SELECT st FROM ScheduledTask st WHERE DATE(st.executionTime) = DATE(:today)")
    List<ScheduledTask> findTodayTasks(@Param("today") Instant today);
    
    // Bu həftəki tapşırıqlar
    @Query("SELECT st FROM ScheduledTask st WHERE st.executionTime >= :weekStart AND st.executionTime <= :weekEnd")
    List<ScheduledTask> findThisWeekTasks(@Param("weekStart") Instant weekStart, @Param("weekEnd") Instant weekEnd);
}
