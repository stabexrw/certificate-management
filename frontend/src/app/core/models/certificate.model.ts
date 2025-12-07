export interface Certificate {
  id: number;
  uniqueId: string;
  customerId: number;
  templateId: number;
  templateName: string;
  certificateData?: any;
  recipientName: string;
  recipientEmail: string;
  filePath: string;
  downloadUrl: string;
  digitalSignature: string;
  qrCodeData: string;
  status: string;
  createdAt: Date;
  downloadedAt?: Date;
  downloadCount: number;
}

export interface GenerateCertificateRequest {
  templateId: number;
  data: { [key: string]: string };
  recipientName?: string;
  recipientEmail?: string;
}
