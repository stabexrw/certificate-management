import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  Certificate, 
  GenerateCertificateRequest, 
  ApiResponse 
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {
  private apiUrl = `${environment.apiUrl}/certificates`;

  constructor(private http: HttpClient) {}

  generateCertificate(request: GenerateCertificateRequest): Observable<ApiResponse<Certificate>> {
    return this.http.post<ApiResponse<Certificate>>(`${this.apiUrl}/generate`, request);
  }

  simulateCertificate(request: GenerateCertificateRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/simulate`, request);
  }

  getCertificates(): Observable<ApiResponse<Certificate[]>> {
    return this.http.get<ApiResponse<Certificate[]>>(this.apiUrl);
  }

  getCertificateByUniqueId(uniqueId: string): Observable<ApiResponse<Certificate>> {
    return this.http.get<ApiResponse<Certificate>>(`${this.apiUrl}/${uniqueId}`);
  }

  downloadCertificate(uniqueId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${uniqueId}/download`, {
      responseType: 'blob'
    });
  }
}