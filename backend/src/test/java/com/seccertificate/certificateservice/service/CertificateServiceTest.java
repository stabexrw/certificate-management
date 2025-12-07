package com.seccertificate.certificateservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.certificateservice.entity.Certificate;
import com.seccertificate.certificateservice.repository.CertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private PdfGenerationService pdfGenerationService;

    @Mock
    private SignatureService signatureService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private com.seccertificate.certificateservice.service.CertificateService certificateService;

    private Certificate certificate;

    @BeforeEach
    void setup() throws Exception {
        certificate = Certificate.builder()
                .id(1L)
                .uniqueId("u-1")
                .certificateData('{'+"\"name\":\"Alice\""+'}')
                .digitalSignature("sig1")
                .build();
    }

    @Test
    void verifyCertificate_shouldReturnTrueWhenSignatureMatches() throws Exception {
        when(certificateRepository.findByUniqueId("u-1")).thenReturn(Optional.of(certificate));

        // stub reading stored data (use TypeReference signature)
        when(objectMapper.readValue(eq(certificate.getCertificateData()), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(Map.of("name", "Alice"));
        when(signatureService.verify(eq("u-1"), anyMap(), eq("sig1"))).thenReturn(true);

        boolean res = certificateService.verifyCertificate("u-1", "sig1");
        assertTrue(res);
    }

    @Test
    void verifyCertificate_shouldReturnFalseWhenNotFound() {
        when(certificateRepository.findByUniqueId("missing")).thenReturn(Optional.empty());
        boolean res = certificateService.verifyCertificate("missing", "xxx");
        assertFalse(res);
    }
}
