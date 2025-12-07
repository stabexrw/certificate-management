# CKEditor Certificate Template System - Implementation Complete

## Overview

This document describes the complete implementation of a CKEditor-based certificate template creation system that replaces the previous canvas/drag-and-drop builder.

## Architecture

### Backend Components

#### 1. **Template Entity** (`Template.java`)
- Stores certificate templates with HTML content
- Fields include: name, description, templateContent (HTML), placeholders (JSON array)
- Supports extraction and validation of placeholders

#### 2. **TemplateService** (`TemplateService.java`)
Enhanced with:
- **Placeholder Extraction**: Regex-based extraction of `{{placeholder}}` patterns
- **Template Simulation**: Replace placeholders with test data for preview
- **Placeholder Validation**: Automatic extraction and storage of placeholders

Key methods:
```java
public List<String> extractPlaceholders(String content)
public TemplateSimulationResponse simulateTemplate(Long customerId, TemplateSimulationRequest request)
public String replacePlaceholders(String content, Map<String, String> placeholderValues)
```

#### 3. **PdfGenerationService** (`PdfGenerationService.java`)
Enhanced with:
- **HTML to PDF Conversion**: Using iText HTML2PDF
- **Batch PDF Generation**: Generate multiple certificates at once
- **Style Preservation**: Maintains CKEditor formatting in PDFs

Key methods:
```java
public byte[] generatePdfFromHtmlContent(String htmlContent, Map<String, String> placeholderValues)
public List<byte[]> generateBatchPdfs(String htmlTemplate, List<Map<String, String>> batchData)
```

#### 4. **TemplateController** (`TemplateController.java`)
New endpoints:
- `POST /api/templates/simulate` - Simulate template with test data (returns HTML)
- `POST /api/templates/simulate/pdf` - Simulate template and generate PDF
- `GET /api/templates/{id}/placeholders` - Get extracted placeholders

#### 5. **DTOs**
- `TemplateSimulationRequest`: Contains templateId and placeholderValues map
- `TemplateSimulationResponse`: Returns previewHtml and extractedPlaceholders
- `CertificateBatchRequest`: For batch certificate generation

### Frontend Components

#### 1. **CKEditor Template Editor** (`ckeditor-template-editor.component.ts`)
Complete replacement for old builder components.

**Features:**
- Rich text editing with CKEditor 5
- Full toolbar: fonts, colors, alignment, images, tables, etc.
- Placeholder insertion helper (suggested placeholders)
- Real-time placeholder detection from content
- Save/Update templates
- Navigate to simulation

**Placeholder Format:** `{{placeholder_name}}`

**Suggested Placeholders:**
- student_name
- course_name
- date
- instructor_name
- completion_date
- grade
- certificate_id
- organization_name

#### 2. **Template Simulation** (`template-simulation.component.ts`)
Preview and test certificate templates.

**Features:**
- Input form for all detected placeholders
- Auto-populated default values for testing
- Live HTML preview in styled iframe
- PDF download functionality
- Backend simulation API integration

**Workflow:**
1. User enters test values for placeholders
2. Click "Generate Preview" → shows styled HTML
3. Click "Download PDF" → generates and downloads PDF

#### 3. **Updated TemplateService** (`template.service.ts`)
New methods:
```typescript
simulateTemplate(request): Observable<ApiResponse<any>>
simulateTemplatePdf(request): Observable<Blob>
getTemplatePlaceholders(templateId): Observable<ApiResponse<string[]>>
```

### Removed Components

The following old components have been completely removed:
- `/templates/builder/` - Visual template builder
- `/templates/certificate-generator/` - Old certificate generator
- `/templates/create/` - Old create component
- `/templates/edit/` - Old edit component

All functionality is now consolidated in the CKEditor-based components.

## Installation & Dependencies

### Backend
Already included in `pom.xml`:
- iText 7 (HTML2PDF)
- Jsoup (HTML sanitization)
- ZXing (QR codes)

### Frontend
New dependencies installed:
```bash
npm install --save ckeditor5 @ckeditor/ckeditor5-angular
```

Updated `angular.json` to include CKEditor styles:
```json
"styles": [
  "src/styles.scss",
  "node_modules/ckeditor5/dist/ckeditor5.css"
]
```

