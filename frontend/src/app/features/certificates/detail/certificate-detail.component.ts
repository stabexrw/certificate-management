import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, JsonPipe, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

// Material imports - ALL OF THEM
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

import { CertificateService } from '../../../core/services/certificate.service';
import { Certificate } from '../../../core/models/certificate.model';



@Component({
  selector: 'app-certificate-detail',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    JsonPipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './certificate-detail.component.html',
  styleUrls: ['./certificate-detail.component.scss']
})
export class CertificateDetailComponent implements OnInit {
  certificate: Certificate | null = null;
  loading = false;
  downloading = false;
  uniqueId: string = ''; 

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private certificateService: CertificateService,
    private snackBar: MatSnackBar,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.uniqueId = this.route.snapshot.paramMap.get('uniqueId') || '';
    if (this.uniqueId) {
      this.loadCertificate();
    }
  }

  loadCertificate(): void {
    this.loading = true;
    this.certificateService.getCertificateByUniqueId(this.uniqueId).subscribe({
      next: (response) => {
        this.certificate = response.data;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open('Failed to load certificate', 'Close', { duration: 3000 });
        this.goBack();
      }
    });
  }

  onDownload(): void {
    if (!this.certificate) return;

    this.downloading = true;
    this.certificateService.downloadCertificate(this.certificate.uniqueId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `certificate_${this.certificate!.uniqueId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.downloading = false;
        this.snackBar.open('Certificate downloaded!', 'Close', { duration: 3000 });
        
        // Reload to update download count
        this.loadCertificate();
      },
      error: (error) => {
        this.downloading = false;
        this.snackBar.open('Download failed', 'Close', { duration: 3000 });
      }
    });
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.snackBar.open('Copied to clipboard!', 'Close', { duration: 2000 });
    });
  }

  shareViaEmail(): void {
    if (!this.certificate) return;
    
    const subject = encodeURIComponent(`Certificate: ${this.certificate.recipientName}`);
    const body = encodeURIComponent(
      `Certificate ID: ${this.certificate.uniqueId}\n` +
      `Recipient: ${this.certificate.recipientName}\n` +
      `Verification URL: ${this.certificate.qrCodeData}`
    );
    
    window.location.href = `mailto:${this.certificate.recipientEmail}?subject=${subject}&body=${body}`;
  }

  goBack(): void {
    this.location.back();
  }

  getStatusIcon(status: string): string {
    const icons: { [key: string]: string } = {
      'GENERATED': 'check_circle',
      'DOWNLOADED': 'download_done',
      'VERIFIED': 'verified',
      'REVOKED': 'cancel'
    };
    return icons[status] || 'info';
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'GENERATED': 'accent',
      'DOWNLOADED': 'primary',
      'VERIFIED': 'primary',
      'REVOKED': 'warn'
    };
    return colors[status] || '';
  }
}