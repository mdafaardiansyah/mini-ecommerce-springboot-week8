package edts.week7_practice1.entity;

import edts.week7_practice1.enums.CustomerMembership;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntityWithSoftDelete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level", nullable = false, length = 50)
    private CustomerMembership membershipLevel = CustomerMembership.REGULAR;

    @Column(name = "total_spent", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    // Constructors
    public Customer() {
        super();
        setActive(true);
        this.membershipLevel = CustomerMembership.REGULAR;
        this.totalSpent = BigDecimal.ZERO;
    }

    public Customer(String name, String email) {
        this();
        this.name = name;
        this.email = email;
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

    // Business logic helpers
    public void addTotalSpent(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.totalSpent = this.totalSpent.add(amount);
        updateMembershipLevel();
    }

    public void updateMembershipLevel() {
        CustomerMembership newLevel = calculateMembershipLevel();
        // Only upgrade, never downgrade
        if (newLevel.ordinal() > this.membershipLevel.ordinal()) {
            this.membershipLevel = newLevel;
        }
    }

    private CustomerMembership calculateMembershipLevel() {
        if (totalSpent.compareTo(new BigDecimal("50000000")) >= 0) {
            return CustomerMembership.PLATINUM;
        } else if (totalSpent.compareTo(new BigDecimal("10000000")) >= 0) {
            return CustomerMembership.GOLD;
        }
        return CustomerMembership.REGULAR;
    }

    public BigDecimal getDiscountPercentage() {
        return switch (membershipLevel) {
            case REGULAR -> BigDecimal.ZERO;
            case GOLD -> new BigDecimal("0.10");
            case PLATINUM -> new BigDecimal("0.20");
        };
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) &&
                Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", membershipLevel=" + membershipLevel +
                ", totalSpent=" + totalSpent +
                ", active=" + getActive() +
                '}';
    }
}
