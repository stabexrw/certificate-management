package com.seccertificate.certificateservice.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.seccertificate.certificateservice.entity.Template;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class PdfGenerationService {
    
    @Value("${app.certificate.storage-path:./certificates}")
    private String storagePath;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    public String generateCertificatePdf(Template template, Map<String, String> data, String uniqueId, String qrCodeUrl) 
            throws IOException {
        
        // Create storage directory if it doesn't exist
        Path directoryPath = Paths.get(storagePath);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
        
        // Replace placeholders in template
        String processedContent = replacePlaceholders(template.getTemplateContent(), data);
        
        // Generate PDF file path
        String fileName = String.format("certificate_%s.pdf", uniqueId);
        String filePath = Paths.get(storagePath, fileName).toString();
        
        // Generate PDF (embed QR code in the left corner)
        if (template.getType() == Template.TemplateType.HTML) {
            generatePdfFromHtml(processedContent, filePath, uniqueId, qrCodeUrl);
        } else {
            generateSimplePdf(processedContent, filePath, uniqueId, qrCodeUrl);
        }
        
        log.info("Certificate PDF generated: {}", filePath);
        return filePath;
    }
    
    private void generatePdfFromHtml(String htmlContent, String outputPath, String uniqueId, String qrCodeUrl) 
            throws IOException {
        
        // Add watermark and unique ID to HTML
        String enhancedHtml = addWatermarkToHtml(htmlContent, uniqueId);

        // If template author placed a placeholder `{{qr_image}}` in HTML, replace it with an inline base64 PNG
        if (qrCodeUrl != null && !qrCodeUrl.isBlank()) {
            try {
                String qrBase64 = generateQrBase64(qrCodeUrl, 200);
                String inlineImg = String.format("<img src=\"data:image/png;base64,%s\" style=\"width:100px;height:100px;\"/>", qrBase64);

                if (enhancedHtml.contains("{{qr_image}}")) {
                    enhancedHtml = enhancedHtml.replace("{{qr_image}}", inlineImg);
                } else {
                    // Fallback: embed QR at fixed top-left position if placeholder not present
                    String qrImg = String.format("<img src=\"data:image/png;base64,%s\" style=\"position: fixed; left: 10px; top: 10px; width: 100px; height: 100px; z-index:9999;\"/>", qrBase64);
                    if (enhancedHtml.contains("</body>")) {
                        enhancedHtml = enhancedHtml.replace("</body>", qrImg + "</body>");
                    } else {
                        enhancedHtml = qrImg + enhancedHtml;
                    }
                }
            } catch (WriterException e) {
                log.warn("Failed to generate QR code for PDF: {}", e.getMessage());
            }
        }
        
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            ConverterProperties converterProperties = new ConverterProperties();
            HtmlConverter.convertToPdf(enhancedHtml, fos, converterProperties);
        }
    }
    
    private void generateSimplePdf(String content, String outputPath, String uniqueId, String qrCodeUrl) 
            throws IOException {
        
        try (PdfWriter writer = new PdfWriter(outputPath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Add content
            document.add(new Paragraph(content));
            
            // Add watermark/unique ID
            document.add(new Paragraph("\n\n")
                    .setFontSize(8)
                    .setItalic());
            document.add(new Paragraph("Certificate ID: " + uniqueId)
                    .setFontSize(8)
                    .setItalic());

            // Add QR code image at top-left if provided
            if (qrCodeUrl != null && !qrCodeUrl.isBlank()) {
                try {
                    String qrBase64 = generateQrBase64(qrCodeUrl, 200);
                    byte[] imgBytes = Base64.getDecoder().decode(qrBase64);
                    ImageData imgData = ImageDataFactory.create(imgBytes);
                    Image img = new Image(imgData);
                    // position near top-left (1 inch margin = 72 units)
                    float x = 36f;
                    float y = pdf.getDefaultPageSize().getHeight() - 36f - 100f; // top minus height
                    img.setFixedPosition(x, y);
                    img.scaleToFit(100f, 100f);
                    document.add(img);
                } catch (WriterException e) {
                    log.warn("Failed to generate QR code for simple PDF: {}", e.getMessage());
                }
            }
        }
    }
    
    private String replacePlaceholders(String template, Map<String, String> data) {
        String result = template;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }
    
    private String addWatermarkToHtml(String html, String uniqueId) {
        // Add unique ID watermark to HTML
        String watermark = String.format(
            "<div style='position: fixed; bottom: 10px; right: 10px; font-size: 8px; color: #ccc;'>" +
            "Certificate ID: %s</div>", uniqueId);
        
        if (html.contains("</body>")) {
            return html.replace("</body>", watermark + "</body>");
        }
        return html + watermark;
    }

    private String generateQrBase64(String text, int size) throws WriterException, IOException {
        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrWriter.encode(text, BarcodeFormat.QR_CODE, size, size);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
    
    public String generateQRCode(String uniqueId, Long customerId, String digitalSignature) {
        // Generate verification URL for QR code pointing to frontend verify route
        String encodedSignature = URLEncoder.encode(digitalSignature, StandardCharsets.UTF_8);
        String base = frontendUrl != null && !frontendUrl.isBlank() ? frontendUrl.replaceAll("/+$", "") : "http://localhost:4200";
        return String.format("%s/verify/%s?customer=%d&signature=%s", base, uniqueId, customerId, encodedSignature);
    }
    
    public byte[] readCertificateFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }
    
    /**
     * Generate PDF from HTML content with placeholder replacement (for simulation/preview)
     */
    public byte[] generatePdfFromHtmlContent(String htmlContent, Map<String, String> placeholderValues) throws IOException {
        try {
            // Replace placeholders with actual values
            String processedHtml = replacePlaceholders(htmlContent, placeholderValues);
            
            // Add CSS to ensure proper rendering
            String styledHtml = wrapHtmlWithStyles(processedHtml);
            
            // Generate PDF using iText
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ConverterProperties converterProperties = new ConverterProperties();
            
            HtmlConverter.convertToPdf(styledHtml, outputStream, converterProperties);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF from HTML", e);
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate multiple PDFs in batch
     */
    public java.util.List<byte[]> generateBatchPdfs(String htmlTemplate, java.util.List<Map<String, String>> batchData) throws IOException {
        return batchData.stream()
                .map(data -> {
                    try {
                        return generatePdfFromHtmlContent(htmlTemplate, data);
                    } catch (IOException e) {
                        log.error("Error generating PDF in batch", e);
                        return null;
                    }
                })
                .filter(pdf -> pdf != null)
                .toList();
    }
    
    /**
     * Wrap HTML content with necessary styles for proper PDF rendering
     */
    private String wrapHtmlWithStyles(String htmlContent) {
        // If HTML already has DOCTYPE and html tags, don't wrap again
        if (htmlContent.trim().toLowerCase().startsWith("<!doctype") || 
            htmlContent.trim().toLowerCase().startsWith("<html")) {
            return htmlContent;
        }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    @page {
                        size: A4 landscape;
                        margin: 0;
                    }
                    body {
                        margin: 0;
                        padding: 20px;
                        font-family: 'Arial', sans-serif;
                    }
                    * {
                        box-sizing: border-box;
                    }
                </style>
            </head>
            <body>
                %s
            </body>
            </html>
            """.formatted(htmlContent);
    }
}
