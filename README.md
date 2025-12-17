# Certificate Management System

A full-stack web application for managing and generating digital certificates with a rich  editor, authentication, and database persistence.

## 🚀 Live Demo

- **Frontend:** https://spectacular-cucurucho-793843.netlify.app/
- **Backend API:** https://certificate-backend-orsd.onrender.com/api
- **Database:** Supabase PostgreSQL

## 📋 Features

### Frontend (Angular 21 + CKEditor 5)
- ✅ User registration & JWT authentication
- ✅ Rich certificate template editor with **CKEditor 5** (73 plugins)
- ✅ Full-featured editor with:
  - Source code editing
  - HTML embed support
  - Predefined styles system
  - Image insertion & resizing
  - Table support
  - Font/color customization
  - And much more...
- ✅ Template management (CRUD)
- ✅ Certificate generation from templates
- ✅ Customer management
- ✅ Dashboard with analytics
- ✅ Responsive Material Design UI

### Backend (Spring Boot 3.2)
- ✅ REST API with JWT authentication
- ✅ Customer, Template, Certificate, and Audit management
- ✅ PDF certificate generation with iText
- ✅ Digital signature support
- ✅ Database migrations with Hibernate
- ✅ CORS configured for frontend
- ✅ Comprehensive error handling

### Database (PostgreSQL 15)
- ✅ User & authentication tables
- ✅ Certificate templates with HTML content
- ✅ Generated certificates storage
- ✅ Customer information management
- ✅ Audit logs for compliance
- ✅ Supabase free tier hosting

## 🛠️ Tech Stack

### Frontend
- Angular 21 (standalone components)
- TypeScript
- Angular Material UI
- CKEditor 5 (GPL - Open Source)
- RxJS for reactive programming
- Responsive SCSS styling

### Backend
- Spring Boot 3.2
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL 15
- iText 7 (PDF generation)
- Lombok (boilerplate reduction)
- Maven for build management

### Deployment
- **Frontend:** Netlify (free tier)
- **Backend:** Render (free tier with Docker)
- **Database:** Supabase (free tier PostgreSQL)

## 📦 Installation & Setup

### Prerequisites
- Node.js 18+ (for frontend)
- Java 17+ (for backend)
- Maven 3.9+ (for backend build)
- PostgreSQL 15+ or Supabase account
- Git

### Local Development

#### 1. Clone Repository
```bash
git clone https://github.com/stabexrw/certificate-management.git
cd certificate-management
```

#### 2. Frontend Setup
```bash
cd frontend
npm install
ng serve
# Frontend runs at http://localhost:4200
```

#### 3. Backend Setup
```bash
cd backend
# Using Docker Compose (easiest)
docker-compose up -d

# Or manually with existing Postgres
mvn clean package -DskipTests
java -jar target/certificate-service-1.0.0.jar
# Backend runs at http://localhost:8080/api
```

#### 4. Environment Configuration

**Frontend** (`frontend/src/environments/environment.ts`):
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  apiTimeout: 30000
};
```

**Backend** (`backend/src/main/resources/application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/certificate_db
    username: postgres
    password: postgres
```

## 🔐 Authentication

### Registration
```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "fullName": "John Doe"
}
```

### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

Returns JWT token in response - include in Authorization header for authenticated requests:
```bash
Authorization: Bearer <jwt_token>
```

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Templates
- `GET /api/templates` - List all templates
- `POST /api/templates` - Create new template
- `GET /api/templates/{id}` - Get template details
- `PUT /api/templates/{id}` - Update template
- `DELETE /api/templates/{id}` - Delete template

### Certificates
- `GET /api/certificates` - List certificates
- `POST /api/certificates/generate` - Generate certificate from template
- `GET /api/certificates/{id}` - Get certificate details
- `GET /api/certificates/{id}/download` - Download certificate PDF

### Customers
- `GET /api/customers` - List customers
- `POST /api/customers` - Add customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

## 🎨 CKEditor Features

The template editor includes:
- **Text Formatting:** Bold, italic, underline, strikethrough
- **Lists:** Bullet, numbered, todo lists
- **Styling:** Predefined styles, font size (9-96pt), 15+ fonts
- **Colors:** Font color & background color (20 shades each)
- **Media:** Image insertion, resizing, captions
- **Tables:** Full table support with cell properties
- **HTML:** Source editing, HTML embed, code blocks
- **Advanced:** Find/replace, special characters, horizontal lines, page breaks
- **Accessibility:** Accessibility checker, balloon toolbar

## 📋 Usage Example

### Creating a Certificate Template

1. **Login** at https://spectacular-cucurucho-793843.netlify.app/login
2. Navigate to **Templates** → **Create New**
3. Use the CKEditor to design your template:
   - Add header: `Certificate of Completion`
   - Add body text with placeholders: `{student_name} has completed {course_name}`
   - Format with fonts, colors, and styling
   - Insert company logo as image
4. **Save Template**

### Generating Certificates

1. Go to **Certificates** → **Generate**
2. Select template
3. Choose customer
4. Review preview
5. **Generate PDF**
6. Download certificate

## 🚀 Deployment

### Deploy Frontend to Netlify
```bash
# Frontend already deployed at:
# https://spectacular-cucurucho-793843.netlify.app/

