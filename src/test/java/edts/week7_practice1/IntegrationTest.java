package edts.week7_practice1;

import edts.week7_practice1.dto.customer.CustomerRequest;
import edts.week7_practice1.dto.order.*;
import edts.week7_practice1.dto.product.ProductRequest;
import edts.week7_practice1.entity.*;
import edts.week7_practice1.enums.*;
import edts.week7_practice1.repository.*;
import edts.week7_practice1.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        // Clean database
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer
        customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");
        customer.setMembershipLevel(CustomerMembership.REGULAR);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setActive(true);
        customer = customerRepository.save(customer);

        // Create test product
        product = new Product();
        product.setName("Test Product");
        product.setCategory(ProductCategory.ELECTRONICS);
        product.setPrice(new BigDecimal("100000"));
        product.setStock(100);
        product.setActive(true);
        product = productRepository.save(product);
    }

    @Test
    @DisplayName("Complete Order Flow: Create -> Pay -> Verify Membership Upgrade")
    void completeOrderFlow_Success() {
        // Given - Initial state
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.REGULAR);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("0");
        assertThat(product.getStock()).isEqualTo(100);

        // When - Create order
        OrderItemRequest itemRequest = new OrderItemRequest(product.getId(), 50);
        OrderRequest orderRequest = new OrderRequest(customer.getId(), Collections.singletonList(itemRequest));
        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        // Then - Order created successfully
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(orderResponse.getTotalAmount()).isEqualByComparingTo("5000000"); // 100000 * 50
        assertThat(product.getStock()).isEqualTo(50); // Stock reduced immediately

        // Refresh product from database
        product = productRepository.findById(product.getId()).get();

        // When - Pay order
        OrderResponse paidOrder = orderService.payOrder(orderResponse.getId());

        // Then - Order paid
        assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        // Refresh customer from database
        customer = customerRepository.findById(customer.getId()).get();

        // Then - Customer upgraded to GOLD (spent 5M >= 10M threshold, but not PLATINUM yet)
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("5000000");
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.REGULAR); // Still REGULAR (need 10M)

        // Create another order to reach GOLD threshold
        OrderItemRequest itemRequest2 = new OrderItemRequest(product.getId(), 50);
        OrderRequest orderRequest2 = new OrderRequest(customer.getId(), Collections.singletonList(itemRequest2));
        OrderResponse orderResponse2 = orderService.createOrder(orderRequest2);
        orderService.payOrder(orderResponse2.getId());

        // Refresh customer
        customer = customerRepository.findById(customer.getId()).get();

        // Then - Customer upgraded to GOLD (spent 10M)
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("10000000");
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.GOLD);
    }

    @Test
    @DisplayName("Order Cancellation Flow: Create -> Cancel -> Verify Stock Restoration")
    void orderCancellationFlow_Success() {
        // Given
        int initialStock = product.getStock();
        assertThat(initialStock).isEqualTo(100); // After setUp, stock is 100

        // When - Create order
        OrderItemRequest itemRequest = new OrderItemRequest(product.getId(), 30);
        OrderRequest orderRequest = new OrderRequest(customer.getId(), Collections.singletonList(itemRequest));
        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        // Then - Stock reduced
        product = productRepository.findById(product.getId()).get();
        assertThat(product.getStock()).isEqualTo(70); // 100 - 30

        // When - Cancel order
        OrderResponse cancelledOrder = orderService.cancelOrder(orderResponse.getId());

        // Then - Order cancelled and stock restored
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        product = productRepository.findById(product.getId()).get();
        assertThat(product.getStock()).isEqualTo(100); // Restored to original

        // Then - Customer total spent unchanged
        customer = customerRepository.findById(customer.getId()).get();
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("PLATINUM Customer Maximum Discount Flow")
    void platinumCustomerMaxDiscountFlow_Success() {
        // Given - Upgrade customer to PLATINUM
        customer.setMembershipLevel(CustomerMembership.PLATINUM);
        customer = customerRepository.save(customer);

        // When - Create large order (> 5M to trigger bonus discount)
        product.setPrice(new BigDecimal("2000000")); // 2M per item
        product = productRepository.save(product);

        OrderItemRequest itemRequest = new OrderItemRequest(product.getId(), 5); // Total 10M
        OrderRequest orderRequest = new OrderRequest(customer.getId(), Collections.singletonList(itemRequest));
        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        // Then - Maximum discount applied (capped at 30%)
        // PLATINUM (20%) + Bonus (5%) = 25%, which is under the 30% cap
        assertThat(orderResponse.getTotalAmount()).isEqualByComparingTo("10000000");
        assertThat(orderResponse.getDiscountAmount()).isEqualByComparingTo("2500000"); // 25%
        assertThat(orderResponse.getFinalAmount()).isEqualByComparingTo("7500000");
        assertThat(orderResponse.getDiscountPercentage()).isEqualByComparingTo("25");
    }

    @Test
    @DisplayName("Product Validation Flow - FOOD Category Max Price")
    void foodProductMaxPriceValidation_Success() {
        // Given - Valid FOOD product
        ProductRequest foodRequest = new ProductRequest(
                "Food Item",
                ProductCategory.FOOD,
                new BigDecimal("500000"), // Under 1M limit
                50
        );

        // When - Create product
        var response = productService.createProduct(foodRequest);

        // Then - Product created successfully
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCategory()).isEqualTo(ProductCategory.FOOD);
    }

    @Test
    @DisplayName("Membership Auto-Upgrade REGULAR -> GOLD -> PLATINUM")
    void membershipAutoUpgradeFlow_Success() {
        // Given - REGULAR customer
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.REGULAR);

        // When - Spend 15M (should upgrade to GOLD)
        customerService.updateTotalSpent(customer.getId(), new BigDecimal("15000000"));

        // Then
        customer = customerRepository.findById(customer.getId()).get();
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.GOLD);

        // When - Spend additional 40M (total 55M, should upgrade to PLATINUM)
        customerService.updateTotalSpent(customer.getId(), new BigDecimal("40000000"));

        // Then
        customer = customerRepository.findById(customer.getId()).get();
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.PLATINUM);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("55000000");
    }
}
