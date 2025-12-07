export interface Customer {
  id: number;
  companyName: string;
  email: string;
  apiKey: string;
  status: string;
  role: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  companyName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  apiKey: string;
  customer: Customer;
}