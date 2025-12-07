package com.seccertificate.certificateservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateBatchRequest {
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
    
    @NotEmpty(message = "At least one certificate data set is required")
    private List<Map<String, String>> certificatesData;
}
