# Quick Reference: Certificate Template System Implementation

## 1. Rich Text Editing Workflow

### Open Rich Text Editor
```typescript
// In visual-template-builder.component.ts
openRichTextEditor(existingHtml?: string) {
  const dialogData: RichTextEditorData = {
    initialHtml: existingHtml || '',
    title: 'Add Rich Text Block'
  };

  this.dialog.open(RichTextEditorModalComponent, {
    data: dialogData,
    width: '800px'
  }).afterClosed().subscribe((result: string | null) => {
    if (result) {
      this.addRichTextToCanvas(result);
    }
  });
}
```

### Convert HTML to Canvas Image
```typescript
private addRichTextToCanvas(htmlContent: string) {
  const container = document.createElement('div');
  container.innerHTML = htmlContent;
  container.style.position = 'absolute';
  container.style.left = '-9999px';
  document.body.appendChild(container);

  html2canvas(container, {
    backgroundColor: 'white',
    scale: 2
  }).then((canvas: HTMLCanvasElement) => {
    const imageUrl = canvas.toDataURL('image/png');

    fabric.Image.fromURL(imageUrl, (img) => {
      img.set({
        left: 50,
        top: 50,
        originalHtml: htmlContent,
        isRichText: true
      } as any);
      this.fabricCanvas!.add(img);
    });

    document.body.removeChild(container);
  });
}
```

## 2. Placeholder Management

### Add Placeholder to Canvas
```typescript
addPlaceholderToCanvas(placeholderKey: string) {
  if (!this.fabricCanvas) return;

  const placeholder = `{{${placeholderKey}}}`;
  const fabricText = new fabric.Text(placeholder, {
    left: 50,
    top: 50,
    fontSize: 18,
    fill: '#1976d2',
    selectable: true
  } as any);

  (fabricText as any).templatePlaceholderKey = placeholderKey;
  (fabricText as any)._isPlaceholder = true;

  this.fabricCanvas.add(fabricText);
  this.fabricCanvas.renderAll();
}
```

### Extract Placeholders from Template
```typescript
// In template.service.ts
extractPlaceholders(templateJson: string, templateObjects?: TemplateObject[]): string[] {
  const placeholders = new Set<string>();

  if (templateObjects) {
    templateObjects.forEach(obj => {
      if (obj.templatePlaceholderKey) {
        placeholders.add(obj.templatePlaceholderKey);
      }

      const content = obj.content || obj.originalHtml || '';
      const matches = content.match(/\{\{(\w+)\}\}/g);
      if (matches) {
        matches.forEach(match => {
          const key = match.replace(/[\{\}]/g, '');
          placeholders.add(key);
        });
      }
    });
  }

  return Array.from(placeholders);
}
```

## 3. Template Saving

### Save Template with Canvas JSON
```typescript
saveTemplateWithCanvasData() {
  const canvasJson = this.fabricCanvas!.toJSON();
  const canvasJsonStr = JSON.stringify(canvasJson);

  const templateData = {
    name: this.templateName,
    description: this.templateDescription,
    category: this.templateCategory,
    canvasJson: canvasJsonStr,
    designElements: canvasJson.objects
  };

  this.templateService.updateTemplate(this.initialTemplate?.id || 0, templateData)
    .subscribe(
      (response) => {
        this.toastr.success('Template saved');
        this.templateSaved.emit(templateData);
      },
      (error: any) => {
        this.toastr.error('Failed to save');
      }
    );
}
```

## 4. Certificate Generation

### Extract and Display Placeholder Form
```typescript
// In certificate-generator.component.ts
ngOnInit() {
  this.route.queryParams.subscribe(params => {
    this.templateJson = params['json'];
    this.templateObjects = params['objects'] ? JSON.parse(params['objects']) : [];

    // Extract placeholders for form
    this.placeholders = this.templateService.extractPlaceholders(
      this.templateJson,
      this.templateObjects
    );

    // Create form controls
    this.placeholders.forEach(ph => {
      this.dataForm.addControl(ph, this.fb.control(''));
    });

    this.updatePreview();
  });
}
```

### Update Preview with Data
```typescript
private updatePreview() {
  try {
    const offscreenCanvas = new fabric.Canvas(document.createElement('canvas'), {
      width: 800,
      height: 600
    } as any);

    const canvasData = JSON.parse(this.templateJson);

    offscreenCanvas.loadFromJSON(canvasData, () => {
      const substitutionData = this.dataForm.getRawValue();

      // Apply substitutions
      offscreenCanvas.forEachObject((obj: any) => {
        if (obj.text) {
          let newText = obj.text;
          Object.entries(substitutionData).forEach(([key, value]) => {
            const regex = new RegExp(`\\{\\{${key}\\}\\}`, 'g');
            newText = newText.replace(regex, String(value));
          });
          obj.set({ text: newText });
        }
      });

      offscreenCanvas.renderAll();

      // Export to visible canvas
      const dataUrl = (offscreenCanvas as any).toDataURL({ format: 'png' });
      const previewImg = new Image();
      previewImg.onload = () => {
        const canvas = this.canvasRef?.nativeElement;
        if (canvas) {
          canvas.width = previewImg.width;
          canvas.height = previewImg.height;
          const ctx = canvas.getContext('2d');
          if (ctx) ctx.drawImage(previewImg, 0, 0);
        }
      };
      previewImg.src = dataUrl;
    });
  } catch (e) {
    console.error('Preview error:', e);
  }
}
```

## 5. Export Options

