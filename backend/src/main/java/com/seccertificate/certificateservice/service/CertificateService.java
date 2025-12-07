package com.seccertificate.certificateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.certificateservice.dto.CertificateDTO;
import com.seccertificate.certificateservice.dto.GenerateCertificateRequest;
import com.seccertificate.certificateservice.entity.Certificate;
import com.seccertificate.certificateservice.entity.Customer;
import com.seccertificate.certificateservice.entity.Template;
import com.seccertificate.certificateservice.exception.ResourceNotFoundException;
import com.seccertificate.certificateservice.repository.CertificateRepository;
import com.seccertificate.certificateservice.repository.CustomerRepository;
import com.seccertificate.certificateservice.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
public class CertificateService {
    
    private final CertificateRepository certificateRepository;
    private final TemplateRepository templateRepository;
    private final CustomerRepository customerRepository;
    private final PdfGenerationService pdfGenerationService;
    private final SignatureService signatureService;
    private final ObjectMapper objectMapper;
    private final com.seccertificate.certificateservice.repository.AuditLogRepository auditLogRepository;
    
    @Transactional
    public CertificateDTO generateCertificate(Long customerId, GenerateCertificateRequest request) {
        // Validate template ownership
        Template template = templateRepository.findByIdAndCustomerId(request.getTemplateId(), customerId)
                .orElseThrow(() -> new AccessDeniedException("Template not found or access denied"));
        
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found")); // customerId is always provided, so no null safety issue
        
        // Generate unique ID for certificate
        String uniqueId = UUID.randomUUID().toString();
        
        // Generate digital signature
        try {
            // Generate HMAC-based digital signature
            String digitalSignature = signatureService.sign(uniqueId, request.getData());

            // Generate QR code data for verification (include digital signature)
            String qrCodeData = pdfGenerationService.generateQRCode(uniqueId, customer.getId(), digitalSignature);

            // Generate PDF (embed QR code)
            String filePath = pdfGenerationService.generateCertificatePdf(
                template,
                request.getData(),
                uniqueId,
                qrCodeData
            );

            // Create certificate entity
            Certificate certificate = Certificate.builder()
                .uniqueId(uniqueId)
                .customer(customer)
                .template(template)
                .filePath(filePath)
                .certificateData(serializeData(request.getData()))
                .recipientName(request.getRecipientName())
                .recipientEmail(request.getRecipientEmail())
                .digitalSignature(digitalSignature)
                .signatureKeyId(signatureService.getCurrentKeyId())
                .qrCodeData(qrCodeData)
                .status(Certificate.CertificateStatus.GENERATED)
                .downloadCount(0)
                .build();
            
            certificate = certificateRepository.save(certificate); // certificate is always built, so no null safety issue

            // Audit log: certificate generated
            com.seccertificate.certificateservice.entity.AuditLog audit = com.seccertificate.certificateservice.entity.AuditLog.builder()
                    .customerId(customer.getId())
                    .action("GENERATE_CERTIFICATE")
                    .entityType("CERTIFICATE")
                    .entityId(certificate.getId())
                    .details("{\"recipientName\":\"" + request.getRecipientName() + "\",\"recipientEmail\":\"" + request.getRecipientEmail() + "\"}")
                    .build();
            auditLogRepository.save(audit); // audit is always built, so no null safety issue

            return mapToDTO(certificate);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }
    
    @Transactional(readOnly = true)
    public CertificateDTO getCertificateById(Long customerId, Long certificateId) {
        Certificate certificate = certificateRepository.findByIdAndCustomerId(certificateId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found or access denied"));
        return mapToDTO(certificate);
    }
    
    @Transactional(readOnly = true)
    public CertificateDTO getCertificateByUniqueId(Long customerId, String uniqueId) {
        Certificate certificate = certificateRepository.findByUniqueIdAndCustomerId(uniqueId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found or access denied"));
        return mapToDTO(certificate);
    }
    
    @Transactional(readOnly = true)
    public List<CertificateDTO> getCertificatesByCustomerId(Long customerId) {
        return certificateRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public byte[] downloadCertificate(Long customerId, String uniqueId) throws IOException {
        Certificate certificate = certificateRepository.findByUniqueIdAndCustomerId(uniqueId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found or access denied"));
        
        // Update download statistics
        certificate.setDownloadCount(certificate.getDownloadCount() + 1);
        certificate.setDownloadedAt(LocalDateTime.now());
        certificate.setStatus(Certificate.CertificateStatus.DOWNLOADED);
        certificateRepository.save(certificate);

        // Audit log: certificate downloaded
        com.seccertificate.certificateservice.entity.AuditLog downloadAudit = com.seccertificate.certificateservice.entity.AuditLog.builder()
                .customerId(certificate.getCustomer().getId())
                .action("DOWNLOAD_CERTIFICATE")
                .entityType("CERTIFICATE")
                .entityId(certificate.getId())
                .details("{\"uniqueId\":\"" + certificate.getUniqueId() + "\"}")
                .build();
        auditLogRepository.save(downloadAudit);

        return pdfGenerationService.readCertificateFile(certificate.getFilePath());
    }
    
    @Transactional(readOnly = true)
    public boolean verifyCertificate(String uniqueId, String digitalSignature) {
        Certificate certificate = certificateRepository.findByUniqueId(uniqueId)
                .orElse(null);
        
        if (certificate == null) {
            return false;
        }

        // Recompute signature from stored certificate data to verify integrity
        try {
            Map<String, String> storedData = objectMapper.readValue(certificate.getCertificateData(), new TypeReference<>() {});
            return signatureService.verify(uniqueId, storedData, digitalSignature);
        } catch (IOException e) {
            return false;
        }
    }
    
    private String serializeData(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing certificate data", e);
        }
    }
    
    private CertificateDTO mapToDTO(Certificate certificate) {
        return CertificateDTO.builder()
                .id(certificate.getId())
                .uniqueId(certificate.getUniqueId())
                .customerId(certificate.getCustomer().getId())
                .templateId(certificate.getTemplate().getId())
                .templateName(certificate.getTemplate().getName())
                .recipientName(certificate.getRecipientName())
                .recipientEmail(certificate.getRecipientEmail())
                .filePath(certificate.getFilePath())
                .downloadUrl("/api/certificates/" + certificate.getUniqueId() + "/download")
                .digitalSignature(certificate.getDigitalSignature())
                .qrCodeData(certificate.getQrCodeData())
                .status(certificate.getStatus().name())
                .createdAt(certificate.getCreatedAt())
                .downloadedAt(certificate.getDownloadedAt())
                .downloadCount(certificate.getDownloadCount())
                .build();
    }
}