## API Endpoints

### Template Management
- `POST /api/templates` - Create new template
- `GET /api/templates` - List all templates
- `GET /api/templates/{id}` - Get template by ID
- `PUT /api/templates/{id}` - Update template
- `DELETE /api/templates/{id}` - Delete template

### Simulation & Preview
- `POST /api/templates/simulate` - Simulate with placeholder data (HTML)
- `POST /api/templates/simulate/pdf` - Generate preview PDF
- `GET /api/templates/{id}/placeholders` - Get extracted placeholders

## Usage Guide

### Creating a Template

1. Navigate to Templates → Create New
2. Enter template name and description
3. Use CKEditor to design the certificate:
   - Add borders using tables or paragraph styles
   - Insert images (logos, signatures)
   - Apply colors, fonts, and text effects
   - Use alignment tools for layout
4. Insert placeholders by:
   - Clicking suggested placeholder chips, OR
   - Typing manually: `{{placeholder_name}}`
5. Click "Save Template"

### Simulating a Template

1. From template editor, click "Preview & Simulate"
2. Enter test values for each placeholder
3. Click "Generate Preview" to see styled HTML
4. Click "Download PDF" to generate PDF

### Generating Certificates

Templates can be used to generate certificates via:
- Single certificate generation
- Batch certificate generation
- API calls with placeholder data

## Technical Details

### Placeholder Extraction
Backend uses regex pattern: `/\{\{(.*?)\}\}/g`

Extracts all unique placeholders from HTML content automatically when saving templates.

### HTML Sanitization
Uses Jsoup with relaxed safelist to:
- Prevent XSS attacks
- Preserve CKEditor formatting
- Allow styles, colors, borders, images

### PDF Generation
- Wraps HTML with necessary CSS for A4 landscape
- Preserves all CKEditor styles
- Embeds images as base64
- Supports custom fonts and colors

### Security
- JWT authentication required for all endpoints
- Customer-scoped templates (users only see their templates)
- HTML sanitization on save/update
- Placeholder validation

## Migration Notes

### What Changed
- **Old System**: Canvas-based drag-and-drop with Fabric.js
- **New System**: CKEditor rich text editor with HTML templates

### Benefits
- More intuitive for users (like Word/Google Docs)
- Better text formatting and styling
- Easier placeholder management
- Direct HTML to PDF conversion
- Simpler codebase and maintenance

### Breaking Changes
- Old template format (JSON with Fabric.js objects) is incompatible
- Existing templates need migration or recreation
- Different placeholder syntax (now `{{name}}` instead of object properties)

## Testing

### Backend
```bash
cd backend
mvn clean compile
mvn test
```

### Frontend
```bash
cd frontend
npm install
ng build
ng serve
```

### Manual Testing Checklist
- [ ] Create template with CKEditor
- [ ] Insert placeholders
- [ ] Save template
- [ ] Edit existing template
- [ ] Simulate template with test data
- [ ] View HTML preview
- [ ] Download PDF preview
- [ ] Generate actual certificate
- [ ] List all templates
- [ ] Delete template

## Future Enhancements

Potential improvements:
1. Template gallery with predefined designs
2. Image upload and storage
3. Custom fonts support
4. Template versioning
5. Template sharing between customers
6. Bulk certificate generation UI
7. Email delivery integration
8. Certificate analytics dashboard

## Troubleshooting

### CKEditor not loading
- Ensure `ckeditor5` packages are installed
- Check `angular.json` includes CKEditor CSS
- Clear `node_modules` and reinstall

### PDF generation issues
- Check iText dependencies in `pom.xml`
- Verify HTML is well-formed
- Check for unsupported CSS properties

### Placeholder not detected
- Ensure format is exactly `{{name}}`
- No spaces inside braces: `{{ name }}` won't work
- Placeholder names should be alphanumeric with underscores

## Support

For issues or questions:
- Check backend logs for API errors
- Use browser DevTools to inspect frontend errors
- Verify JWT token is valid
- Ensure backend is running on correct port

## Conclusion

The CKEditor-based certificate template system provides a modern, user-friendly approach to designing certificates with full support for rich formatting, placeholders, simulation, and PDF generation. All old builder components have been removed and replaced with this streamlined solution.
