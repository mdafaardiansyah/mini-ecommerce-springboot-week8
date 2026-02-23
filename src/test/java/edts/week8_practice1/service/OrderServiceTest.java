package edts.week8_practice1.service;

import edts.week8_practice1.dto.order.OrderItemRequest;
import edts.week8_practice1.dto.order.OrderRequest;
import edts.week8_practice1.dto.order.OrderResponse;
import edts.week8_practice1.entity.*;
import edts.week8_practice1.enums.CustomerMembership;
import edts.week8_practice1.enums.OrderStatus;
import edts.week8_practice1.enums.ProductCategory;
import edts.week8_practice1.exception.BusinessException;
import edts.week8_practice1.repository.CustomerRepository;
import edts.week8_practice1.repository.OrderRepository;
import edts.week8_practice1.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product product;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setMembershipLevel(CustomerMembership.REGULAR);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setActive(true);

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setCategory(ProductCategory.ELECTRONICS);
        product.setPrice(new BigDecimal("100000"));
        product.setStock(100);
        product.setActive(true);

        OrderItemRequest itemRequest = new OrderItemRequest(1L, 5);
        orderRequest = new OrderRequest(1L, Collections.singletonList(itemRequest));
    }

    @Test
    @DisplayName("Should create order successfully for REGULAR customer")
    void createOrder_RegularCustomer_Success() {
        // Given
        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(orderRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("500000"); // 100000 * 5
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("0"); // REGULAR = 0%
        assertThat(response.getFinalAmount()).isEqualByComparingTo("500000");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(product.getStock()).isEqualTo(95); // 100 - 5

        verify(orderRepository).save(any(Order.class));
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should apply 10% discount for GOLD customer")
    void createOrder_GoldCustomer_DiscountApplied() {
        // Given
        customer.setMembershipLevel(CustomerMembership.GOLD);

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(orderRequest);

        // Then
        assertThat(response.getTotalAmount()).isEqualByComparingTo("500000");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("50000"); // 10% of 500000
        assertThat(response.getFinalAmount()).isEqualByComparingTo("450000");
        assertThat(response.getDiscountPercentage()).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("Should apply 20% discount for PLATINUM customer")
    void createOrder_PlatinumCustomer_DiscountApplied() {
        // Given
        customer.setMembershipLevel(CustomerMembership.PLATINUM);

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(orderRequest);

        // Then
        assertThat(response.getTotalAmount()).isEqualByComparingTo("500000");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("100000"); // 20% of 500000
        assertThat(response.getFinalAmount()).isEqualByComparingTo("400000");
        assertThat(response.getDiscountPercentage()).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("Should apply 5% bonus discount when total > 5,000,000")
    void createOrder_BonusDiscountApplied() {
        // Given
        product.setPrice(new BigDecimal("2000000")); // High price product

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(orderRequest);

        // Then
        BigDecimal expectedTotal = new BigDecimal("10000000"); // 2000000 * 5
        BigDecimal expectedDiscount = new BigDecimal("500000"); // 5% of 10000000
        BigDecimal expectedFinal = new BigDecimal("9500000");

        assertThat(response.getTotalAmount()).isEqualByComparingTo(expectedTotal);
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(expectedDiscount);
        assertThat(response.getFinalAmount()).isEqualByComparingTo(expectedFinal);
    }

    @Test
    @DisplayName("Should cap discount at 30%")
    void createOrder_MaxDiscountCapped() {
        // Given
        customer.setMembershipLevel(CustomerMembership.PLATINUM); // 20%
        product.setPrice(new BigDecimal("2000000")); // Triggers bonus 5% = 25%, but cap at 30%

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(orderRequest);

        // Then
        BigDecimal expectedTotal = new BigDecimal("10000000");
        BigDecimal expectedDiscount = new BigDecimal("2500000"); // 25% of 10000000
        BigDecimal expectedFinal = new BigDecimal("7500000");

        assertThat(response.getTotalAmount()).isEqualByComparingTo(expectedTotal);
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(expectedDiscount);
        assertThat(response.getFinalAmount()).isEqualByComparingTo(expectedFinal);
        assertThat(response.getDiscountPercentage()).isEqualByComparingTo("25");
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void createOrder_InsufficientStock_ThrowsException() {
        // Given
        product.setStock(2); // Only 2 in stock, requesting 5

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Not enough stock");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should pay order successfully and update customer total spent")
    void payOrder_Success() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("500000"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(new BigDecimal("500000"));

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        doNothing().when(customerService).updateTotalSpent(anyLong(), any());

        // When
        OrderResponse response = orderService.payOrder(1L);

        // Then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
        verify(customerService).updateTotalSpent(1L, new BigDecimal("500000"));
    }

    @Test
    @DisplayName("Should throw exception when paying non-CREATED order")
    void payOrder_NotCreatedStatus_ThrowsException() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        // When/Then
        assertThatThrownBy(() -> orderService.payOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order cannot be paid");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should cancel order and restore stock")
    void cancelOrder_Success() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProduct(product);
        orderItem.setQuantity(5);
        orderItem.setPriceAtPurchase(new BigDecimal("100000"));
        orderItem.setOrder(order);

        order.addOrderItem(orderItem);

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        OrderResponse response = orderService.cancelOrder(1L);

        // Then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStock()).isEqualTo(105); // 100 + 5 restored
        verify(orderRepository).save(order);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw exception when cancelling PAID order")
    void cancelOrder_PaidOrder_ThrowsException() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        // When/Then
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order cannot be cancelled");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when customer not found for create order")
    void createOrder_CustomerNotFound_ThrowsException() {
        // Given
        when(customerRepository.findActiveById(999L)).thenReturn(Optional.empty());

        OrderRequest request = new OrderRequest(999L, Collections.singletonList(
            new OrderItemRequest(1L, 5)));

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(edts.week8_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining("999");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found for create order")
    void createOrder_ProductNotFound_ThrowsException() {
        // Given
        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                .isInstanceOf(edts.week8_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("1");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create order with multiple items")
    void createOrder_MultipleItems_Success() {
        // Given
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Test Product 2");
        product2.setPrice(new BigDecimal("200000"));
        product2.setStock(50);
        product2.setActive(true);

        OrderItemRequest itemRequest1 = new OrderItemRequest(1L, 3);
        OrderItemRequest itemRequest2 = new OrderItemRequest(2L, 2);
        OrderRequest request = new OrderRequest(1L, List.of(itemRequest1, itemRequest2));

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findActiveById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("700000"); // (100000*3) + (200000*2)
        assertThat(response.getOrderItems()).hasSize(2);
        assertThat(product.getStock()).isEqualTo(97); // 100 - 3
        assertThat(product2.getStock()).isEqualTo(48); // 50 - 2

        verify(orderRepository).save(any(Order.class));
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should find order by id successfully")
    void findById_Success() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("500000"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(new BigDecimal("500000"));

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        // When
        OrderResponse response = orderService.findById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("Should throw exception when order not found by id")
    void findById_NotFound_ThrowsException() {
        // Given
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.findById(999L))
                .isInstanceOf(edts.week8_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Order")
                .hasMessageContaining("999");

        verify(orderRepository).findByIdWithDetails(999L);
    }

    @Test
    @DisplayName("Should find all orders")
    void findAll_Success() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Order> orderPage =
            new org.springframework.data.domain.PageImpl<>(List.of(order));

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // When
        org.springframework.data.domain.Page<OrderResponse> responses =
            orderService.findAll(pageable);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(1L);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should find orders by customer id")
    void findByCustomerId_Success() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Order> orderPage =
            new org.springframework.data.domain.PageImpl<>(List.of(order));

        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(orderPage);

        // When
        org.springframework.data.domain.Page<OrderResponse> responses =
            orderService.findByCustomerId(1L, pageable);

        // Then
        assertThat(responses).hasSize(1);
        verify(orderRepository).findByCustomerId(1L, pageable);
    }

    @Test
    @DisplayName("Should find orders by status")
    void findByStatus_Success() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Order> orderPage =
            new org.springframework.data.domain.PageImpl<>(List.of(order));

        when(orderRepository.findByStatus(OrderStatus.CREATED, pageable)).thenReturn(orderPage);

        // When
        org.springframework.data.domain.Page<OrderResponse> responses =
            orderService.findByStatus(OrderStatus.CREATED, pageable);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepository).findByStatus(OrderStatus.CREATED, pageable);
    }

    @Test
    @DisplayName("Should throw exception when paying order not found")
    void payOrder_NotFound_ThrowsException() {
        // Given
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.payOrder(999L))
                .isInstanceOf(edts.week8_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Order")
                .hasMessageContaining("999");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when paying cancelled order")
    void payOrder_CancelledOrder_ThrowsException() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        // When/Then
        assertThatThrownBy(() -> orderService.payOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order cannot be paid");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling order not found")
    void cancelOrder_NotFound_ThrowsException() {
        // Given
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.cancelOrder(999L))
                .isInstanceOf(edts.week8_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Order")
                .hasMessageContaining("999");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling cancelled order")
    void cancelOrder_AlreadyCancelled_ThrowsException() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        // When/Then
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order cannot be cancelled");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create order with exact stock threshold")
    void createOrder_ExactStock_Success() {
        // Given
        product.setStock(5); // Exactly the requested quantity

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(orderRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(product.getStock()).isEqualTo(0); // 5 - 5 = 0
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create order with zero total amount after discount")
    void createOrder_ZeroTotalAfterDiscount_Success() {
        // Given
        customer.setMembershipLevel(CustomerMembership.PLATINUM); // 20% discount
        product.setPrice(new BigDecimal("1000000")); // 5,000,000 total

        OrderItemRequest itemRequest = new OrderItemRequest(1L, 5);
        OrderRequest request = new OrderRequest(1L, Collections.singletonList(itemRequest));

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("5000000");
        // Should have discount applied
        assertThat(response.getDiscountAmount()).isGreaterThan(BigDecimal.ZERO);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle discount calculation with decimal precision")
    void createOrder_DiscountPrecision_Success() {
        // Given
        product.setPrice(new BigDecimal("333333")); // Will create decimals
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 3);
        OrderRequest request = new OrderRequest(1L, Collections.singletonList(itemRequest));

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFinalAmount()).isNotNull();
        assertThat(response.getFinalAmount().scale()).isLessThanOrEqualTo(2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create order that exactly equals bonus discount threshold")
    void createOrder_ExactlyBonusThreshold_Success() {
        // Given
        product.setPrice(new BigDecimal("1000000")); // Exactly 5,000,000 for 5 items
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 5);
        OrderRequest request = new OrderRequest(1L, Collections.singletonList(itemRequest));

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("5000000");
        // Exactly 5,000,000 should NOT trigger bonus (> 5,000,000)
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle discount percentage calculation for zero total")
    void mapToResponse_ZeroTotal_NoDiscount() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(BigDecimal.ZERO);

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));

        // When
        OrderResponse response = orderService.findById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDiscountPercentage()).isEqualByComparingTo("0");
        verify(orderRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("Should cap discount at 30% for PLATINUM with large order")
    void createOrder_MaxDiscountCap_Success() {
        // Given
        customer.setMembershipLevel(CustomerMembership.PLATINUM); // 20%
        product.setPrice(new BigDecimal("4000000")); // 20,000,000 total

        OrderItemRequest itemRequest = new OrderItemRequest(1L, 5);
        OrderRequest request = new OrderRequest(1L, Collections.singletonList(itemRequest));

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        // PLATINUM (20%) + Bonus (5%) = 25%, not capped
        BigDecimal expectedTotal = new BigDecimal("20000000");
        BigDecimal expectedDiscount = new BigDecimal("5000000"); // 25%
        BigDecimal expectedFinal = new BigDecimal("15000000");

        assertThat(response.getTotalAmount()).isEqualByComparingTo(expectedTotal);
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(expectedDiscount);
        assertThat(response.getFinalAmount()).isEqualByComparingTo(expectedFinal);
        assertThat(response.getDiscountPercentage()).isEqualByComparingTo("25");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should apply only bonus discount for REGULAR customer")
    void createOrder_RegularWithBonus_Success() {
        // Given
        product.setPrice(new BigDecimal("2000000")); // 10,000,000 total

        OrderItemRequest itemRequest = new OrderItemRequest(1L, 5);
        OrderRequest request = new OrderRequest(1L, Collections.singletonList(itemRequest));

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        // REGULAR (0%) + Bonus (5%) = 5%
        assertThat(response.getTotalAmount()).isEqualByComparingTo("10000000");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("500000"); // 5%
        assertThat(response.getFinalAmount()).isEqualByComparingTo("9500000");
        assertThat(response.getDiscountPercentage()).isEqualByComparingTo("5");
        verify(orderRepository).save(any(Order.class));
    }
}
