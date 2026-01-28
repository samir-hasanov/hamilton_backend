package www.hamilton.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import www.hamilton.com.entity.TaskCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {
    
    Optional<TaskCategory> findByName(String name);
    
    List<TaskCategory> findByNameContainingIgnoreCase(String name);
    
    boolean existsByName(String name);
}
