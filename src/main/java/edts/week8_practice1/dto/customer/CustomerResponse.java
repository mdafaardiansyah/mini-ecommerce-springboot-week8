package edts.week8_practice1.dto.customer;

import edts.week8_practice1.enums.CustomerMembership;

import java.math.BigDecimal;

public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private CustomerMembership membershipLevel;
    private BigDecimal totalSpent;
    private Boolean active;

    // Constructors
    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String name, String email, CustomerMembership membershipLevel,
                           BigDecimal totalSpent, Boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.membershipLevel = membershipLevel;
        this.totalSpent = totalSpent;
        this.active = active;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CustomerMembership getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(CustomerMembership membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "CustomerResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", membershipLevel=" + membershipLevel +
                ", totalSpent=" + totalSpent +
                ", active=" + active +
                '}';
    }
}