### Export to PDF (Client-side)
```typescript
exportToPDF() {
  try {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) throw new Error('Canvas not available');

    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF({
      orientation: canvas.width > canvas.height ? 'landscape' : 'portrait',
      unit: 'px',
      format: [canvas.width, canvas.height]
    });

    pdf.addImage(imgData, 'PNG', 0, 0, canvas.width, canvas.height);
    pdf.save('certificate.pdf');

    this.toastr.success('PDF exported');
  } catch (e) {
    this.toastr.error('Export failed');
  }
}
```

### Export to PNG (Client-side)
```typescript
exportToPNG() {
  try {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) throw new Error('Canvas not available');

    const link = document.createElement('a');
    link.href = canvas.toDataURL('image/png');
    link.download = 'certificate.png';
    link.click();

    this.toastr.success('PNG exported');
  } catch (e) {
    this.toastr.error('Export failed');
  }
}
```

### Generate on Server (Server-side)
```typescript
requestServerGeneration() {
  const request: CertificateGenerationRequest = {
    templateId: this.templateId,
    data: this.dataForm.getRawValue(),
    format: 'pdf'
  };

  this.templateService.generateCertificate(request).subscribe(
    (blob: Blob) => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'certificate.pdf';
      link.click();
      window.URL.revokeObjectURL(url);

      this.toastr.success('Generated on server');
    },
    (error: any) => {
      this.toastr.error('Server generation failed');
    }
  );
}
```

## 6. Template Export for Generation

### Prepare Template Data
```typescript
exportTemplateForGeneration() {
  const canvasJson = JSON.stringify(this.fabricCanvas!.toJSON());

  const templateObjects = this.fabricCanvas!.getObjects().map((obj: any) => ({
    type: obj.type,
    content: obj.text || undefined,
    originalHtml: obj.originalHtml,
    templatePlaceholderKey: obj.templatePlaceholderKey,
    isRichText: obj.isRichText || false,
    ...obj.toJSON()
  }));

  const params = new URLSearchParams({
    templateId: this.initialTemplate?.id?.toString() || 'new',
    json: canvasJson,
    objects: JSON.stringify(templateObjects)
  });

  window.open(`/templates/generate?${params.toString()}`, '_blank');
}
```

## 7. Data Substitution Logic

### Template Text Substitution
```typescript
private substituteText(text: string, data: { [key: string]: string | number }): string {
  let result = text;
  Object.entries(data).forEach(([key, value]) => {
    const regex = new RegExp(`\\{\\{${key}\\}\\}`, 'g');
    result = result.replace(regex, String(value));
  });
  return result;
}
```

### Apply to Template Objects
```typescript
applySubstitutions(
  objects: TemplateObject[],
  substitutionData: { [key: string]: string | number }
): TemplateObject[] {
  return objects.map(obj => {
    const newObj = { ...obj };

    if (newObj.content) {
      newObj.content = this.substituteText(newObj.content, substitutionData);
    }

    if (newObj.originalHtml) {
      newObj.originalHtml = this.substituteText(newObj.originalHtml, substitutionData);
    }

    return newObj;
  });
}
```

## 8. Real-time Preview Updates

### Canvas Event Listeners
```typescript
ngAfterViewInit() {
  this.fabricCanvas = new fabric.Canvas('fabric-canvas', {
    width: 800,
    height: 400,
    backgroundColor: '#fff'
  });

  // Listen to changes
  this.fabricCanvas.on('object:added', () => this.exportFabricToPreview());
  this.fabricCanvas.on('object:modified', () => this.exportFabricToPreview());
  this.fabricCanvas.on('object:removed', () => this.exportFabricToPreview());
}
```

### Export Canvas to HTML
```typescript
private exportFabricToPreview() {
  if (!this.fabricCanvas) return;

  let html = '<div style="position: relative; width: 100%; height: 100%;">';
  
  this.fabricCanvas.forEachObject((obj: any) => {
    if (obj.type === 'text') {
      const style = `
        position: absolute;
        left: ${obj.left}px;
        top: ${obj.top}px;
        font-size: ${obj.fontSize}px;
        color: ${obj.fill};
      `;
      html += `<div style="${style}">${obj.text}</div>`;
    }
  });
  
  html += '</div>';
  this.fabricExportHtml = html;
}
```

## 9. Module Imports

### Visual Builder Component
```typescript
imports: [
  CommonModule,
  FormsModule,
  MatTabsModule,
  MatIconModule,
  MatButtonModule,
  MatDividerModule,
  MatChipsModule,
  MatTooltipModule,
  MatDialogModule,
  MatMenuModule,
  TemplateGalleryComponent,
  PlaceholderManagerComponent,
  LivePreviewComponent,
  RichTextEditorModalComponent
]
```

### Generator Component
```typescript
imports: [
  CommonModule,
  FormsModule,
  ReactiveFormsModule,
  MatButtonModule,
  MatFormFieldModule,
  MatInputModule,
  MatProgressSpinnerModule,
  MatCardModule,
  MatIconModule
]
```

## 10. API Contract

### Certificate Generation Request
```typescript
interface CertificateGenerationRequest {
  templateId: string;
  data: {
    [key: string]: string | number;
  };
  format: 'pdf' | 'png';
}
```

### Template Object Interface
```typescript
interface TemplateObject {
  type: 'text' | 'image' | 'rect' | 'rich-text';
  content?: string;
  originalHtml?: string;
  templatePlaceholderKey?: string;
  isRichText?: boolean;
  [key: string]: any; // Fabric.js properties
}
```

This implementation provides a production-ready certificate template system with:
- ✅ Rich text editing
- ✅ Placeholder substitution  
- ✅ Real-time preview
- ✅ Multiple export formats
- ✅ Reusable templates
- ✅ Server-side generation support
