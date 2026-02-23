package edts.week7_practice1.repository;

import edts.week7_practice1.entity.Product;
import edts.week7_practice1.enums.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Product entity using Native Queries as required by PRD.
 * All queries use specific column names instead of SELECT *
 * Count queries use COUNT(1) instead of COUNT(*)
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p.id, p.name, p.category, p.price, p.stock, p.active, " +
                   "p.created_at, p.updated_at, p.created_by, p.updated_by " +
                   "FROM products p WHERE p.id = :id AND p.active = TRUE", nativeQuery = true)
    Optional<Product> findActiveById(@Param("id") Long id);

    @Query(value = "SELECT p.id, p.name, p.category, p.price, p.stock, p.active, " +
                   "p.created_at, p.updated_at, p.created_by, p.updated_by " +
                   "FROM products p WHERE p.name = :name AND p.active = TRUE", nativeQuery = true)
    Optional<Product> findActiveByName(@Param("name") String name);

    @Query(value = "SELECT p.id, p.name, p.category, p.price, p.stock, p.active, " +
                   "p.created_at, p.updated_at, p.created_by, p.updated_by " +
                   "FROM products p WHERE p.active = TRUE AND p.category = :category " +
                   "ORDER BY p.id", nativeQuery = true)
    Page<Product> findActiveByCategory(@Param("category") ProductCategory category, Pageable pageable);

    @Query(value = "SELECT p.id, p.name, p.category, p.price, p.stock, p.active, " +
                   "p.created_at, p.updated_at, p.created_by, p.updated_by " +
                   "FROM products p WHERE p.active = TRUE " +
                   "ORDER BY p.id", nativeQuery = true)
    Page<Product> findAllActive(Pageable pageable);

    /**
     * Search products by name (contains, case-insensitive)
     * SEPARATE from sorting - this is for SEARCHING only
     */
    @Query(value = "SELECT p.id, p.name, p.category, p.price, p.stock, p.active, " +
                   "p.created_at, p.updated_at, p.created_by, p.updated_by " +
                   "FROM products p WHERE p.active = TRUE " +
                   "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "ORDER BY p.name", nativeQuery = true)
    Page<Product> searchActiveProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN TRUE ELSE FALSE END " +
                   "FROM products p WHERE p.id = :id AND p.active = TRUE AND p.stock > 0", nativeQuery = true)
    boolean existsActiveWithStock(@Param("id") Long id);

    @Query(value = "SELECT COUNT(1) FROM products p WHERE p.active = TRUE AND p.stock < :threshold", nativeQuery = true)
    long countLowStockProducts(@Param("threshold") Integer threshold);

    @Query(value = "SELECT p.id, p.name, p.category, p.price, p.stock, p.active, " +
                   "p.created_at, p.updated_at, p.created_by, p.updated_by " +
                   "FROM products p WHERE p.active = TRUE AND p.stock < :threshold " +
                   "ORDER BY p.stock ASC", nativeQuery = true)
    Page<Product> findLowStockProducts(@Param("threshold") Integer threshold, Pageable pageable);

    @Query(value = "SELECT COUNT(1) FROM products WHERE active = TRUE", nativeQuery = true)
    long countActive();

    @Query(value = "SELECT COUNT(1) FROM products WHERE name = :name AND active = TRUE", nativeQuery = true)
    long countActiveByName(@Param("name") String name);
}
