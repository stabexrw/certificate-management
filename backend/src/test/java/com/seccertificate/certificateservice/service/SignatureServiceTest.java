package com.seccertificate.certificateservice.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

@SpringBootTest
@TestPropertySource(properties = {"app.signature.secret=TestSecretKey12345"})
public class SignatureServiceTest {

    @Autowired
    private SignatureService signatureService;

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
