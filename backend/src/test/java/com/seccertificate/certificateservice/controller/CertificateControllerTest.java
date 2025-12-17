package com.seccertificate.certificateservice.controller;

import com.seccertificate.certificateservice.dto.ApiResponse;
import com.seccertificate.certificateservice.dto.CertificateDTO;
import com.seccertificate.certificateservice.dto.GenerateCertificateRequest;
import com.seccertificate.certificateservice.entity.Customer;
import com.seccertificate.certificateservice.security.CustomUserDetails;
import com.seccertificate.certificateservice.service.CertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateControllerTest {

    @Mock
    private CertificateService certificateService;

    private CertificateController controller;

    @BeforeEach
    void setup() {
        controller = new CertificateController(certificateService);
    }

    private CustomUserDetails userDetails() {
        return new CustomUserDetails(1L, "user@example.com", "pwd", "USER", Customer.CustomerStatus.ACTIVE);
    }

    @Test
    void generateCertificate_shouldReturnCertificate() {
        GenerateCertificateRequest req = GenerateCertificateRequest.builder()
                .templateId(5L)
                .data(Map.of("name", "John Doe"))
                .recipientName("John Doe")
                .recipientEmail("john@example.com")
                .build();

        CertificateDTO dto = new CertificateDTO();
        dto.setId(99L);
        dto.setUniqueId("cert-99");

        when(certificateService.generateCertificate(eq(1L), any(GenerateCertificateRequest.class)))
                .thenReturn(dto);

        ResponseEntity<ApiResponse<CertificateDTO>> response = controller.generateCertificate(userDetails(), req);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Certificate generated successfully", response.getBody().getMessage());
        assertEquals("cert-99", response.getBody().getData().getUniqueId());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void getCertificates_shouldReturnList() {
        CertificateDTO c1 = new CertificateDTO();
        c1.setId(1L);
        c1.setUniqueId("u1");
        CertificateDTO c2 = new CertificateDTO();
        c2.setId(2L);
        c2.setUniqueId("u2");

        when(certificateService.getCertificatesByCustomerId(1L)).thenReturn(List.of(c1, c2));

        ResponseEntity<ApiResponse<List<CertificateDTO>>> response = controller.getCertificates(userDetails());

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("u1", response.getBody().getData().get(0).getUniqueId());
        assertEquals("u2", response.getBody().getData().get(1).getUniqueId());
    }
}

