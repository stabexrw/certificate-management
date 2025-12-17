package com.seccertificate.certificateservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateBatchRequest {
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
    
    @NotEmpty(message = "At least one certificate data set is required")
        @Size(max = 1000, message = "Maximum 1000 certificates per batch")
        private List<GenerateCertificateRequest> certificates;
}
