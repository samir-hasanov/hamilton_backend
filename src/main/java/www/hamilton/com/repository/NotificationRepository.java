package www.hamilton.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import www.hamilton.com.entity.Notification;
import www.hamilton.com.entity.User;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsRead(User user, boolean isRead);

    long countByUserAndIsRead(User user, boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.relatedTaskId = :taskId")
    List<Notification> findByUserAndTask(@Param("user") User user, @Param("taskId") Long taskId);
}


