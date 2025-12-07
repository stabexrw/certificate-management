export interface Template {
  id: number;
  customerId: number;
  name: string;
  description?: string;
  templateContent?: string;
  content?: string;
  type: 'HTML' | 'JSON' | 'PDF_TEMPLATE';
  placeholders: string[];
  status: string;
  category?: string;
  createdAt?: Date;
  updatedAt?: Date;
}

export type TemplateModel = Partial<Template> & { id?: number };

export interface CreateTemplateRequest {
  name: string;
  description: string;
  templateContent?: string;
  content?: string;
  type: string;
  placeholders?: string[];
  category?: string;
}
