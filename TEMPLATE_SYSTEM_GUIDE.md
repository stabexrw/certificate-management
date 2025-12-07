# Certificate Template System - Complete Implementation Guide

## Overview

This implementation provides a complete practical approach for certificate template creation, preview, and generation using:

- **Fabric.js** - Canvas-based layout editor for images, shapes, and text
- **html2canvas** - Convert rich HTML to images for canvas rendering
- **jsPDF** - PDF export functionality
- **Angular Material** - UI components and dialogs
- **Placeholders** - Dynamic {{key}} substitution for certificate data

## Architecture

### 1. Template Creation Workflow

```
Gallery Templates
    ↓
Visual Template Builder (Fabric.js Canvas)
    ├─ Add text objects
    ├─ Add rich text (via modal → html2canvas → canvas image)
    ├─ Add placeholders (marked with metadata)
    └─ Arrange and style objects
    ↓
Template Saved (Canvas JSON + Metadata)
```

### 2. Certificate Generation Workflow

```
Saved Template (Canvas JSON)
    ↓
Load into Offscreen Canvas
    ├─ Apply data substitutions ({{key}} → value)
    ├─ Re-render rich text images if needed
    └─ Export to visible preview
    ↓
User Input Form (Extract placeholders)
    ↓
Export Options:
    ├─ Client-side: PDF (jsPDF) or PNG (canvas.toDataURL)
    └─ Server-side: Backend rendering for consistency
```

## Components

### 1. Visual Template Builder (`visual-template-builder.component.ts`)

**Key Methods:**

```typescript
// Load gallery template into Fabric canvas
loadTemplateIntoFabric(htmlContent: string)

// Open rich text editor modal
openRichTextEditor(existingHtml?: string)

// Convert rich HTML to canvas image via html2canvas
private addRichTextToCanvas(htmlContent: string)

// Add placeholder text with metadata
addPlaceholderToCanvas(placeholderKey: string)

// Export template for generation
exportTemplateForGeneration()

// Save template with canvas JSON to backend
saveTemplateWithCanvasData()

// Get template JSON for storage
getTemplateJSON()
```

**Canvas Listeners:**
```typescript
this.fabricCanvas.on('object:added', () => this.exportFabricToPreview());
this.fabricCanvas.on('object:modified', () => this.exportFabricToPreview());
this.fabricCanvas.on('object:removed', () => this.exportFabricToPreview());
```

**Real-time Preview:**
- Exports Fabric canvas to HTML preview
- Updates automatically on any canvas change

### 2. Rich Text Editor Modal (`rich-text-editor-modal.component.ts`)

**Features:**
- HTML/Preview tabs
- Textarea for HTML input
- Live preview rendering
- Save/Cancel actions

**Usage:**
```typescript
this.openRichTextEditor();
```

### 3. Certificate Generator (`certificate-generator.component.ts`)

**Key Methods:**

```typescript
// Extract all placeholders from template
extractPlaceholders(templateJson, templateObjects)

// Update preview canvas with data substitutions
updatePreview()

// Export to PDF (client-side)
exportToPDF()

// Export to PNG (client-side)
exportToPNG()

// Generate on server
requestServerGeneration()
```

**Placeholder Substitution:**
- Regex-based replacement: `{{key}}` → value
- Applied to both text content and rich text HTML
- Real-time preview updates

### 4. Template Service (`template.service.ts`)

**Key Methods:**

```typescript
// Extract placeholders from template
extractPlaceholders(templateJson: string, objects?: TemplateObject[])

// Apply data substitutions
applySubstitutions(objects: TemplateObject[], data: object)

// Generate certificate (server-side)
generateCertificate(request: CertificateGenerationRequest)
```

## Object Metadata

### Template Object Structure

```typescript
interface TemplateObject {
  type: 'text' | 'image' | 'rect' | 'rich-text';
  content?: string;           // For simple text
  originalHtml?: string;      // For rich text (stores original HTML)
  templatePlaceholderKey?: string; // If object is a placeholder {{key}}
  isRichText?: boolean;       // Flag for rich text objects
  // ... all Fabric.js properties
}
```

### Placeholder Marking

Text objects are marked as placeholders with custom properties:
```typescript
const fabricText = new fabric.Text('{{firstName}}', {
  left: 50,
  top: 50,
  fontSize: 18,
  fill: '#1976d2',
  selectable: true
});

(fabricText as any).templatePlaceholderKey = 'firstName';
(fabricText as any)._isPlaceholder = true;
```

## Data Flow

### Saving a Template

1. User designs in Visual Builder (Fabric canvas)
2. Click "Save Template"
3. Component calls:
   ```typescript
   const canvasJson = this.fabricCanvas.toJSON();
   const templateData = {
     name, description, category, canvasJson
   };
   this.templateService.updateTemplate(id, templateData);
   ```
4. Backend stores canvas JSON + metadata

### Generating a Certificate

1. User navigates to generator with template data
2. Extracts placeholders:
   ```typescript
   const placeholders = this.templateService.extractPlaceholders(
     canvasJson, 
     templateObjects
   );
   ```
3. Creates form with inputs for each placeholder
4. User fills in certificate data
5. On export:
   ```typescript
   // Apply substitutions
   const objects = this.templateService.applySubstitutions(
     templateObjects,
     substitutionData
   );
   
   // Load into offscreen canvas, render, export
   ```

