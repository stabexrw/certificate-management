package com.seccertificate.certificateservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "templates", indexes = {
    @Index(name = "idx_template_customer", columnList = "customer_id"),
    @Index(name = "idx_template_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String templateContent; // JSON structure or HTML
    
    @Column(nullable = false)
    @Builder.Default
    private TemplateType type = TemplateType.HTML;
    
    @Column(columnDefinition = "TEXT")
    private String placeholders; // JSON array of placeholder names
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TemplateStatus status = TemplateStatus.ACTIVE;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Certificate> certificates = new ArrayList<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum TemplateType {
        HTML, JSON, PDF_TEMPLATE
    }
    
    public enum TemplateStatus {
        ACTIVE, INACTIVE, ARCHIVED
    }
}
