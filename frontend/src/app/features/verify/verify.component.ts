import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [CommonModule, RouterModule, HttpClientModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.scss']
})
export class VerifyComponent implements OnInit {
  uniqueId: string | null = null;
  customerId: string | null = null;
  signature: string | null = null;
  loading = true;
  valid: boolean | null = null;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.uniqueId = this.route.snapshot.paramMap.get('uniqueId');
    this.customerId = this.route.snapshot.queryParamMap.get('customer');
    this.signature = this.route.snapshot.queryParamMap.get('signature');

    if (!this.uniqueId || !this.signature) {
      this.error = 'QR code is missing required data; cannot verify.';
      this.loading = false;
      return;
    }

    const url = `${environment.apiUrl}/public/verify/${this.uniqueId}?signature=${encodeURIComponent(this.signature)}`;
    this.http.get<any>(url).subscribe({
      next: (res) => {
        this.valid = !!res.data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Verification failed';
        this.loading = false;
      }
    });
  }

  goToLogin(): void {
    // redirect to login and preserve return url
    this.router.navigate(['/auth/login'], { queryParams: { returnUrl: this.router.url } });
  }

  downloadIfAllowed(): void {
    if (!this.auth.isAuthenticated()) {
      this.goToLogin();
      return;
    }

    // If authenticated, navigate to certificate detail page (adjust route as needed)
    this.router.navigate(['/certificates', 'detail', this.uniqueId]);
  }
}
