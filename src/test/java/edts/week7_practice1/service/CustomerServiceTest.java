package edts.week7_practice1.service;

import edts.week7_practice1.dto.customer.CustomerRequest;
import edts.week7_practice1.dto.customer.CustomerResponse;
import edts.week7_practice1.entity.Customer;
import edts.week7_practice1.enums.CustomerMembership;
import edts.week7_practice1.exception.BusinessException;
import edts.week7_practice1.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setMembershipLevel(CustomerMembership.REGULAR);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setActive(true);

        customerRequest = new CustomerRequest("John Doe", "john@example.com");
    }

    @Test
    @DisplayName("Should create customer successfully")
    void createCustomer_Success() {
        // Given
        when(customerRepository.findActiveByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        CustomerResponse response = customerService.createCustomer(customerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getMembershipLevel()).isEqualTo(CustomerMembership.REGULAR);
        assertThat(response.getTotalSpent()).isEqualByComparingTo("0");

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when creating customer with duplicate email")
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Given
        when(customerRepository.findActiveByEmail(anyString())).thenReturn(Optional.of(customer));

        // When/Then
        assertThatThrownBy(() -> customerService.createCustomer(customerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already exists");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should upgrade to GOLD when total spent >= 10,000,000")
    void updateTotalSpent_UpgradeToGold_Success() {
        // Given
        BigDecimal amount = new BigDecimal("15000000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, amount);

        // Then
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.GOLD);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("15000000");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should upgrade to PLATINUM when total spent >= 50,000,000")
    void updateTotalSpent_UpgradeToPlatinum_Success() {
        // Given
        customer.setMembershipLevel(CustomerMembership.GOLD);
        customer.setTotalSpent(new BigDecimal("20000000"));

        BigDecimal amount = new BigDecimal("35000000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, amount);

        // Then
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.PLATINUM);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("55000000");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should not downgrade membership")
    void updateTotalSpent_NoDowngrade() {
        // Given
        customer.setMembershipLevel(CustomerMembership.PLATINUM);
        customer.setTotalSpent(new BigDecimal("60000000"));

        BigDecimal smallAmount = new BigDecimal("1000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, smallAmount);

        // Then
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.PLATINUM);
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should upgrade from REGULAR directly to PLATINUM when spending >= 50,000,000")
    void updateTotalSpent_DirectToPlatinum_Success() {
        // Given
        BigDecimal amount = new BigDecimal("55000000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, amount);

        // Then
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.PLATINUM);
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should throw exception when customer not found for updateTotalSpent")
    void updateTotalSpent_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.updateTotalSpent(999L, BigDecimal.valueOf(1000)))
                .isInstanceOf(edts.week7_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining("999");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should find customer by id successfully")
    void findById_Success() {
        // Given
        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));

        // When
        CustomerResponse response = customerService.findById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        verify(customerRepository).findActiveById(1L);
    }

    @Test
    @DisplayName("Should throw exception when customer not found by id")
    void findById_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findActiveById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.findById(999L))
                .isInstanceOf(edts.week7_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining("999");

        verify(customerRepository).findActiveById(999L);
    }

    @Test
    @DisplayName("Should find all active customers")
    void findAllActive_Success() {
        // Given
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Customer> customerPage =
            new org.springframework.data.domain.PageImpl<>(java.util.List.of(customer));

        when(customerRepository.findAllActive(pageable)).thenReturn(customerPage);

        // When
        org.springframework.data.domain.Page<CustomerResponse> responses =
            customerService.findAllActive(pageable);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).getName()).isEqualTo("John Doe");
        verify(customerRepository).findAllActive(pageable);
    }

    @Test
    @DisplayName("Should find customers by membership level")
    void findByMembershipLevel_Success() {
        // Given
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Customer> customerPage =
            new org.springframework.data.domain.PageImpl<>(java.util.List.of(customer));

        when(customerRepository.findActiveByMembershipLevel(CustomerMembership.REGULAR, pageable))
            .thenReturn(customerPage);

        // When
        org.springframework.data.domain.Page<CustomerResponse> responses =
            customerService.findByMembershipLevel(CustomerMembership.REGULAR, pageable);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).getMembershipLevel()).isEqualTo(CustomerMembership.REGULAR);
        verify(customerRepository).findActiveByMembershipLevel(CustomerMembership.REGULAR, pageable);
    }

    @Test
    @DisplayName("Should search customers by keyword")
    void searchCustomers_Success() {
        // Given
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Customer> customerPage =
            new org.springframework.data.domain.PageImpl<>(java.util.List.of(customer));

        when(customerRepository.searchActiveCustomers("John", pageable)).thenReturn(customerPage);

        // When
        org.springframework.data.domain.Page<CustomerResponse> responses =
            customerService.searchCustomers("John", pageable);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).getName()).isEqualTo("John Doe");
        verify(customerRepository).searchActiveCustomers("John", pageable);
    }

    @Test
    @DisplayName("Should not upgrade GOLD customer to GOLD again")
    void updateTotalSpent_GoldToGold_NoChange() {
        // Given
        customer.setMembershipLevel(CustomerMembership.GOLD);
        customer.setTotalSpent(new BigDecimal("15000000"));

        BigDecimal amount = new BigDecimal("5000000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, amount);

        // Then
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.GOLD);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("20000000");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should not upgrade PLATINUM customer")
    void updateTotalSpent_Platinum_NoChange() {
        // Given
        customer.setMembershipLevel(CustomerMembership.PLATINUM);
        customer.setTotalSpent(new BigDecimal("60000000"));

        BigDecimal amount = new BigDecimal("10000000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, amount);

        // Then
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.PLATINUM);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("70000000");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should keep PLATINUM customer at PLATINUM even with GOLD threshold spending")
    void updateTotalSpent_PlatinumWithGoldThreshold_NoChange() {
        // Given
        customer.setMembershipLevel(CustomerMembership.PLATINUM);
        customer.setTotalSpent(new BigDecimal("60000000")); // Already PLATINUM

        // Add amount that keeps it in PLATINUM range but above GOLD threshold
        BigDecimal amount = new BigDecimal("5000000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, amount);

        // Then
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.PLATINUM);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("65000000");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should keep PLATINUM customer with spending below PLATINUM threshold")
    void updateTotalSpent_PlatinumBelowThreshold_NoChange() {
        // Given
        // Customer is PLATINUM but total spent is below 50M (e.g., manually set or data inconsistency)
        customer.setMembershipLevel(CustomerMembership.PLATINUM);
        customer.setTotalSpent(new BigDecimal("35000000")); // Below PLATINUM threshold but above GOLD

        // Add spending that keeps it below PLATINUM threshold
        BigDecimal amount = new BigDecimal("5000000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, amount);

        // Then
        // Should remain PLATINUM (never downgrade)
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.PLATINUM);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("40000000");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should keep GOLD customer when spending is below PLATINUM threshold")
    void updateTotalSpent_GoldBelowPlatinumThreshold_NoChange() {
        // Given
        customer.setMembershipLevel(CustomerMembership.GOLD);
        customer.setTotalSpent(new BigDecimal("15000000")); // Below PLATINUM threshold

        // Add spending that keeps it below PLATINUM threshold
        BigDecimal amount = new BigDecimal("5000000");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.updateTotalSpent(1L, amount);

        // Then
        // Should remain GOLD (not PLATINUM yet)
        assertThat(customer.getMembershipLevel()).isEqualTo(CustomerMembership.GOLD);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo("20000000");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should update customer successfully")
    void updateCustomer_Success() {
        // Given
        CustomerRequest updateRequest = new CustomerRequest("Jane Doe", "jane@example.com");

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        CustomerResponse response = customerService.updateCustomer(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Jane Doe");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(customer.getName()).isEqualTo("Jane Doe");
        assertThat(customer.getEmail()).isEqualTo("jane@example.com");

        verify(customerRepository).findActiveById(1L);
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent customer")
    void updateCustomer_NotFound_ThrowsException() {
        // Given
        CustomerRequest updateRequest = new CustomerRequest("Jane Doe", "jane@example.com");

        when(customerRepository.findActiveById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.updateCustomer(999L, updateRequest))
                .isInstanceOf(edts.week7_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining("999");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when email belongs to another customer")
    void updateCustomer_DuplicateEmail_ThrowsException() {
        // Given
        CustomerRequest updateRequest = new CustomerRequest("Jane Doe", "another@example.com");

        Customer anotherCustomer = new Customer();
        anotherCustomer.setId(2L);
        anotherCustomer.setName("Another Customer");
        anotherCustomer.setEmail("another@example.com");
        anotherCustomer.setMembershipLevel(CustomerMembership.REGULAR);
        anotherCustomer.setTotalSpent(BigDecimal.ZERO);
        anotherCustomer.setActive(true);

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findActiveByEmail("another@example.com"))
                .thenReturn(Optional.of(anotherCustomer));

        // When/Then
        assertThatThrownBy(() -> customerService.updateCustomer(1L, updateRequest))
                .isInstanceOf(edts.week7_practice1.exception.BusinessException.class)
                .hasMessageContaining("Email already exists");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should allow updating customer with same email (their own email)")
    void updateCustomer_SameEmail_Success() {
        // Given
        CustomerRequest updateRequest = new CustomerRequest("Jane Updated", customer.getEmail());

        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findActiveByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));  // Email belongs to same customer
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        CustomerResponse response = customerService.updateCustomer(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Jane Updated");
        assertThat(response.getEmail()).isEqualTo(customer.getEmail());

        verify(customerRepository).findActiveById(1L);
        verify(customerRepository).findActiveByEmail(customer.getEmail());
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should soft delete customer successfully")
    void deleteCustomer_Success() {
        // Given
        when(customerRepository.findActiveById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        customerService.deleteCustomer(1L);

        // Then
        assertThat(customer.getActive()).isFalse();
        verify(customerRepository).findActiveById(1L);
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent customer")
    void deleteCustomer_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findActiveById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.deleteCustomer(999L))
                .isInstanceOf(edts.week7_practice1.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining("999");

        verify(customerRepository, never()).save(any(Customer.class));
    }
}
