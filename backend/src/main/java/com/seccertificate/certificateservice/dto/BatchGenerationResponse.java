package com.seccertificate.certificateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGenerationResponse {
    private int totalRequested;
    private int successfullyQueued;
    private String batchId;
    private String message;
    private String estimatedCompletionTime;
}
