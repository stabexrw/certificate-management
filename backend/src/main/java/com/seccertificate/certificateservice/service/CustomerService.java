package com.seccertificate.certificateservice.service;

import com.seccertificate.certificateservice.dto.CustomerDTO;
import com.seccertificate.certificateservice.entity.Customer;
import com.seccertificate.certificateservice.exception.ResourceNotFoundException;
import com.seccertificate.certificateservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        if (customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Customer customer = Customer.builder()
                .companyName(customerDTO.getCompanyName())
                .email(customerDTO.getEmail())
                .password(passwordEncoder.encode(customerDTO.getPassword()))
                .apiKey(generateApiKey())
                .status(Customer.CustomerStatus.ACTIVE)
                .role("CUSTOMER")
                .build();
        
        customer = customerRepository.save(customer);
        return mapToDTO(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToDTO(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return mapToDTO(customer);
    }
    
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        
        customer.setCompanyName(customerDTO.getCompanyName());
        if (customerDTO.getPassword() != null && !customerDTO.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customerDTO.getPassword()));
        }
        
        customer = customerRepository.save(customer);
        return mapToDTO(customer);
    }
    
    @Transactional
    public String regenerateApiKey(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        customer.setApiKey(generateApiKey());
        customerRepository.save(customer);
        return customer.getApiKey();
    }
    
    private String generateApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    private CustomerDTO mapToDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .companyName(customer.getCompanyName())
                .email(customer.getEmail())
                .apiKey(customer.getApiKey())
                .status(customer.getStatus().name())
                .role(customer.getRole())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
