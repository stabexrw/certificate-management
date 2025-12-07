package com.seccertificate.certificateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final ObjectMapper objectMapper;

    @Value("${app.signature.secret:${APP_SIGNATURE_SECRET:ChangeMePlease}}")
    private String signatureSecret;

    @Value("${app.signature.key-id:v1}")
    private String signatureKeyId;

    private static final String HMAC_ALGO = "HmacSHA256";

    public String sign(String uniqueId, Map<String, String> data) {
        try {
            String payload = buildPayload(uniqueId, data);
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec secretKey = new SecretKeySpec(signatureSecret.getBytes(), HMAC_ALGO);
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(payload.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC signature", e);
        }
    }

    public boolean verify(String uniqueId, Map<String, String> data, String providedSignature) {
        String calculated = sign(uniqueId, data);
        return calculated.equals(providedSignature);
    }

    public String getCurrentKeyId() {
        return signatureKeyId;
    }

    private String buildPayload(String uniqueId, Map<String, String> data) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(data == null ? Map.of() : data);
        return uniqueId + ":" + json;
    }
}
