package edts.week8_practice1.service;

import edts.week8_practice1.dto.customer.CustomerRequest;
import edts.week8_practice1.dto.customer.CustomerResponse;
import edts.week8_practice1.entity.Customer;
import edts.week8_practice1.enums.CustomerMembership;
import edts.week8_practice1.exception.BusinessException;
import edts.week8_practice1.exception.ResourceNotFoundException;
import edts.week8_practice1.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
@Transactional
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private static final BigDecimal GOLD_THRESHOLD = new BigDecimal("10000000");
    private static final BigDecimal PLATINUM_THRESHOLD = new BigDecimal("50000000");

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAllActive(Pageable pageable) {
        logger.info("Finding all active customers");
        Page<Customer> customers = customerRepository.findAllActive(pageable);
        return customers.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        logger.info("Finding customer by id: {}", id);
        Customer customer = customerRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findByMembershipLevel(CustomerMembership level, Pageable pageable) {
        logger.info("Finding customers by membership level: {}", level);
        Page<Customer> customers = customerRepository.findActiveByMembershipLevel(level, pageable);
        return customers.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String keyword, Pageable pageable) {
        logger.info("Searching customers with keyword: {}", keyword);
        Page<Customer> customers = customerRepository.searchActiveCustomers(keyword, pageable);
        return customers.map(this::mapToResponse);
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        logger.info("Creating new customer: {}", request.getEmail());

        // Check duplicate email
        customerRepository.findActiveByEmail(request.getEmail())
                .ifPresent(c -> {
                    throw new BusinessException("DUPLICATE_ERROR",
                            "Email already exists",
                            Arrays.asList("Customer with email '" + request.getEmail() + "' already exists"));
                });

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setMembershipLevel(CustomerMembership.REGULAR);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setActive(true);

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer created successfully with id: {}", savedCustomer.getId());
        return mapToResponse(savedCustomer);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        logger.info("Updating customer with id: {}", id);

        Customer customer = customerRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Check duplicate email (excluding current customer)
        customerRepository.findActiveByEmail(request.getEmail())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        throw new BusinessException("DUPLICATE_ERROR",
                                "Email already exists",
                                Arrays.asList("Customer with email '" + request.getEmail() + "' already exists"));
                    }
                });

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer updated successfully: {}", savedCustomer.getId());
        return mapToResponse(savedCustomer);
    }

    public void deleteCustomer(Long id) {
        logger.info("Soft deleting customer with id: {}", id);

        Customer customer = customerRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setActive(false);
        customerRepository.save(customer);
        logger.info("Customer soft deleted successfully: {}", id);
    }

    public void updateTotalSpent(Long customerId, BigDecimal amount) {
        logger.info("Updating total spent for customer {}: {}", customerId, amount);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        BigDecimal newTotalSpent = customer.getTotalSpent().add(amount);
        customer.setTotalSpent(newTotalSpent);

        // Auto-upgrade membership
        updateMembership(customer);

        customerRepository.save(customer);
        logger.info("Customer total spent updated: {}, membership: {}",
                newTotalSpent, customer.getMembershipLevel());
    }

    private void updateMembership(Customer customer) {
        CustomerMembership currentLevel = customer.getMembershipLevel();
        BigDecimal totalSpent = customer.getTotalSpent();

        // Only upgrade, never downgrade
        if (totalSpent.compareTo(PLATINUM_THRESHOLD) >= 0 &&
                currentLevel != CustomerMembership.PLATINUM) {
            logger.info("Upgrading customer {} to PLATINUM", customer.getId());
            customer.setMembershipLevel(CustomerMembership.PLATINUM);
        } else if (totalSpent.compareTo(GOLD_THRESHOLD) >= 0 &&
                currentLevel == CustomerMembership.REGULAR) {
            logger.info("Upgrading customer {} to GOLD", customer.getId());
            customer.setMembershipLevel(CustomerMembership.GOLD);
        }
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getMembershipLevel(),
                customer.getTotalSpent(),
                customer.getActive()
        );
    }
}
