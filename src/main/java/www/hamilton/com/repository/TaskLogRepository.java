package www.hamilton.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import www.hamilton.com.entity.TaskLog;
import www.hamilton.com.entity.Task;
import www.hamilton.com.entity.User;

import java.time.Instant;
import java.util.List;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
    
    // Tapşırığın bütün log-ları
    List<TaskLog> findByTaskOrderByCreatedAtDesc(Task task);
    
    // İstifadəçinin etdiyi dəyişikliklər
    List<TaskLog> findByUserOrderByCreatedAtDesc(User user);
    
    // Tarix aralığında dəyişikliklər
    @Query("SELECT tl FROM TaskLog tl WHERE tl.createdAt BETWEEN :startDate AND :endDate")
    List<TaskLog> findByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
    
    // Tapşırığın son dəyişikliyi
    @Query("SELECT tl FROM TaskLog tl WHERE tl.task = :task ORDER BY tl.createdAt DESC LIMIT 1")
    TaskLog findLatestByTask(@Param("task") Task task);
    
    // İstifadəçinin son dəyişiklikləri
    @Query("SELECT tl FROM TaskLog tl WHERE tl.user = :user ORDER BY tl.createdAt DESC LIMIT 10")
    List<TaskLog> findLatestByUser(@Param("user") User user);
}