# To redeploy after changes:
git push origin main
# Netlify auto-redeploys on push
```

### Deploy Backend to Render
```bash
# Backend already deployed at:
# https://certificate-backend-orsd.onrender.com/api

# To redeploy after changes:
git push origin main
# Render auto-rebuilds Docker image and redeploys
```

### Database on Supabase
- Already provisioned and connected
- PostgreSQL 15 with 500MB free storage
- Connection pooling via Session pooler

## 🔧 Configuration

### CORS Configuration
Backend (`SecurityConfig.java`) allows:
- `http://localhost:*` (development)
- `https://spectacular-cucurucho-793843.netlify.app` (production)

### Database Connection (Production)
Uses Supabase Session pooler:
- Host: `aws-1-eu-central-1.pooler.supabase.com`
- Port: `6543`
- Database: `postgres`

### PDF Generation
Uses iText 7 to generate certificates with:
- HTML to PDF conversion
- Custom fonts and styling
- Image support
- Digital signatures (optional)

## 📊 Project Structure

```
certificate-management/
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── features/        # Feature modules
│   │   │   │   ├── auth/
│   │   │   │   ├── templates/   # CKEditor integration
│   │   │   │   ├── certificates/
│   │   │   │   ├── customers/
│   │   │   │   └── dashboard/
│   │   │   ├── core/            # Services, guards, interceptors
│   │   │   │   ├── services/
│   │   │   │   ├── guards/
│   │   │   │   └── interceptors/
│   │   │   └── shared/          # Shared components
│   │   └── environments/        # Environment configs
│   ├── angular.json
│   ├── package.json
│   └── netlify.toml            # Netlify deployment config
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/seccertificate/certificateservice/
│   │   │   │       ├── config/         # Spring configs
│   │   │   │       ├── controller/     # REST endpoints
│   │   │   │       ├── service/        # Business logic
│   │   │   │       ├── repository/     # Database access
│   │   │   │       ├── entity/         # JPA entities
│   │   │   │       ├── dto/            # Data transfer objects
│   │   │   │       ├── security/       # JWT & auth
│   │   │   │       └── exception/      # Error handling
│   │   │   └── resources/
│   │   │       └── application.yml     # Spring config
│   │   └── test/                       # Unit tests
│   ├── pom.xml
│   ├── Dockerfile                      # Docker image
│   ├── docker-compose.yml              # Local Postgres
│   └── certificates/                   # Generated PDFs
│
├── render.yaml                         # Render deployment config
├── netlify.toml                        # Netlify deployment config
└── README.md                           # This file
```

## 🧪 Testing

### Frontend Tests
```bash
cd frontend
npm test                    # Run unit tests
ng e2e                      # Run e2e tests
ng serve                    # Development server
```

### Backend Tests
```bash
cd backend
mvn test                    # Run unit tests
mvn integration-test        # Run integration tests
mvn clean package           # Build production JAR
```

## 🐛 Troubleshooting

### CORS Errors
- Ensure backend `SecurityConfig.java` includes your frontend URL
- Verify `CORS_ALLOWED_ORIGINS` env var in Render

### Database Connection Failed
- Check Supabase credentials in Render environment variables
- Verify Session pooler URL uses port `6543` (not 5432)
- Ensure SSL mode is set to `require`

### Frontend Build Fails
- Verify Node.js version is 18+
- Delete `node_modules` and run `npm ci`
- Clear Angular cache: `ng cache clean`

### Backend Build Fails on Render
- Check Docker build logs in Render dashboard
- Verify all Java dependencies are in `pom.xml`
- Ensure `backend/Dockerfile` path is correct

## 👥 Contributing

Contributions welcome! Please follow these steps:

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📞 Support

For issues and questions:
- GitHub Issues: https://github.com/stabexrw/certificate-management/issues
- Email: pshema29@gmail.com

## 🎯 Roadmap

- [ ] Email notifications for certificate generation
- [ ] Batch certificate generation
- [ ] Certificate preview before generation
- [ ] Template versioning & history
- [ ] Advanced analytics dashboard
- [ ] Mobile app (React Native)
- [ ] Multi-language support
- [ ] Digital certificate blockchain integration

## 📚 Documentation

- [CKEditor Implementation Guide](./CKEDITOR_IMPLEMENTATION_GUIDE.md)
- [Backend Deployment Guide](./BACKEND_DEPLOYMENT.md)
- [Frontend Deployment Guide](./FRONTEND_DEPLOYMENT.md)
- [Certificate Generation Guide](./CERTIFICATE_GENERATION_GUIDE.md)
- [Template System Guide](./TEMPLATE_SYSTEM_GUIDE.md)

---

**Built with ❤️ using Angular, Spring Boot, and PostgreSQL**

**Deployed on Netlify, Render, and Supabase** 🚀
