package com.seccertificate.certificateservice.repository;

import com.seccertificate.certificateservice.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    
    // Find all templates for a specific customer
    List<Template> findByCustomerId(Long customerId);
    
    Page<Template> findByCustomerId(Long customerId, Pageable pageable);
    
    // Find template by ID and customer ID (for security)
    @Query("SELECT t FROM Template t WHERE t.id = :templateId AND t.customer.id = :customerId")
    Optional<Template> findByIdAndCustomerId(@Param("templateId") Long templateId, 
                                             @Param("customerId") Long customerId);
    
    // Find active templates
    @Query("SELECT t FROM Template t WHERE t.customer.id = :customerId AND t.status = 'ACTIVE'")
    List<Template> findActiveTemplatesByCustomerId(@Param("customerId") Long customerId);
    
    // Check if template exists for customer
    boolean existsByIdAndCustomerId(Long id, Long customerId);
    
    // Count templates by customer
    long countByCustomerId(Long customerId);
}