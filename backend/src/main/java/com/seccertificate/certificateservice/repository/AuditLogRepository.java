package com.seccertificate.certificateservice.repository;

import com.seccertificate.certificateservice.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find logs by customer
    Page<AuditLog> findByCustomerId(Long customerId, Pageable pageable);
    
    // Find logs by action
    List<AuditLog> findByAction(String action);
    
    // Find logs in date range
    @Query("SELECT a FROM AuditLog a WHERE a.customerId = :customerId " +
           "AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByCustomerIdAndDateRange(@Param("customerId") Long customerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    // Find recent activity
    @Query("SELECT a FROM AuditLog a WHERE a.customerId = :customerId " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findRecentActivityByCustomerId(@Param("customerId") Long customerId, 
                                                   Pageable pageable);
}