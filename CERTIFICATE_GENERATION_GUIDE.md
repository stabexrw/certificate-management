# Certificate Template & Generation System

## Overview

This document describes the certificate template design and generation system. The system allows customers to:

1. **Create Certificate Templates** - Design professional certificate layouts with a drag-and-drop editor
2. **Define Placeholders** - Mark areas where actual values will be inserted (e.g., `{{firstName}}`, `{{certificateNumber}}`)
3. **Preview & Simulate** - Test certificate generation with sample data before creating actual certificates
4. **Generate Certificates** - Create final certificates with actual values

## Architecture

### Core Components

#### 1. **Advanced Editor Component** (`advanced-editor.component.ts`)
- **Purpose**: Main template design interface with Photoshop-like UI
- **Features**:
  - Drag-and-drop element creation (5 types: text, image, shape, table, QR code)
  - Visual element selection and manipulation
  - Resize handles for 8-directional resizing
  - Layer management with z-index control
  - Grid snapping and zoom controls
  - Real-time preview

- **Key Methods**:
  - `createElementFromType()` - Factory for creating elements
  - `onCanvasDrop()` - Handles element placement
  - `onResizeHandleMouseDown()` - Starts resize operation
  - `handleResize()` - Performs 8-way resizing
  - `selectElement()` - Selects element for editing
  - `openPreview()` - Opens certificate preview dialog

#### 2. **Certificate Generation Service** (`certificate-generation.service.ts`)
- **Purpose**: Centralized service for all certificate generation operations
- **Responsibilities**:
  - Extract placeholders from templates (`extractPlaceholders()`)
  - Apply data substitutions (`applySubstitutions()`)
  - Render templates to canvas (`renderTemplateToCanvas()`)
  - Export to image formats
  - Server-side generation via HTTP

- **Key Methods**:
  - `extractPlaceholders(template)` - Finds `{{key}}` patterns
  - `applySubstitutions(elements, data)` - Replaces placeholders with values
  - `renderTemplateToCanvas()` - Client-side rendering
  - `generateCertificate()` - Server-side PDF generation
  - `simulateCertificate()` - Preview without saving
  - `downloadCanvasAsImage()` - Export local canvas

#### 3. **Certificate Preview Component** (`certificate-preview.component.ts`)
- **Purpose**: Interactive preview and simulation dialog
- **Features**:
  - Form fields for each placeholder
  - Live canvas preview
  - Export as PNG/JPG (client-side)
  - Server-side PDF generation
  - Data view in JSON format
  - Template information display

- **Tabs**:
  - **Preview**: Form inputs and live canvas
  - **Data**: JSON representation of generated certificate
  - **Template**: Template metadata and placeholders

### Data Models

#### Certificate Template
```typescript
interface CertificateTemplate {
  id?: string;
  name: string;
  description?: string;
  elements: CanvasElement[];
  style: {
    backgroundColor: string;
    border: { color: string; thickness: number; style: string };
  };
  width: number;
  height: number;
}
```

#### Canvas Element
```typescript
interface CanvasElement {
  id: string;
  type: 'text' | 'image' | 'shape' | 'table' | 'qrcode';
  content?: string;
  placeholder?: string;
  imageUrl?: string;
  position: { x: number; y: number };
  size: { width: number; height: number };
  rotation?: number;
  zIndex?: number;
  styling: {
    fontSize?: number;
    fontWeight?: string;
    color?: string;
    textAlign?: 'left' | 'center' | 'right';
    opacity?: number;
    backgroundColor?: string;
    borderColor?: string;
    borderWidth?: number;
  };
}
```

## Workflow

### 1. Template Creation

1. User navigates to Template Editor
2. Uses drag-and-drop to add elements to canvas
3. Configures element properties (text, color, size, position)
4. Defines placeholders in text elements using `{{key}}` syntax
5. Saves template

### 2. Template Definition Example

```
Certificate Template: "Graduation Certificate"

Elements:
1. Text: "Certificate of Achievement"
   - Position: (100, 50), Font: 36px, Bold
   
2. Text: "This is to certify that {{firstName}} {{lastName}}"
   - Position: (100, 150), Font: 24px
   - Placeholder: firstName, lastName
   
3. Text: "Successfully completed {{courseName}}"
   - Position: (100, 200), Font: 20px
   - Placeholder: courseName
   
4. Text: "Date: {{completionDate}}"
   - Position: (100, 400), Font: 16px
   - Placeholder: completionDate

Extracted Placeholders: [firstName, lastName, courseName, completionDate]
```

### 3. Preview & Simulation

1. User clicks "Preview" button in editor
2. Dialog opens with form inputs for each placeholder
3. User enters sample data in form fields
4. Certificate preview updates in real-time on canvas
5. User can export as PNG/JPG or generate PDF

### 4. Certificate Generation

#### Client-Side (Immediate)
- No server call needed
- Uses HTML5 Canvas API
- Limited to PNG/JPG export
- Fast, works offline

#### Server-Side (PDF)
- HTTP POST to `/certificates/generate`
- Server renders to PDF
- Supports complex layouts
- Professional output

## Placeholder System

### Placeholder Definition
- Format: `{{key}}`
- Examples: `{{firstName}}`, `{{certificateNumber}}`, `{{completionDate}}`
- Automatically extracted from all text elements

### Placeholder Replacement
```typescript
// Original: "Certificate for {{firstName}} {{lastName}}"
// Data: { firstName: "John", lastName: "Doe" }
// Result: "Certificate for John Doe"
```

