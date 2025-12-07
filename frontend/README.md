Certificate Management System
A comprehensive solution for managing certificate templates and generating PDF certificates with security features.
🏗️ Architecture Overview
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   Angular UI    │ ──────► │   Spring Boot    │ ──────► │   PostgreSQL    │
│   (Frontend)    │  HTTP   │     Backend      │  JDBC   │    Database     │
└─────────────────┘         └──────────────────┘         └─────────────────┘
        │                            │
        │                            ▼
        │                    ┌──────────────────┐
        │                    │   PDF Generator  │
        │                    │    (iText)       │
        │                    └──────────────────┘
        │
        ▼
┌─────────────────┐
│  Static Files   │
│  (Certificates) │
└─────────────────┘
🎯 Key Features
1. Multi-Tenant Security

Row-Level Security: Every entity has customer_id foreign key
Query Interceptors: All queries filter by authenticated customer
API Key + JWT: Dual authentication for UI and programmatic access

2. High Performance (1000+ certs/min)

Async Processing: Certificate generation in thread pool
Database Indexing: Optimized queries with indexes
Caching: Redis for frequently accessed data (optional)
Connection Pooling: HikariCP with optimized settings

3. Anti-Fraud Protection

Digital Signatures: SHA-256 hash of certificate data
QR Codes: Verification URLs embedded in certificates
Unique IDs: UUID for each certificate
Watermarks: Embedded in PDF for tamper detection

📋 Prerequisites

Java: JDK 17 or higher
Node.js: v18 or higher
PostgreSQL: 15 or higher
Maven: 3.8 or higher
Angular CLI: 17 or higher
Docker (optional)

🚀 Quick Start
Option 1: Docker Compose (Recommended)
bash# Clone the repository
git clone <repository-url>
cd certificate-management-system

# Start all services
docker-compose up -d

# Access the application
Frontend: http://localhost:4200
Backend: http://localhost:8080
API Docs: http://localhost:8080/swagger-ui.html
Option 2: Manual Setup
Backend Setup
bash# Navigate to backend
cd backend

# Start PostgreSQL
docker-compose up -d postgres

# Run the application
./mvnw spring-boot:run

# Or build and run
./mvnw clean package
java -jar target/certificate-service-1.0.0.jar
Frontend Setup
bash# Navigate to frontend
cd certificate-frontend

# Install dependencies
npm install

# Start development server
ng serve

# Build for production
ng build --configuration production
🔧 Configuration
Backend (application.yml)
yamlspring:
  datasource:
    url: jdbc:postgresql://localhost:5432/certificate_db
    username: postgres
    password: postgres
    
app:
  jwt:
    secret: YourSuperSecretKeyThatIsAtLeast256BitsLong
    expiration: 86400000 # 24 hours
  certificate:
    storage-path: ./certificates
Frontend (environment.ts)
typescriptexport const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
📚 API Documentation
Authentication Endpoints
Login
httpPOST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "apiKey": "abc123...",
    "customer": { ... }
  }
}
Register
httpPOST /api/auth/register
Content-Type: application/json

{
  "companyName": "My Company",
  "email": "user@example.com",
  "password": "password123"
}
Template Endpoints
Create Template
httpPOST /api/templates
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Certificate Template",
  "description": "Sample template",
  "templateContent": "<html>...</html>",
  "type": "HTML",
  "placeholders": ["name", "date"]
}
List Templates
httpGET /api/templates
Authorization: Bearer <token>
Certificate Endpoints
Generate Certificate (UI)
httpPOST /api/certificates/generate
Authorization: Bearer <token>
Content-Type: application/json

{
  "templateId": 1,
  "data": {
    "name": "John Doe",
    "date": "2024-01-01"
  },
  "recipientName": "John Doe",
  "recipientEmail": "john@example.com"
}
Generate Certificate (API)
httpPOST /api/certificates/generate
X-API-Key: <api-key>
Content-Type: application/json

{
  "templateId": 1,
  "data": {
    "name": "John Doe"
  }
}
Download Certificate
httpGET /api/certificates/{uniqueId}/download
Authorization: Bearer <token>

Response: PDF file (binary)
Verify Certificate
httpGET /api/public/verify/{uniqueId}?signature={digitalSignature}

Response:
{
  "success": true,
  "data": true
}
🧪 Testing
Backend Tests
bash# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

# View coverage report
open target/site/jacoco/index.html
Frontend Tests
bash# Run unit tests
ng test

# Run e2e tests
ng e2e

# Generate coverage
ng test --code-coverage

# View coverage
open coverage/index.html
🔒 Security Features
1. Template Isolation
java// Every template query includes customer_id
@Query("SELECT t FROM Template t WHERE t.id = :id AND t.customer.id = :customerId")
Optional<Template> findByIdAndCustomerId(Long id, Long customerId);
2. Certificate Access Control
java// JWT Authentication for UI
@GetMapping("/{uniqueId}")
public ResponseEntity<Certificate> getCertificate(
    @AuthenticationPrincipal CustomUserDetails user,
    @PathVariable String uniqueId) {
    // Validates user owns the certificate
}

// API Key Authentication for programmatic access
@GetMapping("/{uniqueId}/download")
@ApiKeyRequired
public ResponseEntity<byte[]> download(@PathVariable String uniqueId) {
    // Validates API key ownership
}
3. Digital Signature Verification
javapublic String generateDigitalSignature(String uniqueId, Map<String, String> data) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    String dataToSign = uniqueId + data.toString();
    byte[] hash = digest.digest(dataToSign.getBytes());
    return Base64.getEncoder().encodeToString(hash);
}
📊 Performance Optimization
1. Async Certificate Generation
java@Async("certificateGenerationExecutor")
public CompletableFuture<Certificate> generateCertificateAsync(
    GenerateCertificateRequest request) {
    // Generate certificate in background thread
}
2. Database Indexes
sqlCREATE INDEX idx_template_customer ON templates(customer_id);
CREATE INDEX idx_cert_unique_id ON certificates(unique_id);
CREATE INDEX idx_cert_created ON certificates(created_at);
3. Connection Pool Configuration
yamlspring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
🎨 UI Screenshots
Dashboard

Overview of templates and certificates
Quick action buttons
Recent activity

Template Management

List all templates
Create new templates with HTML editor
Preview templates
Edit/Delete templates

Certificate Generation

Select template
Fill placeholder values
Preview certificate
Generate and download PDF

📦 Deployment
Docker Deployment
bash# Build images
docker build -t cert-backend ./backend
docker build -t cert-frontend ./frontend

# Run with docker-compose
docker-compose -f docker-compose.prod.yml up -d
Production Checklist

 Change JWT secret in production
 Configure HTTPS
 Set up database backups
 Configure log aggregation
 Set up monitoring (Prometheus/Grafana)
 Configure rate limiting
 Set up CDN for frontend
 Configure email notifications

🐛 Troubleshooting
Backend won't start
bash# Check PostgreSQL is running
docker ps | grep postgres

# Check logs
./mvnw spring-boot:run -X

# Verify database connection
psql -h localhost -U postgres -d certificate_db
Frontend build errors
bash# Clear node_modules
rm -rf node_modules package-lock.json
npm install

# Clear Angular cache
ng cache clean
📝 License
This project is licensed under the MIT License.
👥 Contributors

SHEMA TUYIZERE PATRICK - Initial work

🙏 Acknowledgments

Spring Boot
Angular
iText PDF
PostgreSQL
Material Design