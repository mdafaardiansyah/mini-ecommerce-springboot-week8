package edts.week8_practice1.entity;

import edts.week8_practice1.enums.ProductCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Product extends BaseEntityWithSoftDelete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ProductCategory category;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    // Constructors
    public Product() {
        super();
        setActive(true);
        this.stock = 0;
    }

    public Product(String name, ProductCategory category, BigDecimal price, Integer stock) {
        this();
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    // Business logic helpers
    public boolean hasSufficientStock(Integer requestedQuantity) {
        return this.stock >= requestedQuantity;
    }

    public void reduceStock(Integer quantity) {
        if (!hasSufficientStock(quantity)) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock. Requested: %d, Available: %d", quantity, this.stock)
            );
        }
        this.stock -= quantity;
    }

    public void addStock(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stock += quantity;
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) &&
                Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", stock=" + stock +
                ", active=" + getActive() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
