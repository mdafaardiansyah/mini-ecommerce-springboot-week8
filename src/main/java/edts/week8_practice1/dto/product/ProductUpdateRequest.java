package edts.week8_practice1.dto.product;

import edts.week8_practice1.enums.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductUpdateRequest {

    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;

    private ProductCategory category;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private Boolean active;

    // Constructors
    public ProductUpdateRequest() {
    }

    public ProductUpdateRequest(String name, ProductCategory category, BigDecimal price, Integer stock, Boolean active) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.active = active;
    }

    // Getters and Setters
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "ProductUpdateRequest{" +
                "name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", stock=" + stock +
                ", active=" + active +
                '}';
    }
}
