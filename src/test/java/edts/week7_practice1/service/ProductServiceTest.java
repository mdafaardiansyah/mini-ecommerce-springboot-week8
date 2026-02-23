package edts.week7_practice1.service;

import edts.week7_practice1.dto.product.ProductRequest;
import edts.week7_practice1.dto.product.ProductResponse;
import edts.week7_practice1.dto.product.ProductUpdateRequest;
import edts.week7_practice1.entity.Product;
import edts.week7_practice1.enums.ProductCategory;
import edts.week7_practice1.exception.BusinessException;
import edts.week7_practice1.repository.OrderRepository;
import edts.week7_practice1.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setCategory(ProductCategory.ELECTRONICS);
        product.setPrice(new BigDecimal("500000"));
        product.setStock(100);
        product.setActive(true);

        productRequest = new ProductRequest("Test Product", ProductCategory.ELECTRONICS,
                new BigDecimal("500000"), 100);
    }

    @Test
    @DisplayName("Should create product successfully with valid data")
    void createProduct_Success() {
        // Given
        when(productRepository.findActiveByName(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.createProduct(productRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
        assertThat(response.getPrice()).isEqualByComparingTo("500000");
        assertThat(response.getStock()).isEqualTo(100);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when creating FOOD product with price > 1,000,000")
    void createProduct_FoodPriceTooHigh_ThrowsException() {
        // Given
        ProductRequest foodRequest = new ProductRequest("Food Item", ProductCategory.FOOD,
                new BigDecimal("2000000"), 50);

        // When/Then
        assertThatThrownBy(() -> productService.createProduct(foodRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Food products cannot exceed price");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when creating product with negative stock")
    void createProduct_NegativeStock_ThrowsException() {
        // Given
        ProductRequest invalidRequest = new ProductRequest("Test", ProductCategory.ELECTRONICS,
                new BigDecimal("100000"), -5);

        // When/Then
        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock cannot be negative");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when creating product with price <= 0")
    void createProduct_ZeroPrice_ThrowsException() {
        // Given
        ProductRequest invalidRequest = new ProductRequest("Test", ProductCategory.ELECTRONICS,
                BigDecimal.ZERO, 10);

        // When/Then
        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Price must be greater than 0");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product successfully when no completed orders exist")
    void updateProduct_Success() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest("Updated Name",
                ProductCategory.FASHION, new BigDecimal("300000"), 50, true);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.countByProductIdAndOrderStatus(1L,
                edts.week7_practice1.enums.OrderStatus.PAID)).thenReturn(0L);
        when(productRepository.findActiveByName("Updated Name")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating price for product with completed orders")
    void updateProduct_HasCompletedOrders_ThrowsException() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null, null,
                new BigDecimal("600000"), null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.countByProductIdAndOrderStatus(1L,
                edts.week7_practice1.enums.OrderStatus.PAID)).thenReturn(1L);

        // When/Then
        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot update price for product with completed orders");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should soft delete product successfully when stock is 0")
    void deleteProduct_Success() {
        // Given
        product.setStock(0);
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.deleteProduct(1L);

        // Then
        assertThat(product.getActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw exception when deleting product with stock > 0")
    void deleteProduct_HasStock_ThrowsException() {
        // Given
        product.setStock(50);
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete product with stock");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should find all active products")
    void findAllActive_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.findAllActive(pageable)).thenReturn(productPage);

        // When
        Page<ProductResponse> responses = productService.findAllActive(pageable);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).getName()).isEqualTo("Test Product");
        verify(productRepository).findAllActive(pageable);
    }

    @Test
    @DisplayName("Should find product by id")
    void findById_Success() {
        // Given
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));

        // When
        ProductResponse response = productService.findById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(productRepository).findActiveById(1L);
    }

    @Test
    @DisplayName("Should find products by category")
    void findByCategory_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.findActiveByCategory(ProductCategory.ELECTRONICS, pageable))
                .thenReturn(productPage);

        // When
        Page<ProductResponse> responses = productService.findByCategory(ProductCategory.ELECTRONICS, pageable);

        // Then
        assertThat(responses).hasSize(1);
        verify(productRepository).findActiveByCategory(ProductCategory.ELECTRONICS, pageable);
    }

    @Test
    @DisplayName("Should search products by keyword")
    void searchProducts_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.searchActiveProducts("Test", pageable)).thenReturn(productPage);

        // When
        Page<ProductResponse> responses = productService.searchProducts("Test", pageable);

        // Then
        assertThat(responses).hasSize(1);
        verify(productRepository).searchActiveProducts("Test", pageable);
    }

    @Test
    @DisplayName("Should throw exception when product not found by id")
    void findById_NotFound_ThrowsException() {
        // Given
        when(productRepository.findActiveById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(edts.week7_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("id")
                .hasMessageContaining("999");

        verify(productRepository).findActiveById(999L);
    }

    @Test
    @DisplayName("Should throw exception when creating product with duplicate name")
    void createProduct_DuplicateName_ThrowsException() {
        // Given
        when(productRepository.findActiveByName(anyString())).thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Product name already exists");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should create FOOD product with valid price")
    void createProduct_FoodWithValidPrice_Success() {
        // Given
        ProductRequest foodRequest = new ProductRequest("Food Item", ProductCategory.FOOD,
                new BigDecimal("50000"), 50);

        when(productRepository.findActiveByName(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        // When
        ProductResponse response = productService.createProduct(foodRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCategory()).isEqualTo(ProductCategory.FOOD);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product when name is same as current product")
    void updateProduct_SameName_Success() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest("Test Product",
                null, null, null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findActiveByName("Test Product")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating product with duplicate name")
    void updateProduct_DuplicateName_ThrowsException() {
        // Given
        Product existingProduct = new Product();
        existingProduct.setId(2L);
        existingProduct.setName("Other Product");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest("Other Product",
                null, null, null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findActiveByName("Other Product")).thenReturn(Optional.of(existingProduct));

        // When/Then
        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Product name already exists");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating FOOD product price exceeds limit")
    void updateProduct_FoodPriceExceedsLimit_ThrowsException() {
        // Given
        product.setCategory(ProductCategory.FOOD);
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                ProductCategory.FOOD, new BigDecimal("2000000"), null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Food products cannot exceed price");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating stock to negative")
    void updateProduct_NegativeStock_ThrowsException() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                null, null, -5, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock cannot be negative");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when deactivating product with stock")
    void updateProduct_DeactivateWithStock_ThrowsException() {
        // Given
        product.setStock(50);
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                null, null, null, false);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot deactivate product with stock");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should deactivate product successfully when stock is 0")
    void updateProduct_DeactivateWithZeroStock_Success() {
        // Given
        product.setStock(0);
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                null, null, null, false);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getActive()).isFalse();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting product not found")
    void deleteProduct_NotFound_ThrowsException() {
        // Given
        when(productRepository.findActiveById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(edts.week7_practice1.exception.ResourceNotFoundException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update stock successfully")
    void updateStock_Success() {
        // Given
        product.setStock(100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.updateStock(1L, 50);

        // Then
        assertThat(product.getStock()).isEqualTo(150);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating stock for product not found")
    void updateStock_NotFound_ThrowsException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.updateStock(999L, 50))
                .isInstanceOf(edts.week7_practice1.exception.ResourceNotFoundException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update stock with negative quantity")
    void updateStock_NegativeQuantity_Success() {
        // Given
        product.setStock(100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.updateStock(1L, -50);

        // Then
        assertThat(product.getStock()).isEqualTo(50);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update all fields successfully")
    void updateProduct_AllFields_Success() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest("New Name",
                ProductCategory.FASHION, new BigDecimal("75000"), 25, true);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.countByProductIdAndOrderStatus(1L,
                edts.week7_practice1.enums.OrderStatus.PAID)).thenReturn(0L);
        when(productRepository.findActiveByName("New Name")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getName()).isEqualTo("New Name");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.FASHION);
        assertThat(product.getPrice()).isEqualByComparingTo("75000");
        assertThat(product.getStock()).isEqualTo(25);
        assertThat(product.getActive()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product with only some fields")
    void updateProduct_PartialFields_Success() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest("Partial Update",
                null, null, null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findActiveByName("Partial Update")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getName()).isEqualTo("Partial Update");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.ELECTRONICS); // unchanged
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product price when no completed orders exist")
    void updateProduct_PriceWithoutCompletedOrders_Success() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                null, new BigDecimal("750000"), null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.countByProductIdAndOrderStatus(1L,
                edts.week7_practice1.enums.OrderStatus.PAID)).thenReturn(0L);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getPrice()).isEqualByComparingTo("750000");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product category only")
    void updateProduct_CategoryOnly_Success() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                ProductCategory.FASHION, null, null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getCategory()).isEqualTo(ProductCategory.FASHION);
        assertThat(product.getPrice()).isEqualByComparingTo("500000"); // unchanged
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product stock only")
    void updateProduct_StockOnly_Success() {
        // Given
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                null, null, 75, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getStock()).isEqualTo(75);
        assertThat(product.getName()).isEqualTo("Test Product"); // unchanged
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product active status when stock is zero")
    void updateProduct_ActiveWithZeroStock_Success() {
        // Given
        product.setStock(0);
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                null, null, null, true);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getActive()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update FOOD product with valid price")
    void updateProduct_FoodWithValidPrice_Success() {
        // Given
        product.setCategory(ProductCategory.FOOD);
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                ProductCategory.FOOD, new BigDecimal("50000"), null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.countByProductIdAndOrderStatus(1L,
                edts.week7_practice1.enums.OrderStatus.PAID)).thenReturn(0L);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getPrice()).isEqualByComparingTo("50000");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.FOOD);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should activate product successfully")
    void updateProduct_ActivateProduct_Success() {
        // Given
        product.setActive(false);
        product.setStock(0);
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                null, null, null, true);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getActive()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating FOOD product with price exceeding limit when category is also set")
    void updateProduct_FoodCategoryAndPriceExceeds_ThrowsException() {
        // Given
        product.setCategory(ProductCategory.ELECTRONICS);
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                ProductCategory.FOOD, new BigDecimal("2000000"), null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.countByProductIdAndOrderStatus(1L,
                edts.week7_practice1.enums.OrderStatus.PAID)).thenReturn(0L);

        // When/Then
        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Food products cannot exceed price");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product with null category when updating price")
    void updateProduct_PriceWithoutCategory_Success() {
        // Given
        product.setCategory(ProductCategory.FOOD);
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null,
                null, new BigDecimal("500000"), null, null);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.countByProductIdAndOrderStatus(1L,
                edts.week7_practice1.enums.OrderStatus.PAID)).thenReturn(0L);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getPrice()).isEqualByComparingTo("500000");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.FOOD); // unchanged
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update all fields including name with different case")
    void updateProduct_AllFieldsWithDifferentCase_Success() {
        // Given
        product.setName("test product");
        ProductUpdateRequest updateRequest = new ProductUpdateRequest("Test Product",
                ProductCategory.FASHION, new BigDecimal("75000"), 25, true);

        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.countByProductIdAndOrderStatus(1L,
                edts.week7_practice1.enums.OrderStatus.PAID)).thenReturn(0L);
        when(productRepository.findActiveByName("Test Product")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getName()).isEqualTo("Test Product");
        verify(productRepository).save(any(Product.class));
    }
}
