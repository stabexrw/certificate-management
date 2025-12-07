# Backend Integration Requirements

## Overview

This document outlines the backend changes needed to support the new certificate template and generation system.

## 1. Database Schema Updates

### Template Table Enhancement

Add new columns to store canvas data:

```sql
ALTER TABLE templates ADD COLUMN canvas_json LONGTEXT;
ALTER TABLE templates ADD COLUMN design_elements JSON;
ALTER TABLE templates ADD COLUMN template_category VARCHAR(50);
ALTER TABLE templates ADD COLUMN dimensions_width INT DEFAULT 800;
ALTER TABLE templates ADD COLUMN dimensions_height INT DEFAULT 600;
ALTER TABLE templates ADD COLUMN exported_at TIMESTAMP;
```

### JPA Entity Update

```java
@Entity
@Table(name = "templates")
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "canvas_json", columnDefinition = "LONGTEXT")
    private String canvasJson; // Fabric.js canvas.toJSON() serialized

    @Column(name = "design_elements", columnDefinition = "JSON")
    private String designElements; // Array of template objects

    @Column(name = "template_category")
    private String category; // award, certificate, diploma, etc.

    @Column(name = "dimensions_width")
    private Integer dimensionWidth = 800;

    @Column(name = "dimensions_height")
    private Integer dimensionHeight = 600;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "exported_at")
    private LocalDateTime exportedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getters/setters
}
```

## 2. New API Endpoints

### 2.1 Get Placeholders from Template

**Endpoint:** `GET /api/templates/{templateId}/placeholders`

**Response:**
```json
{
  "status": "success",
  "data": [
    "firstName",
    "lastName",
    "date",
    "certificateId"
  ],
  "message": "Placeholders extracted"
}
```

**Implementation:**
```java
@GetMapping("/{templateId}/placeholders")
public ResponseEntity<ApiResponse<List<String>>> getTemplatePlaceholders(
    @PathVariable Long templateId
) {
    Optional<Template> template = templateRepository.findById(templateId);
    if (template.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>("error", null, "Template not found"));
    }

    List<String> placeholders = extractPlaceholders(template.get().getCanvasJson());
    return ResponseEntity.ok(new ApiResponse<>("success", placeholders, "Placeholders extracted"));
}

private List<String> extractPlaceholders(String canvasJson) {
    Set<String> placeholders = new HashSet<>();
    Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
    
    try {
        JSONObject canvas = new JSONObject(canvasJson);
        JSONArray objects = canvas.optJSONArray("objects");
        
        if (objects != null) {
            for (int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                String text = obj.optString("text", "");
                
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    placeholders.add(matcher.group(1));
                }
            }
        }
    } catch (Exception e) {
        logger.error("Error extracting placeholders", e);
    }
    
    return new ArrayList<>(placeholders);
}
```

### 2.2 Generate Certificate

**Endpoint:** `POST /api/certificates/generate`

**Request Body:**
```json
{
  "templateId": "123",
  "data": {
    "firstName": "John",
    "lastName": "Doe",
    "date": "2025-01-01",
    "certificateId": "CERT-2025-001"
  },
  "format": "pdf"
}
```

**Response:** Binary PDF/PNG file

**Implementation:**

```java
@PostMapping("/generate")
public ResponseEntity<?> generateCertificate(
    @RequestBody CertificateGenerationRequest request,
    HttpServletRequest httpRequest
) {
    try {
        // Verify template exists
        Optional<Template> template = templateRepository.findById(
            Long.parseLong(request.getTemplateId())
        );
        if (template.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>("error", null, "Template not found"));
        }

        // Apply data substitutions
        String renderedHtml = applySubstitutions(
            template.get().getCanvasJson(),
            request.getData()
        );

        // Render to PDF/PNG
        byte[] fileContent;
        String contentType;

        if ("png".equalsIgnoreCase(request.getFormat())) {
            fileContent = renderToPNG(renderedHtml);
            contentType = "image/png";
        } else {
            fileContent = renderToPDF(renderedHtml);
            contentType = "application/pdf";
        }

        // Log certificate generation for audit
        logCertificateGeneration(httpRequest, template.get(), request.getData());

        // Return file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", 
            "certificate." + request.getFormat());

        return ResponseEntity.ok()
            .headers(headers)
            .body(fileContent);

    } catch (Exception e) {
        logger.error("Error generating certificate", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<>("error", null, "Generation failed: " + e.getMessage()));
    }
}
```

