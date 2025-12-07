package com.seccertificate.certificateservice.controller;

import com.seccertificate.certificateservice.dto.ApiResponse;
import com.seccertificate.certificateservice.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicController {
    
    private final CertificateService certificateService;
    
    @GetMapping("/verify/{uniqueId}")
    public ResponseEntity<ApiResponse<Boolean>> verifyCertificate(
            @PathVariable String uniqueId,
            @RequestParam String signature) {
        boolean isValid = certificateService.verifyCertificate(uniqueId, signature);
        return ResponseEntity.ok(ApiResponse.success("Verification complete", isValid));
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Service is running"));
    }
}