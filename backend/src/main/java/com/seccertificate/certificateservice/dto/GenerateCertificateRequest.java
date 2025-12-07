package com.seccertificate.certificateservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateCertificateRequest {
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
    
    @NotNull(message = "Certificate data is required")
    private Map<String, String> data;
    
    private String recipientName;
    private String recipientEmail;
}
