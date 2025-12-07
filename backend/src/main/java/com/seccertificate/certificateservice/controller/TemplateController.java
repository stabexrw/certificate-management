package com.seccertificate.certificateservice.controller;

import com.seccertificate.certificateservice.dto.ApiResponse;
import com.seccertificate.certificateservice.dto.PageResponse;
import com.seccertificate.certificateservice.dto.TemplateDTO;
import com.seccertificate.certificateservice.dto.TemplateSimulationRequest;
import com.seccertificate.certificateservice.dto.TemplateSimulationResponse;
import com.seccertificate.certificateservice.security.CustomUserDetails;
import com.seccertificate.certificateservice.service.TemplateService;
import com.seccertificate.certificateservice.service.PdfGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TemplateController {
    
    private final TemplateService templateService;
    private final PdfGenerationService pdfGenerationService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<TemplateDTO>> createTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TemplateDTO templateDTO) {
        TemplateDTO created = templateService.createTemplate(userDetails.getId(), templateDTO);
        return ResponseEntity.ok(ApiResponse.success("Template created successfully", created));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateDTO>>> getTemplates(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TemplateDTO> templates = templateService.getTemplatesByCustomerId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PageResponse<TemplateDTO>>> getTemplatesPaginated(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString((String) direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        PageResponse<TemplateDTO> templates = templateService.getTemplatesPaginated(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateDTO>> getTemplateById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        TemplateDTO template = templateService.getTemplateById(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateDTO>> updateTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TemplateDTO templateDTO) {
        TemplateDTO updated = templateService.updateTemplate(userDetails.getId(), id, templateDTO);
        return ResponseEntity.ok(ApiResponse.success("Template updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        templateService.deleteTemplate(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully", null));
    }
    
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<TemplateSimulationResponse>> simulateTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TemplateSimulationRequest request) {
        TemplateSimulationResponse response = templateService.simulateTemplate(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Template simulation successful", response));
    }
    
    @PostMapping("/simulate/pdf")
    public ResponseEntity<byte[]> simulateTemplatePdf(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TemplateSimulationRequest request) throws IOException {
        
        TemplateDTO template = templateService.getTemplateById(userDetails.getId(), request.getTemplateId());
        byte[] pdfBytes = pdfGenerationService.generatePdfFromHtmlContent(
            template.getTemplateContent(), 
            request.getPlaceholderValues()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "certificate-preview.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @GetMapping("/{id}/placeholders")
    public ResponseEntity<ApiResponse<List<String>>> getTemplatePlaceholders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        TemplateDTO template = templateService.getTemplateById(userDetails.getId(), id);
        List<String> placeholders = templateService.extractPlaceholders(template.getTemplateContent());
        return ResponseEntity.ok(ApiResponse.success("Placeholders extracted successfully", placeholders));
    }
}
