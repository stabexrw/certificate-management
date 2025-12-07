export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: Date;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ErrorResponse {
  timestamp: Date;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}
