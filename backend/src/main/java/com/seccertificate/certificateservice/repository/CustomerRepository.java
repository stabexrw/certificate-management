package com.seccertificate.certificateservice.repository;

import com.seccertificate.certificateservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByApiKey(String apiKey);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.status = 'ACTIVE'")
    Optional<Customer> findActiveCustomerByEmail(String email);
    
    @Query("SELECT c FROM Customer c WHERE c.apiKey = :apiKey AND c.status = 'ACTIVE'")
    Optional<Customer> findActiveCustomerByApiKey(String apiKey);
}