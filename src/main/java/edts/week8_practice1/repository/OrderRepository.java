package edts.week8_practice1.repository;

import edts.week8_practice1.entity.Order;
import edts.week8_practice1.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity using Native Queries as required by PRD.
 * All queries use specific column names instead of SELECT *
 * Count queries use COUNT(1) instead of COUNT(*)
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT o.id, o.customer_id, o.total_amount, o.discount_amount, o.final_amount, " +
                   "o.status, o.created_at, o.updated_at, o.created_by, o.updated_by " +
                   "FROM orders o WHERE o.id = :id", nativeQuery = true)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT o.id, o.customer_id, o.total_amount, o.discount_amount, o.final_amount, " +
                   "o.status, o.created_at, o.updated_at, o.created_by, o.updated_by " +
                   "FROM orders o WHERE o.status = :status " +
                   "ORDER BY o.created_at DESC", nativeQuery = true)
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

    @Query(value = "SELECT o.id, o.customer_id, o.total_amount, o.discount_amount, o.final_amount, " +
                   "o.status, o.created_at, o.updated_at, o.created_by, o.updated_by " +
                   "FROM orders o WHERE o.customer_id = :customerId AND o.status = :status " +
                   "ORDER BY o.created_at DESC", nativeQuery = true)
    Page<Order> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                          @Param("status") OrderStatus status, Pageable pageable);

    @Query(value = "SELECT o.id, o.customer_id, o.total_amount, o.discount_amount, o.final_amount, " +
                   "o.status, o.created_at, o.updated_at, o.created_by, o.updated_by " +
                   "FROM orders o WHERE o.created_at BETWEEN :startDate AND :endDate " +
                   "ORDER BY o.created_at DESC", nativeQuery = true)
    Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query(value = "SELECT o.id, o.customer_id, o.total_amount, o.discount_amount, o.final_amount, " +
                   "o.status, o.created_at, o.updated_at, o.created_by, o.updated_by " +
                   "FROM orders o WHERE o.customer_id = :customerId " +
                   "AND o.created_at BETWEEN :startDate AND :endDate " +
                   "ORDER BY o.created_at DESC", nativeQuery = true)
    Page<Order> findByCustomerIdAndDateRange(@Param("customerId") Long customerId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query(value = "SELECT oi.product_id FROM order_items oi WHERE oi.order_id = :orderId", nativeQuery = true)
    List<Long> findProductIdsByOrderId(@Param("orderId") Long orderId);

    @Query(value = "SELECT COUNT(1) FROM orders o WHERE o.customer_id = :customerId AND o.status = :status", nativeQuery = true)
    long countByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                     @Param("status") OrderStatus status);

    @Query(value = "SELECT o.id, o.customer_id, o.total_amount, o.discount_amount, o.final_amount, " +
                   "o.status, o.created_at, o.updated_at, o.created_by, o.updated_by " +
                   "FROM orders o " +
                   "ORDER BY o.created_at DESC", nativeQuery = true)
    Page<Order> findAll(Pageable pageable);

    @Query(value = "SELECT o.id, o.customer_id, o.total_amount, o.discount_amount, o.final_amount, " +
                   "o.status, o.created_at, o.updated_at, o.created_by, o.updated_by " +
                   "FROM orders o WHERE o.customer_id = :customerId " +
                   "ORDER BY o.created_at DESC", nativeQuery = true)
    Page<Order> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(o.final_amount), 0) FROM orders o " +
                   "WHERE o.customer_id = :customerId AND o.status = 'PAID'", nativeQuery = true)
    BigDecimal calculateTotalSpentByCustomer(@Param("customerId") Long customerId);

    @Query(value = "SELECT COUNT(1) " +
                   "FROM order_items oi " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE oi.product_id = :productId AND o.status = :status", nativeQuery = true)
    long countByProductIdAndOrderStatus(@Param("productId") Long productId, @Param("status") OrderStatus status);

    @Query(value = "SELECT COUNT(1) FROM orders", nativeQuery = true)
    long countAll();

    @Query(value = "SELECT COUNT(1) FROM orders WHERE status = :status", nativeQuery = true)
    long countByStatus(@Param("status") OrderStatus status);
}