## Rich Text Handling

### Flow

1. **User edits HTML:**
   - Opens modal → enters HTML/preview
   - Saves HTML content

2. **Convert to canvas image:**
   ```typescript
   html2canvas(container, {
     backgroundColor: 'white',
     scale: 2,
     logging: false
   }).then((canvas: HTMLCanvasElement) => {
     const imageUrl = canvas.toDataURL('image/png');
     fabric.Image.fromURL(imageUrl, (img) => {
       img.set({
         originalHtml: htmlContent, // Store original
         isRichText: true
       });
       this.fabricCanvas.add(img);
     });
   });
   ```

3. **Store original HTML:**
   - Image object has `originalHtml` property
   - On generation, can re-render with substituted values

4. **Generate with substitutions:**
   ```typescript
   // Re-render rich text with data
   const substitutedHtml = this.substituteText(
     originalHtml,
     { firstName: 'John', ... }
   );
   // Convert new HTML to image
   // Replace image on canvas
   ```

## Export Options

### Client-side

**PDF Export (jsPDF):**
```typescript
exportToPDF() {
  const canvas = this.canvasRef.nativeElement;
  const imgData = canvas.toDataURL('image/png');
  const pdf = new jsPDF({
    orientation: canvas.width > canvas.height ? 'landscape' : 'portrait',
    unit: 'px',
    format: [canvas.width, canvas.height]
  });
  pdf.addImage(imgData, 'PNG', 0, 0, canvas.width, canvas.height);
  pdf.save('certificate.pdf');
}
```

**PNG Export:**
```typescript
exportToPNG() {
  const canvas = this.canvasRef.nativeElement;
  const link = document.createElement('a');
  link.href = canvas.toDataURL('image/png');
  link.download = 'certificate.png';
  link.click();
}
```

### Server-side

**Request Format:**
```typescript
const request = {
  templateId: 'template-123',
  data: {
    firstName: 'John',
    lastName: 'Doe',
    date: '2025-01-01'
  },
  format: 'pdf'
};
this.templateService.generateCertificate(request);
```

**Backend Endpoint:** `POST /api/certificates/generate`
- Load template canvas JSON
- Render with data using server-side rendering library
- Return PDF/PNG blob

## Installation

### Dependencies Added

```json
{
  "dependencies": {
    "fabric": "^5.5.2",
    "html2canvas": "^1.4.1",
    "jspdf": "^2.5.1"
  },
  "devDependencies": {
    "@types/html2canvas": "^1.0.11",
    "@types/jspdf": "^2.5.10"
  }
}
```

### Install

```bash
cd frontend
npm install
```

## Routes

Add to `app.routes.ts`:

```typescript
{
  path: 'templates',
  children: [
    {
      path: 'builder',
      component: VisualTemplateBuilderComponent
    },
    {
      path: 'generate',
      component: CertificateGeneratorComponent
    }
  ]
}
```

## Usage Example

### 1. Create Template

```typescript
// User navigates to /templates/builder
// Designs certificate in Visual Builder
// Adds text, rich text, placeholders
// Saves template
```

### 2. Generate Certificate

```typescript
// User navigates to /templates/generate?templateId=123
// System extracts placeholders: ['firstName', 'lastName', 'date']
// Form appears with input fields
// User fills: firstName="John", lastName="Doe", date="2025-01-01"
// User clicks "Export to PDF"
// Certificate generated with substituted values
```

## Key Features

✅ **Drag-and-drop Layout** - Fabric.js canvas editor  
✅ **Rich Text Support** - HTML → image conversion  
✅ **Placeholder System** - {{key}} dynamic substitution  
✅ **Real-time Preview** - Live updates on canvas change  
✅ **Multiple Export Formats** - PDF, PNG, server-generated  
✅ **Template Reusability** - Save and load templates  
✅ **Metadata Storage** - Canvas JSON + custom properties  
✅ **Offscreen Rendering** - For preview generation  

## Next Steps

1. **Backend Implementation:**
   - Create `/api/certificates/generate` endpoint
   - Use Fabric.js or headless browser for server-side rendering
   - Return PDF/PNG blob

2. **Database Schema:**
   - Update `Template` entity to store `canvasJson`
   - Optionally store template preview image

3. **Error Handling:**
   - Add try-catch for html2canvas failures
   - Handle invalid template JSON
   - Validate placeholder values

4. **Testing:**
   - Unit tests for placeholder extraction
   - Integration tests for template generation
   - E2E tests for full workflow

## Files Modified/Created

- ✅ `package.json` - Added dependencies
- ✅ `visual-template-builder.component.ts` - Enhanced with rich text & metadata
- ✅ `rich-text-editor-modal.component.ts` - New HTML editor component
- ✅ `certificate-generator.component.ts` - New generator component
- ✅ `template.service.ts` - Extended with substitution methods

## Troubleshooting

**html2canvas not found:**
- Ensure npm install completed successfully
- Check browser console for module errors

**Placeholder not appearing:**
- Verify placeholder key format: `{{key}}`
- Check that templatePlaceholderKey property is set

**Rich text image not rendering:**
- Ensure HTML is valid
- Check that html2canvas callback executes
- Verify Fabric.js Image.fromURL succeeds

**PDF export issues:**
- Check canvas dimensions (might be too large)
- Use jsPDF native unit conversion if needed
- Test with smaller canvas size first
