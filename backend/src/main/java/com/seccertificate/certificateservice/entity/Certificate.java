package com.seccertificate.certificateservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates", indexes = {
    @Index(name = "idx_cert_customer", columnList = "customer_id"),
    @Index(name = "idx_cert_template", columnList = "template_id"),
    @Index(name = "idx_cert_unique_id", columnList = "uniqueId"),
    @Index(name = "idx_cert_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String uniqueId; // UUID for verification
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    @Column(nullable = false, length = 500)
    private String filePath; // Path to PDF file
    
    @Column(columnDefinition = "TEXT")
    private String certificateData; // JSON of actual data used
    
    @Column(length = 200)
    private String recipientName;
    
    @Column(length = 200)
    private String recipientEmail;
    
    @Column(nullable = false)
    private String digitalSignature; // SHA-256 hash for verification
    
    @Column(length = 40)
    private String signatureKeyId;

    @Column(length = 500)
    private String qrCodeData; // QR code for verification
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.GENERATED;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime downloadedAt;
    
    @Column
    @Builder.Default
    private Integer downloadCount = 0;
    
    public enum CertificateStatus {
        GENERATED, DOWNLOADED, VERIFIED, REVOKED
    }
}
