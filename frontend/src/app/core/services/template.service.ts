import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  Template, 
  CreateTemplateRequest, 
  ApiResponse,
  PageResponse 
} from '../models';

export interface TemplateObject {
  type: 'text' | 'image' | 'rect' | 'rich-text';
  content?: string;
  originalHtml?: string; // For rich text
  templatePlaceholderKey?: string; // Mark placeholders like {{firstName}}, {{lastName}}
  [key: string]: any; // Fabric.js properties
}

export interface CertificateGenerationRequest {
  templateId: string;
  data: {
    [key: string]: string | number;
  };
  format: 'pdf' | 'png';
}

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private apiUrl = `${environment.apiUrl}/templates`;

  constructor(private http: HttpClient) {}

  createTemplate(template: CreateTemplateRequest): Observable<ApiResponse<Template>> {
    return this.http.post<ApiResponse<Template>>(this.apiUrl, template);
  }

  getTemplates(): Observable<ApiResponse<Template[]>> {
    return this.http.get<ApiResponse<Template[]>>(this.apiUrl);
  }

  getTemplatesPaginated(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Template>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponse<Template>>>(`${this.apiUrl}/paginated`, { params });
  }

  getTemplateById(id: number): Observable<ApiResponse<Template>> {
    return this.http.get<ApiResponse<Template>>(`${this.apiUrl}/${id}`);
  }

  updateTemplate(id: number, template: Partial<Template>): Observable<ApiResponse<Template>> {
    return this.http.put<ApiResponse<Template>>(`${this.apiUrl}/${id}`, template);
  }

  deleteTemplate(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  /**
   * Extract placeholders from template JSON string
   * Returns array of placeholder keys like ['firstName', 'lastName', 'date']
   */
  extractPlaceholders(templateJson: string, templateObjects?: TemplateObject[]): string[] {
    const placeholders = new Set<string>();

    if (templateObjects) {
      templateObjects.forEach(obj => {
        if (obj.templatePlaceholderKey) {
          placeholders.add(obj.templatePlaceholderKey);
        }

        // Also extract from content/originalHtml
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

    // Also try to parse templateJson if provided
    try {
      const parsed = JSON.parse(templateJson);
      if (parsed.objects && Array.isArray(parsed.objects)) {
        parsed.objects.forEach((obj: any) => {
          if (obj.text) {
            const matches = obj.text.match(/\{\{(\w+)\}\}/g);
            if (matches) {
              matches.forEach((match: string) => {
                const key = match.replace(/[\{\}]/g, '');
                placeholders.add(key);
              });
            }
          }
        });
      }
    } catch (e) {
      // Not valid JSON, skip
    }

    return Array.from(placeholders);
  }

  /**
   * Apply data substitutions to template objects (client-side preview)
   */
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
   * Generate certificate via backend (server-side rendering)
   */
  generateCertificate(request: CertificateGenerationRequest): Observable<Blob> {
    return this.http.post(
      `${environment.apiUrl}/certificates/generate`,
      request,
      { responseType: 'blob' }
    );
  }

  /**
   * Simulate template with placeholder values
   */
  simulateTemplate(request: { templateId: number; placeholderValues: { [key: string]: string } }): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/simulate`, request);
  }

  /**
   * Simulate template and generate PDF
   */
  simulateTemplatePdf(request: { templateId: number; placeholderValues: { [key: string]: string } }): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/simulate/pdf`, request, { responseType: 'blob' });
  }

  /**
   * Get extracted placeholders from a template
   */
  getTemplatePlaceholders(templateId: number): Observable<ApiResponse<string[]>> {
    return this.http.get<ApiResponse<string[]>>(`${this.apiUrl}/${templateId}/placeholders`);
  }
}
