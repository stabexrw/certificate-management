import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TemplateService } from '../../../core/services/template.service';

@Component({
  selector: 'app-template-simulation',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './template-simulation.component.html',
  styleUrls: ['./template-simulation.component.scss']
})
export class TemplateSimulationComponent implements OnInit {
  placeholderForm: FormGroup;
  templateContent = '';
  placeholders: string[] = [];
  templateId: number | null = null;
  previewHtml: SafeHtml = '';
  isLoading = false;
  showPreview = false;

  constructor(
    private fb: FormBuilder,
    private templateService: TemplateService,
    private router: Router,
    private snackBar: MatSnackBar,
    private sanitizer: DomSanitizer
  ) {
    this.placeholderForm = this.fb.group({});
    
    // Get data from navigation state
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state) {
      this.templateContent = navigation.extras.state['content'] || '';
      this.placeholders = navigation.extras.state['placeholders'] || [];
      this.templateId = navigation.extras.state['templateId'] || null;
    }
  }

  ngOnInit(): void {
    // Create form controls for each placeholder
    this.placeholders.forEach(placeholder => {
      this.placeholderForm.addControl(placeholder, this.fb.control(''));
    });

    // Set default values for common placeholders
    this.setDefaultValues();
  }

  setDefaultValues(): void {
    const defaults: Record<string, string> = {
      'student_name': 'John Doe',
      'course_name': 'Advanced Web Development',
      'date': new Date().toLocaleDateString(),
      'completion_date': new Date().toLocaleDateString(),
      'instructor_name': 'Jane Smith',
      'grade': 'A',
      'certificate_id': 'CERT-' + Math.random().toString(36).substr(2, 9).toUpperCase(),
      'organization_name': 'Tech Academy'
    };

    this.placeholders.forEach(placeholder => {
      if (defaults[placeholder]) {
        this.placeholderForm.get(placeholder)?.setValue(defaults[placeholder]);
      }
    });
  }

  simulatePreview(): void {
    this.isLoading = true;
    
    if (this.templateId) {
      // Use backend simulation API
      const request = {
        templateId: this.templateId,
        placeholderValues: this.placeholderForm.value
      };

      this.templateService.simulateTemplate(request).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.previewHtml = this.sanitizer.bypassSecurityTrustHtml(response.data.previewHtml);
            this.showPreview = true;
          }
          this.isLoading = false;
        },
        error: (error) => {
          this.snackBar.open('Failed to generate preview', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    } else {
      // Local simulation without backend
      let previewContent = this.templateContent;
      Object.entries(this.placeholderForm.value).forEach(([key, value]) => {
        const placeholder = `{{${key}}}`;
        previewContent = previewContent.replace(new RegExp(placeholder, 'g'), value as string);
      });
      
      this.previewHtml = this.sanitizer.bypassSecurityTrustHtml(previewContent);
      this.showPreview = true;
      this.isLoading = false;
    }
  }

  downloadPdf(): void {
    if (!this.templateId) {
      this.snackBar.open('Please save the template first before generating PDF', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    const request = {
      templateId: this.templateId,
      placeholderValues: this.placeholderForm.value
    };

    this.templateService.simulateTemplatePdf(request).subscribe({
      next: (blob) => {
        // Create download link
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'certificate-preview.pdf';
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.snackBar.open('PDF downloaded successfully', 'Close', { duration: 3000 });
        this.isLoading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to generate PDF', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/templates']);
  }
}
