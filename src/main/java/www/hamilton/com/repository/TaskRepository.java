package www.hamilton.com.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import www.hamilton.com.entity.Task;
import www.hamilton.com.entity.TaskStatus;
import www.hamilton.com.entity.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // İstifadəçiyə təyin olunmuş tapşırıqlar
    List<Task> findByAssignedUser(User assignedUser);
    
    // Status-a görə tapşırıqlar
    List<Task> findByStatus(TaskStatus status);
    
    // İstifadəçiyə və status-a görə tapşırıqlar
    List<Task> findByAssignedUserAndStatus(User assignedUser, TaskStatus status);
    
    // Şirkətə görə tapşırıqlar
    List<Task> findByCompanyId(Long companyId);
    
    // Şirkətə və status-a görə tapşırıqlar
    List<Task> findByCompanyIdAndStatus(Long companyId, TaskStatus status);
    
    // Şirkətə görə tapşırıq sayı
    long countByCompanyId(Long companyId);
    
    // Şirkətə və status-a görə tapşırıq sayı
    long countByCompanyIdAndStatus(Long companyId, TaskStatus status);
    
    // Kateqoriyaya görə tapşırıqlar
    List<Task> findByCategoryId(Long categoryId);
    
    // Tarix aralığında yaradılmış tapşırıqlar
    @Query("SELECT t FROM Task t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Task> findByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
    
    // İstifadəçinin tamamladığı tapşırıqlar
    @Query("SELECT t FROM Task t WHERE t.assignedUser = :user AND t.status = 'COMPLETED'")
    List<Task> findCompletedTasksByUser(@Param("user") User user);
    
    // Gecikmiş tapşırıqlar
//    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != 'COMPLETED'")
//    List<Task> findOverdueTasks(@Param("now") Instant now);
    
    // Performans statistikası üçün
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser = :user AND t.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") TaskStatus status);
    
    // Gündəlik tamamlanmış tapşırıqlar
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser = :user AND t.status = 'COMPLETED' AND DATE(t.completedAt) = DATE(:date)")
    long countCompletedTasksByUserAndDate(@Param("user") User user, @Param("date") Instant date);
    
    // Pageable sorğular
    Page<Task> findByAssignedUser(User assignedUser, Pageable pageable);
    
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    
    Page<Task> findByCompanyId(Long companyId, Pageable pageable);
    
    // Axtarış üçün
    @Query("SELECT t FROM Task t WHERE t.title LIKE %:searchTerm% OR t.description LIKE %:searchTerm%")
    List<Task> findByTitleOrDescriptionContaining(@Param("searchTerm") String searchTerm);


    // Geciken senedler
    @Query("SELECT t FROM Task t WHERE t.dueDate IS NOT NULL AND t.dueDate < :currentDate AND t.status IN ('PENDING', 'ACTIVE') ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(Instant currentDate);
}
