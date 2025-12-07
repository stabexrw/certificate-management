package com.seccertificate.certificateservice.controller;

import com.seccertificate.certificateservice.dto.ApiResponse;
import com.seccertificate.certificateservice.dto.CustomerDTO;
import com.seccertificate.certificateservice.security.CustomUserDetails;
import com.seccertificate.certificateservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {
    
    private final CustomerService customerService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCurrentCustomer(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CustomerDTO customer = customerService.getCustomerById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(customer));
    }
    
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCurrentCustomer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CustomerDTO customerDTO) {
        CustomerDTO updated = customerService.updateCustomer(userDetails.getId(), customerDTO);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updated));
    }
    
    @PostMapping("/me/regenerate-api-key")
    public ResponseEntity<ApiResponse<String>> regenerateApiKey(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String newApiKey = customerService.regenerateApiKey(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("API key regenerated", newApiKey));
    }
    
    // Admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        CustomerDTO created = customerService.createCustomer(customerDTO);
        return ResponseEntity.ok(ApiResponse.success("Customer created successfully", created));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerById(@PathVariable Long id) {
        CustomerDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }
}