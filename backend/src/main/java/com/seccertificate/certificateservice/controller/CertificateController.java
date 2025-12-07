package com.seccertificate.certificateservice.controller;

import com.seccertificate.certificateservice.dto.ApiResponse;
import com.seccertificate.certificateservice.dto.CertificateDTO;
import com.seccertificate.certificateservice.dto.GenerateCertificateRequest;
import com.seccertificate.certificateservice.security.CustomUserDetails;
import com.seccertificate.certificateservice.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CertificateController {
    
    private final CertificateService certificateService;
    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<CertificateDTO>> generateCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GenerateCertificateRequest request) {
        CertificateDTO certificate = certificateService.generateCertificate(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Certificate generated successfully", certificate));
    }
    
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<String>> simulateCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GenerateCertificateRequest request) {
        // This endpoint simulates certificate generation without actually creating it
        // Used for preview in UI
        return ResponseEntity.ok(ApiResponse.success("Certificate preview generated", 
            "Preview functionality - actual PDF generation would happen here"));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CertificateDTO>>> getCertificates(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CertificateDTO> certificates = certificateService.getCertificatesByCustomerId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }
    
    @GetMapping("/{uniqueId}")
    public ResponseEntity<ApiResponse<CertificateDTO>> getCertificateByUniqueId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String uniqueId) {
        CertificateDTO certificate = certificateService.getCertificateByUniqueId(userDetails.getId(), uniqueId);
        return ResponseEntity.ok(ApiResponse.success(certificate));
    }
    
    @GetMapping("/{uniqueId}/download")
    public ResponseEntity<byte[]> downloadCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String uniqueId) throws IOException {
        byte[] pdfContent = certificateService.downloadCertificate(userDetails.getId(), uniqueId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate_" + uniqueId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }
}
