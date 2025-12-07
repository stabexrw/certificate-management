import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { MatSnackBar } from '@angular/material/snack-bar';

// Angular Material imports
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';

// Services and models
import { TemplateService } from '../../../core/services/template.service';
import { CertificateService } from '../../../core/services/certificate.service';

// Models/Interfaces
interface Template {
  id: number;
  name: string;
  templateContent: string;
  placeholders: string[];
  type?: string;  // Add this
  description?: string;  // Add this
  status: string;
}

interface GenerateCertificateRequest {
  templateId: number;
  data: { [key: string]: string };
  recipientName: string;
  recipientEmail: string;
}

@Component({
  selector: 'app-certificate-generate',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatListModule
  ],
  templateUrl: './certificate-generate.component.html',
  styleUrls: ['./certificate-generate.component.scss']
})
export class CertificateGenerateComponent implements OnInit {
  generateForm: FormGroup;
  templates: Template[] = [];
  selectedTemplate: Template | null = null;
  placeholderControls: { [key: string]: FormControl } = {};
  loading = false;
  generating = false;
  previewHtml: SafeHtml = '';
  showPreview = false;
  generatedCertificate: any = null;

  constructor(
    private fb: FormBuilder,
    private templateService: TemplateService,
    private certificateService: CertificateService,
    private router: Router,
    private snackBar: MatSnackBar,
    private sanitizer: DomSanitizer
  ) {
    this.generateForm = this.fb.group({
      templateId: ['', Validators.required],
      recipientName: ['', Validators.required],
      recipientEmail: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.loading = true;
    this.templateService.getTemplates().subscribe({
      next: (response: any) => {
        this.templates = response.data.filter((t: Template) => t.status === 'ACTIVE');
        this.loading = false;
      },
      error: (error: any) => {
        this.loading = false;
        this.snackBar.open('Failed to load templates', 'Close', { duration: 3000 });
      }
    });
  }

 onTemplateChange(event: any) {
    const templateId = event.value; 
    this.selectedTemplate = this.templates.find(t => t.id === templateId) || null;
    this.placeholderControls = {};
    this.showPreview = false;
    this.generatedCertificate = null;

    if (this.selectedTemplate && this.selectedTemplate.placeholders) {
      // Create form controls for each placeholder
      this.selectedTemplate.placeholders.forEach((placeholder: string) => {
        this.placeholderControls[placeholder] = new FormControl('', Validators.required);
      });
    }
  }

  getPlaceholderKeys(): string[] {
    return Object.keys(this.placeholderControls);
  }

  getPlaceholderLabel(key: string): string {
    // Convert camelCase or snake_case to Title Case
    return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/_/g, ' ')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }

  onPreview(): void {
    if (!this.isFormValid()) {
      this.snackBar.open('Please fill all required fields', 'Close', { duration: 3000 });
      return;
    }

    const data = this.getPlaceholderData();
    if (this.selectedTemplate) {
      let previewContent = this.selectedTemplate.templateContent;
      
      // Replace placeholders with actual data
      Object.keys(data).forEach(key => {
        const placeholder = new RegExp(`{{\\s*${key}\\s*}}`, 'g');
        previewContent = previewContent.replace(placeholder, data[key]);
      });

      this.previewHtml = this.sanitizer.bypassSecurityTrustHtml(previewContent);
      this.showPreview = true;
    }
  }

  onSimulate(): void {
    if (!this.isFormValid()) {
      this.snackBar.open('Please fill all required fields', 'Close', { duration: 3000 });
      return;
    }

    this.generating = true;
    const request = this.buildRequest();

    this.certificateService.simulateCertificate(request).subscribe({
      next: (response: any) => {
        this.generating = false;
        this.snackBar.open('Certificate simulation successful!', 'Close', { duration: 3000 });
        this.onPreview();
      },
      error: (error: any) => {
        this.generating = false;
        this.snackBar.open('Simulation failed: ' + (error.error?.message || 'Unknown error'), 'Close', { duration: 5000 });
      }
    });
  }

  onGenerate(): void {
    if (!this.isFormValid()) {
      this.snackBar.open('Please fill all required fields', 'Close', { duration: 3000 });
      return;
    }

    if (!confirm('Are you sure you want to generate this certificate? This action cannot be undone.')) {
      return;
    }

    this.generating = true;
    const request = this.buildRequest();

    this.certificateService.generateCertificate(request).subscribe({
      next: (response: any) => {
        this.generating = false;
        this.generatedCertificate = response.data;
        this.snackBar.open('Certificate generated successfully!', 'Close', { duration: 3000 });
      },
      error: (error: any) => {
        this.generating = false;
        this.snackBar.open('Generation failed: ' + (error.error?.message || 'Unknown error'), 'Close', { duration: 5000 });
      }
    });
  }

  onDownload(): void {
    if (!this.generatedCertificate) {
      return;
    }

    this.loading = true;
    this.certificateService.downloadCertificate(this.generatedCertificate.uniqueId).subscribe({
      next: (blob: Blob) => {
        this.loading = false;
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `certificate_${this.generatedCertificate.uniqueId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Certificate downloaded!', 'Close', { duration: 3000 });
      },
      error: (error: any) => {
        this.loading = false;
        this.snackBar.open('Download failed', 'Close', { duration: 3000 });
      }
    });
  }

  onViewCertificate(): void {
    if (this.generatedCertificate) {
      this.router.navigate(['/certificates', this.generatedCertificate.uniqueId]);
    }
  }

  onReset(): void {
    this.generateForm.reset();
    this.placeholderControls = {};
    this.selectedTemplate = null;
    this.showPreview = false;
    this.generatedCertificate = null;
    this.previewHtml = '';
  }

  public isFormValid(): boolean {
    const formValid = this.generateForm.valid;
    const placeholdersValid = Object.values(this.placeholderControls).every(control => control.valid);
    return formValid && placeholdersValid;
  }

  public getPlaceholderData(): { [key: string]: string } {
    const data: { [key: string]: string } = {};
    Object.keys(this.placeholderControls).forEach(key => {
      data[key] = this.placeholderControls[key].value;
    });
    return data;
  }

  private buildRequest(): GenerateCertificateRequest {
    return {
      templateId: this.generateForm.get('templateId')?.value,
      data: this.getPlaceholderData(),
      recipientName: this.generateForm.get('recipientName')?.value,
      recipientEmail: this.generateForm.get('recipientEmail')?.value
    };
  }
}

