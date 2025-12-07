import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { CustomerService } from '../../core/services/customer.service';
import { TemplateService } from '../../core/services/template.service';
import { CertificateService } from '../../core/services/certificate.service';
import { Customer, Template, Certificate } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatTableModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  customer: Customer | null = null;
  templates: Template[] = [];
  certificates: Certificate[] = [];
  loading = true;
  displayedColumns = ['uniqueId', 'recipientName', 'templateName', 'createdAt', 'actions'];

  stats = {
    totalTemplates: 0,
    totalCertificates: 0,
    recentCertificates: 0
  };

  constructor(
    private customerService: CustomerService,
    private templateService: TemplateService,
    private certificateService: CertificateService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    this.customerService.getCurrentCustomer().subscribe({
      next: (response: any) => {
        this.customer = response.data;
      }
    });

    this.templateService.getTemplates().subscribe({
      next: (response: any) => {
        this.templates = response.data;
        this.stats.totalTemplates = this.templates.length;
      }
    });

    this.certificateService.getCertificates().subscribe({
      next: (response: any) => {
        this.certificates = response.data;
        this.stats.totalCertificates = this.certificates.length;
        this.stats.recentCertificates = this.certificates.filter(cert => {
          const createdDate = new Date(cert.createdAt);
          const weekAgo = new Date();
          weekAgo.setDate(weekAgo.getDate() - 7);
          return createdDate > weekAgo;
        }).length;
        this.loading = false;
      }
    });
  }
}