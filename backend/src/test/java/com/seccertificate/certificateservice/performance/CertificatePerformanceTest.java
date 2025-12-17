package com.seccertificate.certificateservice.performance;

import com.seccertificate.certificateservice.dto.CertificateBatchRequest;
import com.seccertificate.certificateservice.dto.CertificateDTO;
import com.seccertificate.certificateservice.dto.GenerateCertificateRequest;
import com.seccertificate.certificateservice.service.CertificateService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance tests for certificate generation
 * These tests are disabled by default and should be run manually to validate performance
 */
@SpringBootTest
class CertificatePerformanceTest {
    
    @Autowired
    private CertificateService certificateService;
    
    @Test
    void shouldGenerate100CertificatesUnder60Seconds() throws Exception {
        // Arrange
        List<CompletableFuture<CertificateDTO>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        // Act - Generate 100 certificates asynchronously
        for (int i = 0; i < 100; i++) {
            GenerateCertificateRequest req = GenerateCertificateRequest.builder()
                .templateId(1L)
                .data(Map.of(
                    "name", "Test User " + i,
                    "course", "Performance Testing",
                    "date", "2025-12-17"
                ))
                .recipientName("User " + i)
                .recipientEmail("user" + i + "@test.com")
                .build();
            
            futures.add(certificateService.generateCertificateAsync(1L, req));
        }
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long duration = System.currentTimeMillis() - startTime;
        long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        
        // Assert
        assertThat(duration).isLessThan(60_000); // 60 seconds
        assertThat(futures).allMatch(f -> !f.isCompletedExceptionally());
        
        System.out.println("✅ Generated 100 certificates in " + durationSeconds + " seconds");
        System.out.println("📊 Throughput: " + (100.0 / durationSeconds) + " certificates/second");
    }
    
    @Test
    void shouldGenerate1000CertificatesUnder60Seconds() throws Exception {
        // Arrange
        List<CompletableFuture<CertificateDTO>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        // Act - Generate 1000 certificates asynchronously
        for (int i = 0; i < 1000; i++) {
            GenerateCertificateRequest req = GenerateCertificateRequest.builder()
                .templateId(1L)
                .data(Map.of(
                    "name", "Test User " + i,
                    "course", "Performance Testing",
                    "date", "2025-12-17"
                ))
                .recipientName("User " + i)
                .recipientEmail("user" + i + "@test.com")
                .build();
            
            futures.add(certificateService.generateCertificateAsync(1L, req));
        }
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long duration = System.currentTimeMillis() - startTime;
        long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        
        // Assert
        assertThat(duration).isLessThan(60_000); // 60 seconds
        assertThat(futures).allMatch(f -> !f.isCompletedExceptionally());
        
        System.out.println("✅ Generated 1000 certificates in " + durationSeconds + " seconds");
        System.out.println("📊 Throughput: " + (1000.0 / durationSeconds) + " certificates/second");
        System.out.println("📈 Certificates per minute: " + (1000.0 / durationSeconds * 60));
    }
    
    @Test
    void shouldGenerateBatch100CertificatesUnder10Seconds() throws Exception {
        // Arrange
        List<GenerateCertificateRequest> requests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            requests.add(GenerateCertificateRequest.builder()
                .templateId(1L)
                .data(Map.of(
                    "name", "Batch User " + i,
                    "course", "Batch Testing",
                    "date", "2025-12-17"
                ))
                .recipientName("User " + i)
                .recipientEmail("user" + i + "@test.com")
                .build());
        }
        
        CertificateBatchRequest batchRequest = CertificateBatchRequest.builder()
            .templateId(1L)
            .certificates(requests)
            .build();
        
        long startTime = System.currentTimeMillis();
        
        // Act
        CompletableFuture<List<CertificateDTO>> future = 
            certificateService.generateBatchCertificates(1L, batchRequest);
        
        List<CertificateDTO> results = future.join();
        
        long duration = System.currentTimeMillis() - startTime;
        long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        
        // Assert
        assertThat(duration).isLessThan(10_000); // 10 seconds for batch
        assertThat(results).hasSize(100);
        
        System.out.println("✅ Generated 100 certificates (batch) in " + durationSeconds + " seconds");
        System.out.println("📊 Batch throughput: " + (100.0 / durationSeconds) + " certificates/second");
    }
    
    @Test
    void shouldGenerate10BatchesOf100Concurrently() throws Exception {
        // Arrange - 10 batches of 100 = 1000 certificates total
        List<CompletableFuture<List<CertificateDTO>>> batchFutures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        // Act
        for (int batch = 0; batch < 10; batch++) {
            List<GenerateCertificateRequest> requests = new ArrayList<>();
            
            for (int i = 0; i < 100; i++) {
                int certNum = (batch * 100) + i;
                requests.add(GenerateCertificateRequest.builder()
                    .templateId(1L)
                    .data(Map.of(
                        "name", "Multi-Batch User " + certNum,
                        "course", "Concurrent Testing",
                        "date", "2025-12-17"
                    ))
                    .recipientName("User " + certNum)
                    .recipientEmail("user" + certNum + "@test.com")
                    .build());
            }
            
            CertificateBatchRequest batchRequest = CertificateBatchRequest.builder()
                .templateId(1L)
                .certificates(requests)
                .build();
            
            batchFutures.add(certificateService.generateBatchCertificates(1L, batchRequest));
        }
        
        // Wait for all batches
        CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();
        
        long duration = System.currentTimeMillis() - startTime;
        long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        
        int totalCertificates = batchFutures.stream()
            .mapToInt(f -> f.join().size())
            .sum();
        
        // Assert
        assertThat(totalCertificates).isEqualTo(1000);
        assertThat(batchFutures).allMatch(f -> !f.isCompletedExceptionally());
        
        System.out.println("✅ Generated 1000 certificates (10 batches) in " + durationSeconds + " seconds");
        System.out.println("📊 Total throughput: " + (1000.0 / durationSeconds) + " certificates/second");
        System.out.println("📈 Certificates per minute: " + (1000.0 / durationSeconds * 60));
    }
}
