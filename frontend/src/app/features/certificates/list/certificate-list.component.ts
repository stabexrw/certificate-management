import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

// Material Imports - ADD ALL OF THESE
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { CertificateService } from '../../../core/services/certificate.service';
import { Certificate } from '../../../core/models';

@Component({
  selector: 'app-certificate-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule, // For [(ngModel)]
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatMenuModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './certificate-list.component.html',
  styleUrls: ['./certificate-list.component.scss']
})
export class CertificateListComponent implements OnInit {
  displayedColumns: string[] = [
    'uniqueId',
    'recipientName',
    'recipientEmail',
    'templateName',
    'status',
    'createdAt',
    'downloadCount',
    'actions'
  ];
  
  dataSource: MatTableDataSource<Certificate>;
  loading = false;
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  statusFilter = 'all';
  searchText = '';

  constructor(
    private certificateService: CertificateService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
    this.dataSource = new MatTableDataSource<Certificate>([]);
  }

  ngOnInit(): void {
    this.loadCertificates();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    
    this.dataSource.filterPredicate = (data: Certificate, filter: string) => {
      const searchStr = filter.toLowerCase();
      return data.uniqueId.toLowerCase().includes(searchStr) ||
             data.recipientName.toLowerCase().includes(searchStr) ||
             data.recipientEmail.toLowerCase().includes(searchStr) ||
             data.templateName.toLowerCase().includes(searchStr);
    };
  }

  loadCertificates(): void {
    this.loading = true;
    this.certificateService.getCertificates().subscribe({
      next: (response: any) => {
        this.dataSource.data = response.data;
        this.loading = false;
      },
      error: (error: any) => {
        this.loading = false;
        this.snackBar.open('Failed to load certificates', 'Close', { duration: 3000 });
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.searchText = filterValue;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  applyStatusFilter(): void {
    this.loadCertificates();
  }

  onView(certificate: Certificate): void {
    this.router.navigate(['/certificates', certificate.uniqueId]);
  }

  onDownload(certificate: Certificate): void {
    this.certificateService.downloadCertificate(certificate.uniqueId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `certificate_${certificate.uniqueId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Certificate downloaded!', 'Close', { duration: 3000 });
        this.loadCertificates();
      },
      error: (error: any) => {
        this.snackBar.open('Download failed', 'Close', { duration: 3000 });
      }
    });
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'GENERATED': 'accent',
      'DOWNLOADED': 'primary',
      'VERIFIED': 'warn',
      'REVOKED': 'warn'
    };
    return colors[status] || 'primary';
  }

  onGenerateNew(): void {
    this.router.navigate(['/certificates/generate']);
  }

  exportToCSV(): void {
    const data = this.dataSource.filteredData;
    const csv = this.convertToCSV(data);
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `certificates_${new Date().toISOString()}.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  private convertToCSV(data: Certificate[]): string {
    const headers = ['ID', 'Recipient Name', 'Email', 'Template', 'Status', 'Created', 'Downloads'];
    const rows = data.map(cert => [
      cert.uniqueId,
      cert.recipientName,
      cert.recipientEmail,
      cert.templateName,
      cert.status,
      new Date(cert.createdAt).toLocaleString(),
      cert.downloadCount.toString()
    ]);
    
    return [headers, ...rows]
      .map(row => row.map(cell => `"${cell}"`).join(','))
      .join('\n');
  }
}