### 2.3 Request/Response DTOs

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateGenerationRequest {
    private String templateId;
    private Map<String, Object> data;
    private String format; // "pdf" or "png"
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String canvasJson;
    private String designElements;
    private Integer dimensionWidth;
    private Integer dimensionHeight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 3. Certificate Rendering Service

### Option 1: Using Puppeteer (Node.js)

Create a microservice for rendering:

```javascript
// certificate-renderer.js
const puppeteer = require('puppeteer');

app.post('/render', async (req, res) => {
  try {
    const { html, format } = req.body;
    const browser = await puppeteer.launch();
    const page = await browser.newPage();

    await page.setContent(html);

    let buffer;
    if (format === 'png') {
      buffer = await page.screenshot({ type: 'png' });
    } else {
      buffer = await page.pdf({ format: 'A4' });
    }

    await browser.close();
    
    res.type(`image/${format}`).send(buffer);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});
```

### Option 2: Using Flying Saucer (Java)

Add dependency:
```xml
<dependency>
    <groupId>org.xhtmlrenderer</groupId>
    <artifactId>flying-saucer-core</artifactId>
    <version>9.1.22</version>
</dependency>
<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-core</artifactId>
    <version>1.0.10</version>
</dependency>
```

Implementation:
```java
@Service
public class CertificateRenderingService {

    public byte[] renderToPDF(String html) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        PdfDocument doc = PdfDocument.getInstance(new FileOutputStream("temp.pdf"));
        Document document = new Document(doc);
        
        // Parse and add HTML to PDF
        HtmlConverter.convertToDocument(html, document);
        document.close();
        
        return output.toByteArray();
    }

    public byte[] renderToPNG(String html) throws Exception {
        // Use PhantomJS or similar for screenshot
        // Or use Selenium WebDriver for headless browser rendering
        return captureScreenshot(html);
    }

    private byte[] captureScreenshot(String html) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BufferedImage image = renderHtmlToImage(html, 800, 600);
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
```

### Option 3: Using iText (Simpler for Canvas)

```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
```

## 4. Data Substitution Implementation

```java
private String applySubstitutions(String canvasJson, Map<String, Object> data) {
    try {
        JSONObject canvas = new JSONObject(canvasJson);
        JSONArray objects = canvas.optJSONArray("objects");

        if (objects != null) {
            for (int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);

                if ("text".equals(obj.optString("type"))) {
                    String text = obj.optString("text", "");
                    
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        String placeholder = "{{" + entry.getKey() + "}}";
                        text = text.replace(placeholder, String.valueOf(entry.getValue()));
                    }
                    
                    obj.put("text", text);
                }
            }
        }

        return canvas.toString();
    } catch (Exception e) {
        logger.error("Error applying substitutions", e);
        return canvasJson;
    }
}
```

## 5. Audit Logging

```java
private void logCertificateGeneration(
    HttpServletRequest request,
    Template template,
    Map<String, Object> data
) {
    AuditLog audit = new AuditLog();
    audit.setAction("CERTIFICATE_GENERATED");
    audit.setEntity("Certificate");
    audit.setEntityId(template.getId());
    audit.setDetails("Generated certificate from template: " + template.getName());
    audit.setIpAddress(getClientIpAddress(request));
    audit.setTimestamp(LocalDateTime.now());
    
    auditLogRepository.save(audit);
}
```

## 6. Controller Updates

### Update TemplateController

```java
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateRepository templateRepository;
    private final CertificateService certificateService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateDTO>> updateTemplate(
        @PathVariable Long id,
        @RequestBody TemplateUpdateRequest request
    ) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        // Update canvas data
        if (request.getCanvasJson() != null) {
            template.setCanvasJson(request.getCanvasJson());
        }
        
        if (request.getDesignElements() != null) {
            template.setDesignElements(request.getDesignElements());
        }
        
        if (request.getName() != null) {
            template.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        
        if (request.getCategory() != null) {
            template.setCategory(request.getCategory());
        }

        template.setUpdatedAt(LocalDateTime.now());
        Template updated = templateRepository.save(template);

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            templateToDTO(updated),
            "Template updated"
        ));
    }

    @GetMapping("/{id}/placeholders")
    public ResponseEntity<ApiResponse<List<String>>> getPlaceholders(
        @PathVariable Long id
    ) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        List<String> placeholders = certificateService.extractPlaceholders(template);
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            placeholders,
            "Placeholders extracted"
        ));
    }

    private TemplateDTO templateToDTO(Template template) {
        return new TemplateDTO(
            template.getId(),
            template.getName(),
            template.getDescription(),
            template.getCategory(),
            template.getCanvasJson(),
            template.getDesignElements(),
            template.getDimensionWidth(),
            template.getDimensionHeight(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );
    }
}
```

## 7. Dependencies to Add

```xml
<!-- JSON processing -->
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20231013</version>
</dependency>

<!-- PDF generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>

<!-- HTML to PDF (optional) -->
<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-core</artifactId>
    <version>1.0.10</version>
</dependency>
```

## 8. Configuration (application.yml)

```yaml
certificate:
  rendering:
    # Use 'browser' for Puppeteer/Selenium, 'direct' for iText
    mode: direct
    # Maximum file size for generated certificates (in MB)
    max-size: 10
    # Supported formats
    formats:
      - pdf
      - png
  
  # For browser-based rendering
  renderer-service-url: http://localhost:3000
```

## 9. Testing

### Unit Tests

```java
@SpringBootTest
public class CertificateGenerationTest {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private CertificateService certificateService;

    @Test
    public void testExtractPlaceholders() {
        Template template = new Template();
        template.setCanvasJson("{\"objects\":[{\"type\":\"text\",\"text\":\"Hello {{firstName}}\"}]}");
        
        List<String> placeholders = certificateService.extractPlaceholders(template);
        
        assertThat(placeholders).contains("firstName");
    }

    @Test
    public void testApplySubstitutions() {
        String canvasJson = "{\"objects\":[{\"type\":\"text\",\"text\":\"Hello {{firstName}}\"}]}";
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", "John");
        
        String result = certificateService.applySubstitutions(canvasJson, data);
        
        assertThat(result).contains("Hello John");
    }
}
```

## 10. Migration Script

```sql
-- Add new columns
ALTER TABLE templates ADD COLUMN IF NOT EXISTS canvas_json LONGTEXT;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS design_elements JSON;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS template_category VARCHAR(50) DEFAULT 'certificate';
ALTER TABLE templates ADD COLUMN IF NOT EXISTS dimensions_width INT DEFAULT 800;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS dimensions_height INT DEFAULT 600;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS exported_at TIMESTAMP;

-- Update existing templates
UPDATE templates SET template_category = 'certificate' WHERE template_category IS NULL;
UPDATE templates SET dimensions_width = 800 WHERE dimensions_width IS NULL;
UPDATE templates SET dimensions_height = 600 WHERE dimensions_height IS NULL;
```

## Summary

This backend integration enables:
- ✅ Storage of Fabric.js canvas JSON
- ✅ Placeholder extraction from templates
- ✅ Certificate generation with data substitution
- ✅ PDF/PNG rendering
- ✅ Audit logging for compliance
- ✅ RESTful APIs for template and certificate management

All components are designed to work seamlessly with the Angular frontend implementation.
