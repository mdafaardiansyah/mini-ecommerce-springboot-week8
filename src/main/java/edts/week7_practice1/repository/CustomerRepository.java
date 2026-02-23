package edts.week7_practice1.repository;

import edts.week7_practice1.entity.Customer;
import edts.week7_practice1.enums.CustomerMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Customer entity using Native Queries as required by PRD.
 * All queries use specific column names instead of SELECT *
 * Count queries use COUNT(1) instead of COUNT(*)
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = "SELECT c.id, c.name, c.email, c.membership_level, c.total_spent, c.active, " +
                   "c.created_at, c.updated_at, c.created_by, c.updated_by " +
                   "FROM customers c WHERE c.id = :id AND c.active = TRUE", nativeQuery = true)
    Optional<Customer> findActiveById(@Param("id") Long id);

    @Query(value = "SELECT c.id, c.name, c.email, c.membership_level, c.total_spent, c.active, " +
                   "c.created_at, c.updated_at, c.created_by, c.updated_by " +
                   "FROM customers c WHERE c.email = :email AND c.active = TRUE", nativeQuery = true)
    Optional<Customer> findActiveByEmail(@Param("email") String email);

    @Query(value = "SELECT c.id, c.name, c.email, c.membership_level, c.total_spent, c.active, " +
                   "c.created_at, c.updated_at, c.created_by, c.updated_by " +
                   "FROM customers c WHERE c.active = TRUE " +
                   "ORDER BY c.id", nativeQuery = true)
    Page<Customer> findAllActive(Pageable pageable);

    @Query(value = "SELECT c.id, c.name, c.email, c.membership_level, c.total_spent, c.active, " +
                   "c.created_at, c.updated_at, c.created_by, c.updated_by " +
                   "FROM customers c WHERE c.active = TRUE AND c.membership_level = :level " +
                   "ORDER BY c.id", nativeQuery = true)
    Page<Customer> findActiveByMembershipLevel(@Param("level") CustomerMembership level, Pageable pageable);

    /**
     * Search customers by name or email (contains, case-insensitive)
     * SEPARATE from sorting - this is for SEARCHING only
     */
    @Query(value = "SELECT c.id, c.name, c.email, c.membership_level, c.total_spent, c.active, " +
                   "c.created_at, c.updated_at, c.created_by, c.updated_by " +
                   "FROM customers c WHERE c.active = TRUE " +
                   "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "ORDER BY c.name", nativeQuery = true)
    Page<Customer> searchActiveCustomers(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN TRUE ELSE FALSE END " +
                   "FROM customers c WHERE c.email = :email AND c.active = TRUE", nativeQuery = true)
    boolean existsActiveByEmail(@Param("email") String email);

    @Query(value = "SELECT c.id, c.name, c.email, c.membership_level, c.total_spent, c.active, " +
                   "c.created_at, c.updated_at, c.created_by, c.updated_by " +
                   "FROM customers c WHERE c.active = TRUE " +
                   "ORDER BY c.total_spent DESC", nativeQuery = true)
    Page<Customer> findTopSpenders(Pageable pageable);

    @Query(value = "SELECT COUNT(1) FROM customers WHERE active = TRUE", nativeQuery = true)
    long countActive();

    @Query(value = "SELECT COUNT(1) FROM customers WHERE email = :email AND active = TRUE", nativeQuery = true)
    long countActiveByEmail(@Param("email") String email);
}
