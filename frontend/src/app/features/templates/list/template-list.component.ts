import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';

import { TemplateService } from '../../../core/services/template.service';
import { Template } from '../../../core/models';

@Component({
  selector: 'app-template-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatDividerModule
  ],
  templateUrl: './template-list.component.html',
  styleUrls: ['./template-list.component.scss']
})
export class TemplateListComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = ['name', 'type', 'placeholders', 'status', 'createdAt', 'actions'];
  dataSource: MatTableDataSource<Template>;
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private templateService: TemplateService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.dataSource = new MatTableDataSource<Template>([]);
  }

  ngOnInit(): void {
    this.loadTemplates();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;

    this.dataSource.filterPredicate = (data: Template, filter: string) => {
      const searchStr = filter.toLowerCase();
      return [data.name, data.type, data.description ?? '']
        .some(value => (value || '').toLowerCase().includes(searchStr));
    };
  }

  loadTemplates(): void {
    this.loading = true;
    this.templateService.getTemplates().subscribe({
      next: (response: any) => {
        this.dataSource.data = response.data;
        this.loading = false;
      },
      error: (error: any) => {
        this.loading = false;
        this.snackBar.open('Failed to load templates', 'Close', { duration: 3000 });
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  onView(template: Template): void {
    this.router.navigate(['/templates/edit', template.id]);
  }

  onEdit(template: Template): void {
    this.router.navigate(['/templates/edit', template.id]);
  }

  onDelete(template: Template): void {
    if (!confirm(`Are you sure you want to delete "${template.name}"?`)) {
      return;
    }

    this.templateService.deleteTemplate(template.id).subscribe({
      next: () => {
        this.snackBar.open('Template deleted successfully', 'Close', { duration: 3000 });
        this.loadTemplates();
      },
      error: (error: any) => {
        this.snackBar.open('Failed to delete template', 'Close', { duration: 3000 });
      }
    });
  }

  onCreateNew(): void {
    this.router.navigate(['/templates/create']);
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'ACTIVE': 'primary',
      'INACTIVE': 'accent',
      'ARCHIVED': 'warn'
    };
    return colors[status] || '';
  }
}
