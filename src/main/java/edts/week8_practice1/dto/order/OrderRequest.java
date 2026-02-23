package edts.week8_practice1.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> orderItems;

    // Constructors
    public OrderRequest() {
    }

    public OrderRequest(Long customerId, List<OrderItemRequest> orderItems) {
        this.customerId = customerId;
        this.orderItems = orderItems;
    }

    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemRequest> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemRequest> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "customerId=" + customerId +
                ", orderItems=" + orderItems +
                '}';
    }
}
