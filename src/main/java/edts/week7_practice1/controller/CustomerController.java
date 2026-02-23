package edts.week7_practice1.controller;

import edts.week7_practice1.dto.customer.CustomerRequest;
import edts.week7_practice1.dto.customer.CustomerResponse;
import edts.week7_practice1.enums.CustomerMembership;
import edts.week7_practice1.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        logger.info("GET /api/customers - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CustomerResponse> customers = customerService.findAllActive(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
        logger.info("GET /api/customers/{}", id);
        CustomerResponse customer = customerService.findById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/membership/{level}")
    public ResponseEntity<Page<CustomerResponse>> findByMembership(
            @PathVariable CustomerMembership level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("GET /api/customers/membership/{}", level);
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerResponse> customers = customerService.findByMembershipLevel(level, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("GET /api/customers/search?keyword={}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerResponse> customers = customerService.searchCustomers(keyword, pageable);
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        logger.info("POST /api/customers - Creating customer: {}", request.getName());
        CustomerResponse customer = customerService.createCustomer(request);
        return ResponseEntity
                .created(URI.create("/api/customers/" + customer.getId()))
                .body(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request
    ) {
        logger.info("PUT /api/customers/{} - Updating customer", id);
        CustomerResponse customer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/customers/{}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
