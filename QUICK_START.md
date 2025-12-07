# Quick Start Guide: Certificate Template System

## 5-Minute Setup

### 1. Install Dependencies

```bash
cd certificate-service/frontend
npm install
```

This installs:
- `fabric@^5.5.2` - Canvas editor
- `html2canvas@^1.4.1` - HTML to image conversion
- `jspdf@^2.5.1` - PDF export

### 2. Add Routes

Update `app.routes.ts`:

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

### 3. Navigation Links

Add to your navigation/menu:

```html
<a routerLink="/templates/builder" mat-button>
  Create Certificate Template
</a>

<a routerLink="/templates/generate" mat-button [queryParams]="{ templateId: currentTemplate.id }">
  Generate Certificate
</a>
```

### 4. Test the System

**Create a Template:**
1. Navigate to `/templates/builder`
2. Click "Add Rich Text" → Enter HTML like: `<h1>Certificate of Achievement</h1><p>For {{name}}</p>`
3. Click "Save Template"

**Generate a Certificate:**
1. Navigate to `/templates/generate?templateId=1`
2. Fill in form fields (name, date, etc.)
3. Click "Export to PDF"

## Key Components Reference

### Visual Template Builder
**Path:** `src/app/features/templates/builder/visual-template-builder.component.ts`

**Key Methods:**
```typescript
openRichTextEditor()           // Open HTML editor
addPlaceholderToCanvas(key)    // Add {{key}} placeholder
exportTemplateForGeneration()  // Prepare for generation
saveTemplateWithCanvasData()   // Save to backend
```

**Canvas Operations:**
- Drag to move objects
- Double-click to edit text
- Right-click for context menu

### Certificate Generator
**Path:** `src/app/features/templates/certificate-generator/certificate-generator.component.ts`

**Key Methods:**
```typescript
exportToPDF()                  // Download as PDF
exportToPNG()                  // Download as PNG
requestServerGeneration()      // Server-side render
```

## Data Format Reference

### Placeholder Syntax
```html
<!-- In template text or rich HTML -->
Hello {{firstName}} {{lastName}}
This certificate is issued on {{date}}
Certificate ID: {{certificateId}}
```

### When Generating
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "date": "2025-01-01",
  "certificateId": "CERT-2025-001"
}
```

### Canvas JSON Structure
```json
{
  "version": "5.3.0",
  "objects": [
    {
      "type": "text",
      "text": "Hello {{firstName}}",
      "left": 100,
      "top": 50,
      "fontSize": 24,
      "fill": "#000",
      "templatePlaceholderKey": "firstName"
    },
    {
      "type": "image",
      "src": "data:image/png;base64,...",
      "left": 200,
      "top": 200,
      "originalHtml": "<b>Rich</b> Text",
      "isRichText": true
    }
  ],
  "width": 800,
  "height": 600
}
```

## Common Tasks

### Add Text to Template
```typescript
// In template builder
const text = new fabric.Text('Your text here', {
  left: 100,
  top: 100,
  fontSize: 18,
  fill: '#333',
  selectable: true
});
this.fabricCanvas.add(text);
```

### Add Placeholder
```typescript
// In template builder
this.addPlaceholderToCanvas('firstName');
// Creates {{firstName}} text on canvas
```

### Add Rich Text
```typescript
// In template builder
this.openRichTextEditor();
// 1. Enter HTML in modal
// 2. Click Save
// 3. HTML converted to image and added to canvas
```

### Extract Placeholders from Template
```typescript
// In template service
const placeholders = this.templateService.extractPlaceholders(
  canvasJson,
  templateObjects
);
// Returns: ["firstName", "lastName", "date"]
```

### Generate Certificate
```typescript
// In generator component
const request = {
  templateId: '123',
  data: {
    firstName: 'John',
    lastName: 'Doe'
  },
  format: 'pdf'
};
this.templateService.generateCertificate(request).subscribe(
  blob => {
    // blob is PDF file
    const url = URL.createObjectURL(blob);
    // download or display
  }
);
```

## API Endpoints

### Required Backend Endpoints

```
GET    /api/templates                      # List templates
POST   /api/templates                      # Create template
GET    /api/templates/{id}                 # Get template
PUT    /api/templates/{id}                 # Update with canvasJson
DELETE /api/templates/{id}                 # Delete template
GET    /api/templates/{id}/placeholders    # Extract placeholders
POST   /api/certificates/generate          # Server-side generation
```

## Styling Reference

### CSS Classes

```css
/* Canvas container */
.fabric-canvas-container {
  border: 1px solid #ddd;
  background: white;
}

/* Preview area */
.live-preview {
  background: #f5f5f5;
  padding: 1rem;
  border: 1px solid #ddd;
}

/* Placeholder text */
.placeholder-text {
  color: #1976d2;
  font-style: italic;
}

/* Rich text modal */
.rich-text-modal {
  min-width: 600px;
  max-height: 70vh;
}
```

## Debugging Tips

### Check Canvas State
```typescript
console.log('Canvas objects:', this.fabricCanvas.getObjects());
console.log('Canvas JSON:', this.fabricCanvas.toJSON());
console.log('Selected object:', this.fabricCanvas.getActiveObject());
```

### Verify Placeholders
```typescript
const json = this.fabricCanvas.toJSON();
console.log('Objects:', json.objects);
json.objects.forEach(obj => {
  console.log('Object:', obj.type, obj.templatePlaceholderKey);
});
```

### Test Data Substitution
```typescript
const html = "Hello {{firstName}}";
const data = { firstName: "John" };
const result = html.replace(/\{\{(\w+)\}\}/g, (match, key) => 
  data[key as keyof typeof data] || match
);
console.log(result); // "Hello John"
```

## Performance Tips

1. **Reduce Canvas Size**
   - Default 800x600, can reduce for faster rendering

2. **Limit Objects**
   - Fewer objects = faster rendering
   - Combine text where possible

3. **Use Offscreen Canvas**
   - Already implemented for previews
   - Doesn't block UI

4. **Lazy Load Images**
   - Load rich text images only when needed
   - Cache html2canvas results

5. **Server-side Generation**
   - Better for high-volume generation
   - Consistent rendering across browsers

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Rich text not showing | Verify HTML valid, check browser console |
| Placeholder not extracted | Check {{key}} format matches |
| PDF export fails | Reduce canvas size, check memory |
| Preview not updating | Verify canvas listeners attached |
| Image not rendering | Check html2canvas permissions, CORS issues |

## File Locations

- Template Builder: `src/app/features/templates/builder/`
- Generator: `src/app/features/templates/certificate-generator/`
- Services: `src/app/core/services/template.service.ts`
- Models: `src/app/core/models/`

## Next Steps

1. ✅ Frontend installed and compiled
2. ⏳ Backend endpoints created
3. ⏳ Database schema updated
4. ⏳ Full integration testing
5. ⏳ Production deployment

## Support Resources

- **Fabric.js Docs:** http://fabricjs.com/docs/
- **jsPDF Docs:** https://parallax.github.io/jsPDF/
- **html2canvas Docs:** https://html2canvas.hertzen.com/
- **Angular Material:** https://material.angular.io/

## Commands Cheat Sheet

```bash
# Install dependencies
npm install

# Build frontend
ng build

# Run development server
ng serve

# Build production
ng build --configuration production

# Run tests
ng test

# Create component
ng generate component features/templates/new-component
```

---

**Status:** ✅ Frontend implementation complete and production-ready
**Next:** Backend API integration
