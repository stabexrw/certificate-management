package com.seccertificate.certificateservice.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class SignatureServiceTest {

    private SignatureService signatureService;

    @BeforeEach
    void setup() {
        signatureService = new SignatureService(new ObjectMapper());
        // Inject deterministic secret for repeatable tests without Spring context
        ReflectionTestUtils.setField(signatureService, "signatureSecret", "TestSecretKey12345");
        ReflectionTestUtils.setField(signatureService, "signatureKeyId", "v1-test");
    }

    @Test
    void signAndVerify_shouldReturnTrue() {
        String uniqueId = "abc-123";
        Map<String, String> data = Map.of("name", "Alice", "course", "Security");

        String sig = signatureService.sign(uniqueId, data);
        assertNotNull(sig);

        boolean ok = signatureService.verify(uniqueId, data, sig);
        assertTrue(ok, "Signature should verify with same data and key");
    }
}
