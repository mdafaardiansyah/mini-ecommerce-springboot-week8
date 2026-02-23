package edts.week8_practice1.service;

import edts.week8_practice1.dto.customer.CustomerResponse;
import edts.week8_practice1.dto.order.*;
import edts.week8_practice1.dto.product.ProductResponse;
import edts.week8_practice1.entity.*;
import edts.week8_practice1.enums.CustomerMembership;
import edts.week8_practice1.enums.OrderStatus;
import edts.week8_practice1.exception.BusinessException;
import edts.week8_practice1.exception.ResourceNotFoundException;
import edts.week8_practice1.repository.CustomerRepository;
import edts.week8_practice1.repository.OrderRepository;
import edts.week8_practice1.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final BigDecimal BONUS_DISCOUNT_THRESHOLD = new BigDecimal("5000000");
    private static final BigDecimal BONUS_DISCOUNT_PERCENTAGE = new BigDecimal("0.05");
    private static final BigDecimal MAX_DISCOUNT_PERCENTAGE = new BigDecimal("0.30");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository,
                       ProductRepository productRepository, CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.customerService = customerService;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable) {
        logger.info("Finding all orders");
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        logger.info("Finding order by id: {}", id);
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findByCustomerId(Long customerId, Pageable pageable) {
        logger.info("Finding orders for customer: {}", customerId);
        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);
        return orders.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findByStatus(OrderStatus status, Pageable pageable) {
        logger.info("Finding orders by status: {}", status);
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        return orders.map(this::mapToResponse);
    }

    public OrderResponse createOrder(OrderRequest request) {
        logger.info("Creating order for customer: {}", request.getCustomerId());

        // Get customer
        Customer customer = customerRepository.findActiveById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Process order items
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Product product = productRepository.findActiveById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            // Validate stock
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_STOCK",
                        "Not enough stock for product: " + product.getName(),
                        Arrays.asList("Available: " + product.getStock() + ", Requested: " + itemRequest.getQuantity()));
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.setOrder(order);

            orderItems.add(orderItem);

            // Calculate item subtotal
            BigDecimal itemSubtotal = product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);

            // Reduce stock immediately
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Calculate discount
        BigDecimal discountAmount = calculateDiscount(customer, totalAmount);
        order.setDiscountAmount(discountAmount);

        // Calculate final amount
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        order.setFinalAmount(finalAmount);

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with id: {}, total amount: {}, discount: {}, final amount: {}",
                savedOrder.getId(), totalAmount, discountAmount, finalAmount);

        return mapToResponse(savedOrder);
    }

    private BigDecimal calculateDiscount(Customer customer, BigDecimal totalAmount) {
        // Base discount by membership
        BigDecimal baseDiscountRate = switch (customer.getMembershipLevel()) {
            case REGULAR -> BigDecimal.ZERO;
            case GOLD -> new BigDecimal("0.10");
            case PLATINUM -> new BigDecimal("0.20");
        };

        BigDecimal discountRate = baseDiscountRate;

        // Bonus 5% if total amount > 5,000,000
        if (totalAmount.compareTo(BONUS_DISCOUNT_THRESHOLD) > 0) {
            discountRate = discountRate.add(BONUS_DISCOUNT_PERCENTAGE);
        }

        // Cap at 30%
        if (discountRate.compareTo(MAX_DISCOUNT_PERCENTAGE) > 0) {
            discountRate = MAX_DISCOUNT_PERCENTAGE;
        }

        BigDecimal discountAmount = totalAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

        logger.debug("Discount calculation - Membership: {}, Base rate: {}, Total: {}, Discount rate: {}, Discount amount: {}",
                customer.getMembershipLevel(), baseDiscountRate, totalAmount, discountRate, discountAmount);

        return discountAmount;
    }

    public OrderResponse payOrder(Long orderId) {
        logger.info("Paying order: {}", orderId);

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("INVALID_STATUS",
                    "Order cannot be paid",
                    Arrays.asList("Order status is " + order.getStatus() + ", only CREATED orders can be paid"));
        }

        order.setStatus(OrderStatus.PAID);
        Order savedOrder = orderRepository.save(order);

        // Update customer total spent and recalculate membership
        customerService.updateTotalSpent(order.getCustomer().getId(), order.getFinalAmount());

        logger.info("Order paid successfully: {}", orderId);
        return mapToResponse(savedOrder);
    }

    public OrderResponse cancelOrder(Long orderId) {
        logger.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("INVALID_STATUS",
                    "Order cannot be cancelled",
                    Arrays.asList("Order status is " + order.getStatus() + ", only CREATED orders can be cancelled"));
        }

        // Restore stock
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        logger.info("Order cancelled successfully: {}", orderId);
        return mapToResponse(savedOrder);
    }

    private OrderResponse mapToResponse(Order order) {
        CustomerResponse customerResponse = mapCustomerToResponse(order.getCustomer());

        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::mapOrderItemToResponse)
                .collect(Collectors.toList());

        // Calculate discount percentage
        BigDecimal discountPercentage = BigDecimal.ZERO;
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            discountPercentage = order.getDiscountAmount()
                    .divide(order.getTotalAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new OrderResponse(
                order.getId(),
                customerResponse,
                orderItemResponses,
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getFinalAmount(),
                discountPercentage,
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    private CustomerResponse mapCustomerToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getMembershipLevel(),
                customer.getTotalSpent(),
                customer.getActive()
        );
    }

    private ProductResponse mapProductToResponse(Product product) {
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

    private OrderItemResponse mapOrderItemToResponse(OrderItem orderItem) {
        ProductResponse productResponse = mapProductToResponse(orderItem.getProduct());
        BigDecimal subtotal = orderItem.getPriceAtPurchase()
                .multiply(new BigDecimal(orderItem.getQuantity()));

        return new OrderItemResponse(
                orderItem.getId(),
                productResponse,
                orderItem.getQuantity(),
                orderItem.getPriceAtPurchase(),
                subtotal
        );
    }
}
