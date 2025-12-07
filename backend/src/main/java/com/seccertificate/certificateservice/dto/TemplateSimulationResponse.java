package com.seccertificate.certificateservice.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateSimulationResponse {
    
    private String previewHtml;
    private List<String> extractedPlaceholders;
    private boolean success;
    private String message;
}
