import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CanvasElement {
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

export interface CertificateTemplate {
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

export interface CertificateSimulationRequest {
  templateId: number;
  template: CertificateTemplate;
  data: { [key: string]: string | number };
}

export interface CertificateGenerationResponse {
  success: boolean;
  message: string;
  certificateId?: string;
  downloadUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CertificateGenerationService {
  private apiUrl = `${environment.apiUrl}/certificates`;

  constructor(private http: HttpClient) {}

  /**
   * Extract all placeholders from template elements
   * Looks for {{placeholder}} pattern in text content
   */
  extractPlaceholders(template: CertificateTemplate): string[] {
    const placeholders = new Set<string>();

    template.elements.forEach(element => {
      if (element.type === 'text' && element.content) {
        const matches = element.content.match(/\{\{(\w+)\}\}/g);
        if (matches) {
          matches.forEach(match => {
            const key = match.replace(/[\{\}]/g, '');
            placeholders.add(key);
          });
        }
      }
      
      // Check placeholder field
      if (element.placeholder) {
        placeholders.add(element.placeholder);
      }
    });

    return Array.from(placeholders);
  }

  /**
   * Apply data substitutions to create preview elements
   * Returns a new array of elements with placeholders replaced
   */
  applySubstitutions(
    elements: CanvasElement[],
    substitutionData: { [key: string]: string | number }
  ): CanvasElement[] {
    return elements.map(element => {
      const newElement = { ...element, styling: { ...element.styling } };

      // Replace text content placeholders
      if (newElement.type === 'text' && newElement.content) {
        newElement.content = this.substituteText(newElement.content, substitutionData);
      }

      return newElement;
    });
  }

  /**
   * Substitute {{key}} placeholders with values
   */
  private substituteText(text: string, data: { [key: string]: string | number }): string {
    let result = text;
    Object.entries(data).forEach(([key, value]) => {
      const regex = new RegExp(`\\{\\{${key}\\}\\}`, 'g');
      result = result.replace(regex, String(value));
    });
    return result;
  }

  /**
   * Generate certificate on server (backend rendering to PDF/PNG)
   */
  generateCertificate(
    templateId: number,
    data: { [key: string]: string | number },
    format: 'pdf' | 'png' = 'pdf'
  ): Observable<Blob> {
    const payload = {
      templateId,
      data,
      format
    };

    return this.http.post(
      `${this.apiUrl}/generate`,
      payload,
      { responseType: 'blob' }
    );
  }

  /**
   * Simulate certificate generation (get preview without saving)
   */
  simulateCertificate(
    templateId: number,
    template: CertificateTemplate,
    data: { [key: string]: string | number }
  ): Observable<CertificateGenerationResponse> {
    const payload = {
      templateId,
      template,
      data
    };

    return this.http.post<CertificateGenerationResponse>(
      `${this.apiUrl}/simulate`,
      payload
    );
  }

  /**
   * Canvas-to-image conversion (client-side)
   * Renders template elements to canvas with substituted data
   */
  renderTemplateToCanvas(
    canvas: HTMLCanvasElement,
    template: CertificateTemplate,
    substitutedElements: CanvasElement[]
  ): void {
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Set canvas dimensions
    canvas.width = template.width;
    canvas.height = template.height;

    // Draw background
    ctx.fillStyle = template.style.backgroundColor;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Draw border if specified
    if (template.style.border && template.style.border.thickness > 0) {
      ctx.strokeStyle = template.style.border.color;
      ctx.lineWidth = template.style.border.thickness;
      ctx.strokeRect(
        template.style.border.thickness / 2,
        template.style.border.thickness / 2,
        canvas.width - template.style.border.thickness,
        canvas.height - template.style.border.thickness
      );
    }

    // Sort elements by z-index
    const sortedElements = [...substitutedElements].sort(
      (a, b) => (a.zIndex || 0) - (b.zIndex || 0)
    );

    // Draw each element
    sortedElements.forEach(element => {
      this.renderElement(ctx, element);
    });
  }

  /**
   * Render individual element on canvas
   */
  private renderElement(ctx: CanvasRenderingContext2D, element: CanvasElement): void {
    ctx.save();

    // Apply transformation
    ctx.globalAlpha = element.styling.opacity ?? 1;
    if (element.rotation) {
      const centerX = element.position.x + element.size.width / 2;
      const centerY = element.position.y + element.size.height / 2;
      ctx.translate(centerX, centerY);
      ctx.rotate((element.rotation * Math.PI) / 180);
      ctx.translate(-centerX, -centerY);
    }

    switch (element.type) {
      case 'text':
        this.renderTextElement(ctx, element);
        break;
      case 'image':
        this.renderImageElement(ctx, element);
        break;
      case 'shape':
        this.renderShapeElement(ctx, element);
        break;
      case 'table':
        this.renderTableElement(ctx, element);
        break;
      case 'qrcode':
        this.renderQRCodeElement(ctx, element);
        break;
    }

    ctx.restore();
  }

  /**
   * Render text element
   */
  private renderTextElement(ctx: CanvasRenderingContext2D, element: CanvasElement): void {
    if (!element.content) return;

    // Set text properties
    ctx.fillStyle = element.styling.color || '#000000';
    ctx.font = `${element.styling.fontWeight || 'normal'} ${element.styling.fontSize || 16}px Arial`;
    ctx.textAlign = (element.styling.textAlign as any) || 'left';
    ctx.textBaseline = 'top';

    // Draw background if specified
    if (element.styling.backgroundColor) {
      ctx.fillStyle = element.styling.backgroundColor;
      ctx.fillRect(
        element.position.x,
        element.position.y,
        element.size.width,
        element.size.height
      );
    }

    // Draw border if specified
    if (element.styling.borderWidth && element.styling.borderWidth > 0) {
      ctx.strokeStyle = element.styling.borderColor || '#000000';
      ctx.lineWidth = element.styling.borderWidth;
      ctx.strokeRect(
        element.position.x,
        element.position.y,
        element.size.width,
        element.size.height
      );
    }

    // Draw text
    ctx.fillStyle = element.styling.color || '#000000';
    ctx.font = `${element.styling.fontWeight || 'normal'} ${element.styling.fontSize || 16}px Arial`;

    // Handle multiline text
    const lines = element.content.split('\n');
    const lineHeight = (element.styling.fontSize || 16) * 1.2;
    let yOffset = element.position.y + 10;

    lines.forEach(line => {
      const x = element.position.x + (element.size.width / 2);
      ctx.fillText(line, x, yOffset);
      yOffset += lineHeight;
    });
  }

  /**
   * Render image element (placeholder for now)
   */
  private renderImageElement(ctx: CanvasRenderingContext2D, element: CanvasElement): void {
    // Draw placeholder for image
    ctx.fillStyle = element.styling.backgroundColor || '#e0e0e0';
    ctx.fillRect(
      element.position.x,
      element.position.y,
      element.size.width,
      element.size.height
    );

    // Draw border
    ctx.strokeStyle = element.styling.borderColor || '#999999';
    ctx.lineWidth = element.styling.borderWidth || 1;
    ctx.strokeRect(
      element.position.x,
      element.position.y,
      element.size.width,
      element.size.height
    );

    // Draw text
    ctx.fillStyle = '#666666';
    ctx.textAlign = 'center';
    ctx.fillText(
      '[Image]',
      element.position.x + element.size.width / 2,
      element.position.y + element.size.height / 2
    );
  }

  /**
   * Render shape element
   */
  private renderShapeElement(ctx: CanvasRenderingContext2D, element: CanvasElement): void {
    ctx.fillStyle = element.styling.backgroundColor || '#cccccc';
    ctx.fillRect(
      element.position.x,
      element.position.y,
      element.size.width,
      element.size.height
    );

    if (element.styling.borderWidth && element.styling.borderWidth > 0) {
      ctx.strokeStyle = element.styling.borderColor || '#000000';
      ctx.lineWidth = element.styling.borderWidth;
      ctx.strokeRect(
        element.position.x,
        element.position.y,
        element.size.width,
        element.size.height
      );
    }
  }

  /**
   * Render table element (simplified)
   */
  private renderTableElement(ctx: CanvasRenderingContext2D, element: CanvasElement): void {
    ctx.fillStyle = element.styling.backgroundColor || '#ffffff';
    ctx.fillRect(
      element.position.x,
      element.position.y,
      element.size.width,
      element.size.height
    );

    ctx.strokeStyle = element.styling.borderColor || '#000000';
    ctx.lineWidth = element.styling.borderWidth || 1;
    ctx.strokeRect(
      element.position.x,
      element.position.y,
      element.size.width,
      element.size.height
    );

    ctx.fillStyle = '#666666';
    ctx.textAlign = 'center';
    ctx.fillText(
      '[Table]',
      element.position.x + element.size.width / 2,
      element.position.y + element.size.height / 2
    );
  }

  /**
   * Render QR code element (placeholder for now)
   */
  private renderQRCodeElement(ctx: CanvasRenderingContext2D, element: CanvasElement): void {
    ctx.fillStyle = element.styling.backgroundColor || '#ffffff';
    ctx.fillRect(
      element.position.x,
      element.position.y,
      element.size.width,
      element.size.height
    );

    ctx.strokeStyle = element.styling.borderColor || '#000000';
    ctx.lineWidth = 2;
    ctx.strokeRect(
      element.position.x,
      element.position.y,
      element.size.width,
      element.size.height
    );

    ctx.fillStyle = '#666666';
    ctx.textAlign = 'center';
    ctx.fillText(
      '[QR Code]',
      element.position.x + element.size.width / 2,
      element.position.y + element.size.height / 2
    );
  }

  /**
   * Export canvas to data URL (PNG)
   */
  canvasToDataUrl(canvas: HTMLCanvasElement, format: 'png' | 'jpg' = 'png'): string {
    const type = format === 'png' ? 'image/png' : 'image/jpeg';
    return canvas.toDataURL(type, 1.0);
  }

  /**
   * Download canvas as image file
   */
  downloadCanvasAsImage(canvas: HTMLCanvasElement, filename: string, format: 'png' | 'jpg' = 'png'): void {
    const link = document.createElement('a');
    link.href = this.canvasToDataUrl(canvas, format);
    link.download = `${filename}.${format}`;
    link.click();
  }
}
