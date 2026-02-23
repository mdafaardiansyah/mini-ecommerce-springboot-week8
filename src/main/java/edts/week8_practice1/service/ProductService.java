package edts.week8_practice1.service;

import edts.week8_practice1.dto.product.ProductRequest;
import edts.week8_practice1.dto.product.ProductResponse;
import edts.week8_practice1.dto.product.ProductUpdateRequest;
import edts.week8_practice1.entity.Product;
import edts.week8_practice1.enums.ProductCategory;
import edts.week8_practice1.exception.BusinessException;
import edts.week8_practice1.exception.ResourceNotFoundException;
import edts.week8_practice1.repository.OrderRepository;
import edts.week8_practice1.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final BigDecimal FOOD_MAX_PRICE = new BigDecimal("1000000");

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAllActive(Pageable pageable) {
        logger.info("Finding all active products");
        Page<Product> products = productRepository.findAllActive(pageable);
        return products.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        logger.info("Finding product by id: {}", id);
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findByCategory(ProductCategory category, Pageable pageable) {
        logger.info("Finding products by category: {}", category);
        Page<Product> products = productRepository.findActiveByCategory(category, pageable);
        return products.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        logger.info("Searching products with keyword: {}", keyword);
        Page<Product> products = productRepository.searchActiveProducts(keyword, pageable);
        return products.map(this::mapToResponse);
    }

    public ProductResponse createProduct(ProductRequest request) {
        logger.info("Creating new product: {}", request.getName());

        // Validate price > 0
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("VALIDATION_ERROR", "Price must be greater than 0",
                    Arrays.asList("Price must be greater than 0"));
        }

        // Validate stock >= 0
        if (request.getStock() < 0) {
            throw new BusinessException("VALIDATION_ERROR", "Stock cannot be negative",
                    Arrays.asList("Stock must be >= 0"));
        }

        // Validate FOOD category max price
        if (request.getCategory() == ProductCategory.FOOD &&
                request.getPrice().compareTo(FOOD_MAX_PRICE) > 0) {
            throw new BusinessException("VALIDATION_ERROR",
                    "Food products cannot exceed price of 1,000,000",
                    Arrays.asList("Maximum price for FOOD category is 1,000,000"));
        }

        // Check duplicate name
        productRepository.findActiveByName(request.getName())
                .ifPresent(p -> {
                    throw new BusinessException("DUPLICATE_ERROR",
                            "Product name already exists",
                            Arrays.asList("Product with name '" + request.getName() + "' already exists"));
                });

        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setActive(true);

        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully with id: {}", savedProduct.getId());
        return mapToResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        logger.info("Updating product with id: {}", id);

        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check if product has completed orders
        long completedOrdersCount = orderRepository.countByProductIdAndOrderStatus(id,
                edts.week8_practice1.enums.OrderStatus.PAID);
        if (completedOrdersCount > 0 && request.getPrice() != null) {
            throw new BusinessException("BUSINESS_ERROR",
                    "Cannot update price for product with completed orders",
                    Arrays.asList("Product has paid orders, price cannot be modified"));
        }

        if (request.getName() != null) {
            productRepository.findActiveByName(request.getName())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new BusinessException("DUPLICATE_ERROR",
                                    "Product name already exists",
                                    Arrays.asList("Product with name '" + request.getName() + "' already exists"));
                        }
                    });
            product.setName(request.getName());
        }

        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }

        if (request.getPrice() != null) {
            if (request.getCategory() == ProductCategory.FOOD &&
                    request.getPrice().compareTo(FOOD_MAX_PRICE) > 0) {
                throw new BusinessException("VALIDATION_ERROR",
                        "Food products cannot exceed price of 1,000,000",
                        Arrays.asList("Maximum price for FOOD category is 1,000,000"));
            }
            product.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new BusinessException("VALIDATION_ERROR", "Stock cannot be negative",
                        Arrays.asList("Stock must be >= 0"));
            }
            product.setStock(request.getStock());
        }

        if (request.getActive() != null) {
            if (!request.getActive() && product.getStock() > 0) {
                throw new BusinessException("BUSINESS_ERROR",
                        "Cannot deactivate product with stock",
                        Arrays.asList("Product has " + product.getStock() + " items in stock"));
            }
            product.setActive(request.getActive());
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Product updated successfully: {}", savedProduct.getId());
        return mapToResponse(savedProduct);
    }

    public void deleteProduct(Long id) {
        logger.info("Soft deleting product with id: {}", id);

        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (product.getStock() > 0) {
            throw new BusinessException("BUSINESS_ERROR",
                    "Cannot delete product with stock",
                    Arrays.asList("Product has " + product.getStock() + " items in stock"));
        }

        product.setActive(false);
        productRepository.save(product);
        logger.info("Product soft deleted successfully: {}", id);
    }

    public void updateStock(Long productId, Integer quantity) {
        logger.info("Updating stock for product {}: {} units", productId, quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