## Technical Implementation

### Element Rendering Pipeline

```
1. Template Definition
   ↓
2. Extract Placeholders
   ↓
3. Apply Substitutions (replace {{key}} with values)
   ↓
4. Render to Canvas
   ├─ Background & Border
   ├─ Text Elements (with substituted content)
   ├─ Image Placeholders
   ├─ Shapes & Tables
   └─ QR Codes
   ↓
5. Export Options
   ├─ Canvas to Data URL
   ├─ Download as PNG/JPG
   └─ Send to Server for PDF
```

### Canvas Rendering

The service provides specialized rendering for each element type:

#### Text Elements
- Font properties (size, weight, family)
- Color and opacity
- Text alignment (left, center, right)
- Multiline support

#### Image Elements
- Placeholder rendering (gray box with [Image] label)
- Future: Actual image rendering with dimensions

#### Shape Elements
- Rectangle with fill color
- Border customization
- Rotation support

#### Table Elements
- Grid-based layout
- Cell borders
- Placeholder rendering

#### QR Code Elements
- Square container
- Border styling
- Placeholder rendering

## API Integration

### Backend Endpoints Required

```
POST /certificates/generate
- Input: { templateId, data, format }
- Output: PDF/PNG Blob
- Generates and returns certificate file

POST /certificates/simulate
- Input: { templateId, template, data }
- Output: { success, message, certificateId }
- Preview without saving

GET /certificates/{{id}}
- Returns certificate metadata and download URL
```

## Features

### For Template Design
- ✅ 5 Element Types (Text, Image, Shape, Table, QR Code)
- ✅ Drag-and-drop placement
- ✅ 8-directional resizing
- ✅ Grid snapping (10px)
- ✅ Zoom controls (50-200%)
- ✅ Layer management
- ✅ Z-index control
- ✅ Property panel

### For Certificate Simulation
- ✅ Automatic placeholder extraction
- ✅ Form-based data entry
- ✅ Real-time preview
- ✅ Canvas rendering
- ✅ PNG/JPG export (client-side)
- ✅ PDF generation (server-side)
- ✅ JSON data view
- ✅ Template metadata display

### Planned Features
- [ ] Batch certificate generation
- [ ] Template versioning
- [ ] Digital signatures
- [ ] QR code generation with data
- [ ] Barcode support
- [ ] Font upload
- [ ] Image library management
- [ ] Certificate verification

## Usage Examples

### Example 1: Diploma Certificate

```
Template: "University Diploma"
Elements:
- Title: "DIPLOMA"
- Body: "This diploma certifies that {{studentName}} has successfully..."
- Institution: "{{universityName}}"
- Date: "{{graduationDate}}"
- Signature placeholder: [Image element]

Simulation Data:
{
  studentName: "Jane Smith",
  universityName: "State University",
  graduationDate: "May 15, 2024"
}
```

### Example 2: Achievement Certificate

```
Template: "Course Completion"
Elements:
- Header: "Certificate of Completion"
- Text: "{{participantName}} has successfully completed {{courseName}}"
- Text: "Course Duration: {{hours}} hours"
- Text: "Completed on {{completionDate}}"

Simulation Data:
{
  participantName: "John Doe",
  courseName: "Advanced Angular Development",
  hours: "40",
  completionDate: "December 5, 2024"
}
```

## Error Handling

- **Missing Placeholders**: Component shows "No placeholders found" message
- **Invalid Template**: Graceful degradation with error messages
- **Network Errors**: Retry mechanism with user feedback
- **Canvas Rendering**: Fallback to placeholder rendering if image fails

## Performance Considerations

- Canvas rendering is optimized for templates up to 2000x2000px
- Large templates cached locally
- Placeholder extraction is O(n) where n = number of elements
- Canvas refresh debounced during rapid changes

## Security

- Placeholders are case-sensitive to prevent injection attacks
- No HTML/script content allowed in substitutions
- All values escaped before canvas rendering
- Server-side validation for PDF generation

## Browser Compatibility

- Canvas API: All modern browsers
- HTML5 Drag-Drop: IE11+
- FormGroup: All modern frameworks
- Export functionality: All modern browsers with download API

## Troubleshooting

### Preview not updating
- Check placeholder names match exactly (case-sensitive)
- Ensure placeholders are in `{{key}}` format
- Verify form values are entered

### Canvas rendering issues
- Check template dimensions are reasonable
- Verify element positions are within canvas bounds
- Try zooming out to see full canvas

### Export failures
- Ensure CORS is configured for external images
- Check browser console for specific errors
- Try PDF generation via server instead

## File Structure

```
templates/
├── builder/
│   ├── advanced-editor.component.ts
│   ├── advanced-editor.component.html
│   ├── advanced-editor.component.scss
│   ├── certificate-preview.component.ts
│   ├── certificate-preview.component.html
│   └── certificate-preview.component.scss
├── services/
│   └── certificate-generation.service.ts
├── create/
│   ├── template-create.component.ts
│   └── template-create.component.html
├── edit/
│   ├── template-edit.component.ts
│   └── template-edit.component.html
├── list/
│   ├── template-list.component.ts
│   └── template-list.component.html
└── template.routes.ts
```

## Conclusion

This certificate template and generation system provides a complete end-to-end solution for creating, previewing, and generating professional certificates. The modular architecture allows for easy extension and customization, while the client-side rendering provides fast, responsive previews and exports.
