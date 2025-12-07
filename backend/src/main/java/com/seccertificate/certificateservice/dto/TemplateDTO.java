package com.seccertificate.certificateservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateDTO {
    private Long id;
    
    private Long customerId;
    
    @NotBlank(message = "Template name is required")
    @Size(max = 200, message = "Template name must not exceed 200 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Template content is required")
    private String templateContent;
    
    private String type; // HTML, JSON, PDF_TEMPLATE
    
    private List<String> placeholders;
    
    private String status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
