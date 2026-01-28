package www.hamilton.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import www.hamilton.com.entity.Company;
import www.hamilton.com.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findByName(String name);
    
    List<Company> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Company c WHERE c.taxNumber = :taxNumber")
    Optional<Company> findByTaxNumber(@Param("taxNumber") String taxNumber);
    
    // VOEN üzrə axtarış (qismən uyğunluq)
    List<Company> findByTaxNumberContaining(String taxNumber);
    
    // Son yoxlanış tarixinə görə sıralama - əvvəlcə tarixi olmayanlar, sonra tarixə görə
    @Query("SELECT c FROM Company c ORDER BY CASE WHEN c.lastCheckDate IS NULL THEN 0 ELSE 1 END, c.lastCheckDate ASC")
    List<Company> findAllOrderByLastCheckDate();
    
    // VOEN üzrə axtarış və son yoxlanış tarixinə görə sıralama
    @Query("SELECT c FROM Company c WHERE c.taxNumber LIKE %:taxNumber% ORDER BY CASE WHEN c.lastCheckDate IS NULL THEN 0 ELSE 1 END, c.lastCheckDate ASC")
    List<Company> findByTaxNumberContainingOrderByLastCheckDate(@Param("taxNumber") String taxNumber);
    
    boolean existsByName(String name);
    
    boolean existsByTaxNumber(String taxNumber);

    // Assignment/visibility queries
    List<Company> findByAssignedUser(User user);

    @Query("SELECT c FROM Company c WHERE c.isPublic = true")
    List<Company> findAllPublic();
}
