package com.seccertificate.certificateservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDTO {
    private Long id;
    
    private String uniqueId;
    
    private Long customerId;
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
    
    private String templateName;
    
    private Map<String, String> certificateData;
    
    private String recipientName;
    private String recipientEmail;
    
    private String filePath;
    private String downloadUrl;
    
    private String digitalSignature;
    private String qrCodeData;
    
    private String status;
    
    private LocalDateTime createdAt;
    private LocalDateTime downloadedAt;
    
    private Integer downloadCount;
}