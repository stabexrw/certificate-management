package com.seccertificate.certificateservice.repository;

import com.seccertificate.certificateservice.entity.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    // Find certificate by unique ID
    Optional<Certificate> findByUniqueId(String uniqueId);
    
    // Find certificates for a customer
    Page<Certificate> findByCustomerId(Long customerId, Pageable pageable);
    
    List<Certificate> findByCustomerId(Long customerId);
    
    // Find certificate by ID and customer ID (security check)
    @Query("SELECT c FROM Certificate c WHERE c.id = :certificateId AND c.customer.id = :customerId")
    Optional<Certificate> findByIdAndCustomerId(@Param("certificateId") Long certificateId, 
                                                @Param("customerId") Long customerId);
    
    // Find by unique ID and customer ID
    @Query("SELECT c FROM Certificate c WHERE c.uniqueId = :uniqueId AND c.customer.id = :customerId")
    Optional<Certificate> findByUniqueIdAndCustomerId(@Param("uniqueId") String uniqueId, 
                                                       @Param("customerId") Long customerId);
    
    // Find certificates by template
    List<Certificate> findByTemplateId(Long templateId);
    
    // Count certificates by customer
    long countByCustomerId(Long customerId);
    
    // Count certificates by template
    long countByTemplateId(Long templateId);
    
    // Find certificates created in date range
    @Query("SELECT c FROM Certificate c WHERE c.customer.id = :customerId " +
           "AND c.createdAt BETWEEN :startDate AND :endDate")
    List<Certificate> findByCustomerIdAndDateRange(@Param("customerId") Long customerId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    // Verify certificate exists
    boolean existsByUniqueId(String uniqueId);
    
    boolean existsByIdAndCustomerId(Long id, Long customerId);
}