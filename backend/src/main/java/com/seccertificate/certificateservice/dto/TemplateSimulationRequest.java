package com.seccertificate.certificateservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateSimulationRequest {
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
    
    @NotNull(message = "Placeholder values are required")
    private Map<String, String> placeholderValues;
}
