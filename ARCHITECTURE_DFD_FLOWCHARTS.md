# Certificate Management System - Data Flow Diagrams & Flowcharts

## 🔄 System Data Flow Diagram (Level 0 - Context)

```mermaid
graph TB
    User["👤 User"]
    System["🏢 Certificate<br/>Management System"]
    Email["📧 Email Service<br/>(Future)"]
    PDF["📄 PDF Service<br/>(Internal)"]
    
    User -->|Interacts| System
    System -->|Sends Notifications| Email
    System -->|Generates| PDF
    
    style User fill:#e1f5ff
    style System fill:#fff3e0
    style Email fill:#f3e5f5
    style PDF fill:#e8f5e9
```

---

## 📊 Level 1 DFD - Main Processes

```mermaid
graph TB
    User["👤 User"]
    Frontend["🖥️ Frontend<br/>Angular 21"]
    Backend["⚙️ Backend<br/>Spring Boot"]
    Database[("🗄️ Database<br/>PostgreSQL")]
    
    User -->|Browser Request| Frontend
    Frontend -->|REST API + JWT| Backend
    Backend -->|SQL Queries| Database
    Database -->|Result Set| Backend
    Backend -->|JSON Response| Frontend
    Frontend -->|Display| User
    
    style User fill:#e1f5ff
    style Frontend fill:#fff3e0
    style Backend fill:#fff3e0
    style Database fill:#e8f5e9
```

---

## 🔐 Authentication Data Flow

```mermaid
sequenceDiagram
    participant User as 👤 User
    participant Frontend as 🖥️ Angular Frontend
    participant Backend as ⚙️ Spring Backend
    participant Database as 🗄️ PostgreSQL
    
    rect rgb(200, 220, 255)
    Note over User,Database: Registration Flow
    User->>Frontend: Enter email & password
    Frontend->>Backend: POST /api/auth/register
    Backend->>Database: Save user (hashed password)
    Database-->>Backend: User created
    Backend-->>Frontend: 201 Created + JWT Token
    Frontend-->>User: Registration success
    end
    
    rect rgb(220, 200, 255)
    Note over User,Database: Login Flow
    User->>Frontend: Enter credentials
    Frontend->>Backend: POST /api/auth/login
    Backend->>Database: Query user by email
    Database-->>Backend: User record (hashed password)
    Backend->>Backend: Validate password (BCrypt)
    Backend->>Backend: Generate JWT token
    Backend-->>Frontend: 200 OK + JWT Token
    Frontend->>Frontend: Store token in LocalStorage
    Frontend-->>User: Login success
    end
```

---

## 📝 Template Creation & Certificate Generation Flow

```mermaid
sequenceDiagram
    participant User as 👤 User
    participant Frontend as 🖥️ Frontend<br/>CKEditor
    participant Backend as ⚙️ Backend<br/>Service
    participant DB as 🗄️ Database
    participant PDF as 📄 PDF<br/>Generator
    
    rect rgb(255, 230, 200)
    Note over User,DB: Template Creation
    User->>Frontend: Edit template with CKEditor
    User->>Frontend: Click "Save Template"
    Frontend->>Backend: POST /api/templates<br/>{name, htmlContent}
    Backend->>Backend: Validate HTML
    Backend->>DB: Save template (HTML stored)
    DB-->>Backend: Template ID returned
    Backend-->>Frontend: 201 Created + ID
    Frontend-->>User: Template saved ✓
    end
    
    rect rgb(200, 255, 200)
    Note over User,PDF: Certificate Generation
    User->>Frontend: Select template & customer
    Frontend->>Backend: POST /api/certificates/generate<br/>{templateId, customerId}
    Backend->>DB: Fetch template HTML
    DB-->>Backend: HTML content
    Backend->>DB: Fetch customer data
    DB-->>Backend: Customer details
    Backend->>Backend: Replace placeholders<br/>({{name}}, {{email}}, etc)
    Backend->>PDF: HTML → PDF conversion<br/>(iText 7)
    PDF-->>Backend: PDF bytes
    Backend->>Backend: Sign PDF (digital signature)
    Backend->>DB: Save certificate record
    DB-->>Backend: Certificate ID
    Backend-->>Frontend: 200 OK + PDF link
    Frontend-->>User: Display PDF preview
    User->>Frontend: Download certificate
    Frontend->>Backend: GET /api/certificates/ID/download
    Backend-->>Frontend: PDF file
    Frontend-->>User: ⬇️ PDF downloaded
    end
```

---

## 🗄️ Data Flow - Database Operations

```mermaid
graph TB
    User["👤 User"]
    
    subgraph Frontend["🖥️ Frontend Layer"]
        F1["Auth Component"]
        F2["Templates Component"]
        F3["Certificates Component"]
        F4["Customers Component"]
    end
    
    subgraph Backend["⚙️ Backend Layer"]
        B1["Auth Service"]
        B2["Template Service"]
        B3["Certificate Service"]
        B4["Customer Service"]
        B5["PDF Generator"]
        B6["Digital Signature"]
    end
    
    subgraph Database["🗄️ PostgreSQL Database"]
        D1["Users Table"]
        D2["Templates Table"]
        D3["Certificates Table"]
        D4["Customers Table"]
        D5["Audit Logs"]
    end
    
    User -->|Register/Login| F1
    F1 -->|HTTP| B1
    B1 -->|Query/Insert| D1
    
    User -->|Create Template| F2
    F2 -->|HTTP| B2
    B2 -->|Query/Insert| D2
    
    User -->|Generate Cert| F3
    F3 -->|HTTP| B3
    B3 -->|Read| D2
    B3 -->|Read| D4
    B3 -->|Call| B5
    B3 -->|Call| B6
    B3 -->|Insert| D3
    
    User -->|Manage Customers| F4
    F4 -->|HTTP| B4
    B4 -->|Query/Insert/Update| D4
    
    B1 & B2 & B3 & B4 -->|Log action| D5
    
    style User fill:#e1f5ff
    style Frontend fill:#fff3e0
    style Backend fill:#fff3e0
    style Database fill:#e8f5e9
```

---

## 🔄 Certificate Generation Process Flow

```mermaid
flowchart TD
    Start([User Requests<br/>Certificate]) -->|Select Template| Step1["Step 1: Fetch Template<br/>from Database"]
    
    Step1 -->|Get HTML| Step2["Step 2: Fetch Customer<br/>Data from Database"]
    
    Step2 -->|Customer Info| Step3["Step 3: Extract Placeholders<br/>{{name}}, {{email}}, {{date}}, etc"]
    
    Step3 -->|Placeholders| Step4["Step 4: Substitute Values<br/>in HTML"]
    
    Step4 -->|HTML Content| Step5["Step 5: HTML → PDF<br/>Conversion using iText 7"]
    
    Step5 -->|PDF Object| Step6["Step 6: Apply Digital<br/>Signature"]
    
    Step6 -->|Signed PDF| Step7["Step 7: Encrypt PDF<br/>with AES-256"]
    
    Step7 -->|Encrypted| Step8["Step 8: Save Certificate<br/>to Database"]
    
    Step8 -->|Certificate ID| Step9["Step 9: Return PDF Link<br/>to Frontend"]
    
    Step9 -->|Download Link| End([User Downloads<br/>Certificate PDF])
    
    Step3 -.->|Validation Error| ErrorVal["❌ Invalid Data<br/>Return Error"]
    ErrorVal --> EndErr([Return Error<br/>to User])
    
    Step5 -.->|Conversion Error| ErrorConv["❌ Conversion Error<br/>Return Error"]
    ErrorConv --> EndErr
    
    style Start fill:#e3f2fd
    style End fill:#c8e6c9
    style EndErr fill:#ffcdd2
    style Step1 fill:#fff9c4
    style Step2 fill:#fff9c4
    style Step3 fill:#fff9c4
    style Step4 fill:#fff9c4
    style Step5 fill:#fff9c4
    style Step6 fill:#fff9c4
    style Step7 fill:#fff9c4
    style Step8 fill:#fff9c4
    style Step9 fill:#fff9c4
    style ErrorVal fill:#ffcdd2
    style ErrorConv fill:#ffcdd2
```

---

## 🔐 Security & Authentication Flow

```mermaid
flowchart TD
    Request["🌐 HTTP Request<br/>from Browser"]
    
    Request -->|Check Header| CheckAuth{"Authorization<br/>Header<br/>Present?"}
    
    CheckAuth -->|No| Return401["❌ 401 Unauthorized<br/>Return Error"]
    Return401 --> EndFail["Reject Request"]
    
    CheckAuth -->|Yes| Extract["Extract JWT Token<br/>from Header"]
    
    Extract -->|Token| Verify{"JWT<br/>Signature<br/>Valid?"}
    
    Verify -->|Invalid| Return403["❌ 403 Forbidden<br/>Token Tampered"]
    Return403 --> EndFail
    
    Verify -->|Valid| CheckExpiry{"Token<br/>Expired?"}
    
    CheckExpiry -->|Yes| Return401b["❌ 401 Token Expired<br/>Request Re-login"]
    Return401b --> EndFail
    
    CheckExpiry -->|No| ExtractUser["Extract User ID<br/>from JWT Claims"]
    
    ExtractUser -->|User ID| CheckRole{"User Has<br/>Required<br/>Role?"}
    
    CheckRole -->|No| Return403b["❌ 403 Insufficient<br/>Permissions"]
    Return403b --> EndFail
    
    CheckRole -->|Yes| CheckCORS{"CORS<br/>Origin<br/>Allowed?"}
    
    CheckCORS -->|No| Return403c["❌ 403 CORS Policy<br/>Violation"]
    Return403c --> EndFail
    
    CheckCORS -->|Yes| Process["✅ Process Request<br/>Execute Business Logic"]
    
    Process -->|Complete| Response["Return 200 OK<br/>with Data"]
    
    Response --> EndSuccess["✅ Request Processed"]
    
    style Request fill:#e3f2fd
    style EndSuccess fill:#c8e6c9
    style EndFail fill:#ffcdd2
    style CheckAuth fill:#fffacd
    style Verify fill:#fffacd
    style CheckExpiry fill:#fffacd
    style CheckRole fill:#fffacd
    style CheckCORS fill:#fffacd
    style Return401 fill:#ffcdd2
    style Return403 fill:#ffcdd2
    style Return403b fill:#ffcdd2
    style Return403c fill:#ffcdd2
    style Return401b fill:#ffcdd2
```

---

## 🌐 Request-Response Cycle DFD

```mermaid
graph TB
    subgraph Client["🖥️ Client Side (Netlify)"]
        UI["User Interface<br/>Angular Component"]
        Service["Certificate Service<br/>HTTP Client"]
        Interceptor["Auth Interceptor<br/>Add JWT Token"]
        Cache["Local Cache<br/>LocalStorage"]
    end
    
    subgraph Network["🌐 Network"]
        CORS["CORS Policy<br/>Check"]
        HTTPS["HTTPS/TLS<br/>Encryption"]
    end
    
    subgraph Server["⚙️ Server Side (Render)"]
        Controller["REST Controller<br/>@PostMapping"]
        Filter["Spring Security<br/>Filter Chain"]
        Service2["Business Service<br/>Logic"]
        Repo["JPA Repository<br/>Data Access"]
    end
    
    subgraph DB["🗄️ Database (Supabase)"]
        PG["PostgreSQL<br/>Data Storage"]
    end
    
    UI -->|1. Call Service| Service
    Service -->|2. Add Interceptor| Interceptor
    Interceptor -->|3. Include JWT| Cache
    Interceptor -->|4. HTTP Request| CORS
    CORS -->|5. CORS Check| HTTPS
    HTTPS -->|6. Encrypt & Send| Controller
    Controller -->|7. Route Request| Filter
    Filter -->|8. Validate JWT| Service2
    Service2 -->|9. Business Logic| Repo
    Repo -->|10. Query| PG
    PG -->|11. Result Set| Repo
    Repo -->|12. Entity| Service2
    Service2 -->|13. Process Data| Controller
    Controller -->|14. JSON Response| HTTPS
    HTTPS -->|15. Decrypt| Service
    Service -->|16. Parse JSON| UI
    UI -->|17. Update View| UI
    
    style Client fill:#e3f2fd
    style Network fill:#fff9c4
    style Server fill:#fff3e0
    style DB fill:#e8f5e9
```

---

## 📊 CKEditor Template Editing Flow

```mermaid
flowchart TD
    Start(["👤 User Opens<br/>Template Editor"])
    
    Start --> Fetch["Fetch Template<br/>from Database"]
    
    Fetch --> Load["Load HTML into<br/>CKEditor 5"]
    
    Load --> Display["Display in<br/>WYSIWYG Mode"]
    
    Display --> Edit["User Edits Content<br/>in CKEditor"]
    
    Edit --> Validate{"Content<br/>Valid?"}
    
    Validate -->|Invalid| Error["Show Error<br/>Message"]
    Error --> Edit
    
    Validate -->|Valid| Preview["Show Live Preview"]
    
    Preview --> Decision{"Save<br/>Template?"}
    
    Decision -->|No| Discard["❌ Discard Changes"]
    Discard --> End(["Cancel"])
    
    Decision -->|Yes| Serialize["Serialize to<br/>Clean HTML"]
    
    Serialize --> Sanitize["Sanitize HTML<br/>(XSS Prevention)"]
    
    Sanitize --> Upload["POST Template<br/>to Backend"]
    
    Upload --> Save["Save HTML to<br/>Database"]
    
    Save --> Success["✅ Template Saved"]
    
    Success --> Redirect(["Redirect to<br/>Template List"])
    
    style Start fill:#e3f2fd
    style Redirect fill:#c8e6c9
    style End fill:#ffcdd2
    style Error fill:#ffcdd2
    style Discard fill:#ffcdd2
    style Edit fill:#fff9c4
    style Display fill:#fff9c4
    style Preview fill:#fff9c4
    style Validate fill:#fffacd
    style Decision fill:#fffacd
    style Serialize fill:#fff9c4
    style Sanitize fill:#fff9c4
    style Upload fill:#fff9c4
    style Save fill:#fff9c4
    style Success fill:#c8e6c9
```

---

## 🚀 Deployment Pipeline Flow

```mermaid
flowchart TD
    Start(["👨‍💻 Developer<br/>Push to Git"])
    
    Start -->|GitHub Webhook| FrontendCheck["🖥️ Frontend Pipeline"]
    
    FrontendCheck --> InstallF["Install Dependencies"]
    InstallF --> BuildF["Build Angular App<br/>ng build --prod"]
    BuildF --> TestF["Run Unit Tests"]
    TestF --> LintF["Run ESLint"]
    
    LintF --> FrontendOK{"All Checks<br/>Pass?"}
    
    FrontendOK -->|No| FrontendFail["❌ Build Failed<br/>Notify Developer"]
    FrontendFail --> EndFail(["Stop Pipeline"])
    
    FrontendOK -->|Yes| DeployF["Deploy to Netlify<br/>Upload to CDN"]
    
    DeployF -->|GitHub Webhook| BackendCheck["⚙️ Backend Pipeline"]
    
    BackendCheck --> CheckoutB["Checkout Code"]
    CheckoutB --> InstallB["Setup JDK 17"]
    InstallB --> BuildB["Build with Maven<br/>mvn clean package"]
    BuildB --> TestB["Run JUnit Tests"]
    TestB --> DockerB["Build Docker Image"]
    
    DockerB --> BackendOK{"All Checks<br/>Pass?"}
    
    BackendOK -->|No| BackendFail["❌ Build Failed<br/>Notify Developer"]
    BackendFail --> EndFail
    
    BackendOK -->|Yes| PushDocker["Push Image<br/>to Registry"]
    
    PushDocker --> DeployB["Deploy to Render<br/>Pull & Run Docker"]
    
    DeployF -.->|Parallel| DeployB
    
    DeployB -->|Health Check| Verify["Verify Services<br/>Running"]
    
    Verify --> VerfiyOK{"Services<br/>Healthy?"}
    
    VerfiyOK -->|No| Rollback["⚠️ Rollback to<br/>Previous Version"]
    Rollback --> EndFail
    
    VerfiyOK -->|Yes| RunTests["Run E2E Tests<br/>Integration Tests"]
    
    RunTests --> TestOK{"Tests<br/>Pass?"}
    
    TestOK -->|No| Rollback
    
    TestOK -->|Yes| Success["✅ Deployment Complete<br/>Service Live"]
    
    Success --> Monitor["Monitor Performance<br/>& Logs"]
    
    Monitor --> End(["🎉 Pipeline Complete"])
    
    style Start fill:#e3f2fd
    style End fill:#c8e6c9
    style EndFail fill:#ffcdd2
    style Success fill:#c8e6c9
    style FrontendCheck fill:#fff3e0
    style BackendCheck fill:#fff3e0
    style Rollback fill:#ffcdd2
    style Monitor fill:#fff9c4
```

---

## 🔄 Data Synchronization Flow

```mermaid
graph TB
    subgraph User1["👤 User 1"]
        Template1["Edit Template A<br/>Change Color"]
    end
    
    subgraph User2["👤 User 2"]
        Template2["View Template A<br/>(Before sync)"]
    end
    
    subgraph API["⚙️ Backend API"]
        Controller["Template Controller"]
        Service["Template Service"]
        Cache["In-Memory Cache"]
    end
    
    subgraph DB["🗄️ Database"]
        Table["Templates Table"]
    end
    
    Template1 -->|PUT /templates/1| Controller
    Controller --> Service
    Service -->|Invalidate Cache| Cache
    Service -->|UPDATE SQL| Table
    
    Table -->|Acknowledgement| Service
    Service -->|Response| Controller
    Controller -->|200 OK| Template1
    
    Template2 -->|GET /templates/1| Controller
    Controller -->|Cache Hit?| Cache
    Cache -->|No - Query DB| Table
    Table -->|Template Data| Service
    Service -->|Cache Result| Cache
    Service -->|Response| Controller
    Controller -->|Updated Template| Template2
    
    style User1 fill:#e3f2fd
    style User2 fill:#e3f2fd
    style API fill:#fff3e0
    style DB fill:#e8f5e9
```

---

## 📈 Performance Monitoring Flow

```mermaid
flowchart TD
    subgraph Sources["📊 Data Sources"]
        Frontend["🖥️ Frontend Metrics<br/>- Page Load Time<br/>- JS Errors<br/>- User Actions"]
        Backend["⚙️ Backend Metrics<br/>- API Response Time<br/>- DB Query Time<br/>- Memory Usage"]
        Database["🗄️ Database Metrics<br/>- Query Performance<br/>- Connections<br/>- Disk Usage"]
    end
    
    Frontend --> Collect1["Collect Metrics"]
    Backend --> Collect2["Collect Metrics"]
    Database --> Collect3["Collect Metrics"]
    
    Collect1 --> Aggregate["Aggregate & Normalize"]
    Collect2 --> Aggregate
    Collect3 --> Aggregate
    
    Aggregate --> Store["Store in<br/>Time-Series DB"]
    
    Store --> Analyze["Analyze Patterns<br/>ML Models"]
    
    Analyze --> Alert{"Threshold<br/>Exceeded?"}
    
    Alert -->|No| Dashboard["Display in<br/>Dashboard"]
    Alert -->|Yes| SendAlert["⚠️ Send Alert<br/>Email/Slack"]
    
    SendAlert --> Investigate["Investigate Issue<br/>Check Logs"]
    
    Investigate --> Resolve["Apply Fix<br/>Deploy Solution"]
    
    Dashboard --> Monitor["Continuous Monitoring"]
    
    Resolve --> Monitor
    
    Monitor -.->|Feedback Loop| Sources
    
    style Sources fill:#e3f2fd
    style Aggregate fill:#fff9c4
    style Analyze fill:#fff9c4
    style Alert fill:#fffacd
    style SendAlert fill:#ffcdd2
    style Dashboard fill:#c8e6c9
    style Monitor fill:#c8e6c9
```

---

## 🔒 Backup & Recovery Flow

```mermaid
flowchart TD
    DB["🗄️ PostgreSQL Database<br/>(Supabase)"]
    
    DB -->|Automatic Daily| Backup["🔄 Create Backup<br/>Full Database Dump"]
    
    Backup -->|Compress| Zip["Compress Backup<br/>gzip"]
    
    Zip -->|Encrypt| Encrypt["Encrypt with AES-256<br/>Customer Key"]
    
    Encrypt -->|Store| Storage["Store in<br/>Geo-Redundant Storage"]
    
    Storage -->|Multiple Copies| MultiRegion["Replicate to<br/>Multiple Regions"]
    
    Storage -->|Version Control| Versions["Keep Last 30 Versions<br/>Point-in-Time Recovery"]
    
    Versions --> Monitor["Monitor Backup<br/>Integrity"]
    
    Monitor --> TestRestore["Weekly Test<br/>Restore from Backup"]
    
    TestRestore --> VerifyOK{"Data<br/>Integrity<br/>OK?"}
    
    VerifyOK -->|No| Alert1["⚠️ Alert: Backup<br/>Corruption Detected"]
    Alert1 --> Manual["Manual Investigation"]
    
    VerifyOK -->|Yes| Success1["✅ Backup Valid"]
    Success1 --> Notify["Notify Administrator"]
    
    Notify --> Incident["🚨 If Disaster<br/>Occurs"]
    
    Incident --> Trigger["Trigger Restore<br/>Process"]
    
    Trigger --> Restore["Restore from Latest<br/>Backup"]
    
    Restore --> RTO["Recovery Time<br/>< 15 minutes"]
    
    RTO --> RPO["Recovery Point<br/>< 24 hours"]
    
    RPO --> Live["Database Live<br/>Minimal Data Loss"]
    
    style DB fill:#e8f5e9
    style Backup fill:#fff9c4
    style MultiRegion fill:#c8e6c9
    style Versions fill:#c8e6c9
    style Alert1 fill:#ffcdd2
    style Success1 fill:#c8e6c9
    style Live fill:#c8e6c9
```

---

## 🎯 Error Handling & Recovery Flow

```mermaid
flowchart TD
    Request["🌐 User Request"]
    
    Request --> Try["Try: Execute Operation"]
    
    Try --> Execute["Perform Business Logic"]
    
    Execute --> Catch{"Exception<br/>Caught?"}
    
    Catch -->|No| Success["✅ Success"]
    Success --> Log1["Log Success Event"]
    Log1 --> Return["Return 200 + Data"]
    Return --> End1(["Response to User"])
    
    Catch -->|Yes| Identify["Identify Exception<br/>Type"]
    
    Identify --> Type1{"Exception<br/>Type?"}
    
    Type1 -->|Validation Error| Val["❌ Validation Failed"]
    Val --> LogVal["Log Warning"]
    LogVal --> ReturnVal["Return 400<br/>Bad Request"]
    
    Type1 -->|Database Error| DB["❌ Database Error"]
    DB --> LogDB["Log Error<br/>Stack Trace"]
    LogDB --> ReturnDB["Return 500<br/>Internal Error"]
    
    Type1 -->|Auth Error| Auth["❌ Authorization Failed"]
    Auth --> LogAuth["Log Security Event"]
    LogAuth --> ReturnAuth["Return 403<br/>Forbidden"]
    
    Type1 -->|Resource Not Found| NotFound["❌ Resource Missing"]
    NotFound --> LogNotFound["Log Info"]
    LogNotFound --> ReturnNotFound["Return 404<br/>Not Found"]
    
    Type1 -->|Timeout| Timeout["❌ Operation Timeout"]
    Timeout --> LogTimeout["Log Timeout"]
    LogTimeout --> ReturnTimeout["Return 503<br/>Service Unavailable"]
    
    ReturnVal --> Finally["Finally Block:<br/>Cleanup Resources"]
    ReturnDB --> Finally
    ReturnAuth --> Finally
    ReturnNotFound --> Finally
    ReturnTimeout --> Finally
    
    Finally --> Cleanup["Close DB Connections<br/>Release Memory"]
    
    Cleanup --> Alert{"Critical<br/>Error?"}
    
    Alert -->|Yes| SendAlert["⚠️ Send Alert<br/>to Ops Team"]
    Alert -->|No| Dashboard["Log to Dashboard"]
    
    SendAlert --> Monitor["Monitor for<br/>Patterns"]
    Dashboard --> Monitor
    
    Monitor --> End2(["Response to User"])
    End1 --> end(["Complete"])
    End2 --> end
    
    style Success fill:#c8e6c9
    style Val fill:#ffcdd2
    style DB fill:#ffcdd2
    style Auth fill:#ffcdd2
    style NotFound fill:#ffcdd2
    style Timeout fill:#ffcdd2
    style SendAlert fill:#ffcdd2
    style End1 fill:#c8e6c9
    style End2 fill:#c8e6c9
```

---

## 📤 Certificate Download & Delivery Flow

```mermaid
sequenceDiagram
    participant User as 👤 User<br/>Browser
    participant Frontend as 🖥️ Frontend<br/>Netlify
    participant Backend as ⚙️ Backend<br/>Render
    participant Database as 🗄️ DB<br/>Supabase
    participant SignService as 🔐 Signature<br/>Service
    
    rect rgb(200, 230, 255)
    Note over User,Backend: Certificate Download Request
    User->>Frontend: Click Download Button
    Frontend->>Backend: GET /api/certificates/{id}/download<br/>Authorization: Bearer JWT
    Backend->>Database: Query certificate record
    Database-->>Backend: Certificate metadata + PDF bytes
    Backend->>SignService: Verify signature on PDF
    SignService-->>Backend: ✅ Signature Valid
    Backend->>Backend: Check file size & cache headers
    Backend->>Backend: Set Content-Type: application/pdf<br/>Set Content-Disposition: attachment
    Backend-->>Frontend: 200 OK + PDF Stream
    Frontend->>Frontend: Create blob from stream
    Frontend->>Frontend: Generate download link
    Frontend-->>User: ⬇️ Start browser download
    User->>User: Certificate.pdf saved to Downloads
    end
    
    rect rgb(230, 200, 255)
    Note over Backend,Database: Audit & Logging
    Backend->>Database: Log download event<br/>user_id, certificate_id, timestamp, IP
    Database-->>Backend: Audit log created
    Backend->>Backend: Update certificate<br/>last_accessed timestamp
    Database-->>Backend: Updated
    end
    
    rect rgb(200, 255, 230)
    Note over Frontend,User: Delivery Confirmation
    Frontend->>Frontend: Clear temp objects
    Frontend->>User: Show confirmation message
    User->>User: ✅ Certificate downloaded
    end
```

---

## 🔍 Template Search & Filter Flow

```mermaid
flowchart TD
    Start(["User Opens<br/>Template List"])
    
    Start --> Load["Load All Templates<br/>with Pagination"]
    
    Load --> Display["Display in Table<br/>25 items per page"]
    
    Display --> User["User Input:<br/>Search Keywords<br/>or Filters"]
    
    User --> Input["Enter search term<br/>or select filters"]
    
    Input --> Frontend["Frontend Validation<br/>- Min 2 characters<br/>- Max 100 characters"]
    
    Frontend --> Valid{"Input<br/>Valid?"}
    
    Valid -->|No| Error["Show Error<br/>Message"]
    Error --> User
    
    Valid -->|Yes| Query["Build Query:<br/>WHERE name ILIKE %?%<br/>AND status = ?"
    ]
    
    Query --> Backend["POST /api/templates/search<br/>{keyword, filters, page}"]
    
    Backend --> DB["Execute Query<br/>with Indexes"]
    
    DB --> Results["Return Matching<br/>Templates"]
    
    Results --> Paginate["Paginate Results<br/>25 per page"]
    
    Paginate --> Cache["Cache Results<br/>5 minute TTL"]
    
    Cache --> Return["Return to Frontend<br/>with Metadata"]
    
    Return --> Frontend2["Display Results<br/>Highlight matches<br/>Show pagination"]
    
    Frontend2 --> User2{{"User Actions<br/>- View<br/>- Edit<br/>- Delete<br/>- Clone"}}
    
    User2 -->|View| ViewTemplate["Open Template<br/>Details"]
    User2 -->|Edit| EditTemplate["Load in CKEditor"]
    User2 -->|Delete| DeleteTemplate["Confirm Delete<br/>Remove from DB"]
    User2 -->|Clone| CloneTemplate["Create Copy<br/>Save as New"]
    
    ViewTemplate --> End(["Display Template"])
    EditTemplate --> End
    DeleteTemplate --> End
    CloneTemplate --> End
    
    style Start fill:#e3f2fd
    style End fill:#c8e6c9
    style Error fill:#ffcdd2
    style Display fill:#fff9c4
    style Frontend fill:#fff3e0
    style Backend fill:#fff3e0
    style DB fill:#e8f5e9
    style Cache fill:#c8e6c9
```

---

## 📋 Summary Table - DFD Components

| Component | Location | Data In | Data Out | Function |
|-----------|----------|---------|----------|----------|
| **User** | Browser | None | Actions | Initiates requests |
| **Angular Frontend** | Netlify | HTTP responses | HTTP requests + JWT | UI & client logic |
| **Spring Backend** | Render | DB queries | REST responses | Business logic |
| **PostgreSQL** | Supabase | Insert/Update/Delete | Query results | Data persistence |
| **CKEditor** | Frontend | HTML content | Serialized HTML | Template editing |
| **iText PDF** | Backend | HTML + data | PDF bytes | PDF generation |
| **Digital Signature** | Backend | PDF bytes + private key | Signed PDF | Certificate signing |
| **JWT Token** | Network | Credentials | Authorization header | Authentication |

---

## 🎯 How to Use These Diagrams

### Option 1: View on GitHub
- These Mermaid diagrams render automatically on GitHub
- View the raw markdown file to see the diagram syntax
- Great for documentation & code review

### Option 2: Export as Images
```bash
# Install mermaid-cli
npm install -g @mermaid-js/mermaid-cli

# Convert to PNG
mmdc -i ARCHITECTURE_DFD_FLOWCHARTS.md -o architecture_diagrams.png

# Convert to SVG
mmdc -i ARCHITECTURE_DFD_FLOWCHARTS.md -o architecture_diagrams.svg
```

### Option 3: Embed in Google Slides
1. Export each diagram as SVG
2. Insert SVG image into Google Slides
3. Select & right-click → "Edit image" for unlimited editing

### Option 4: Use Mermaid Live Editor
1. Go to [https://mermaid.live](https://mermaid.live)
2. Paste diagram code
3. Export as PNG/SVG
4. Insert into presentations

### Option 5: Convert to PDF
```bash
# Using headless Chrome
node -e "require('puppeteer')" && \
google-chrome --headless --print-to-pdf architecture_diagrams.pdf
```

---

## 🎨 Diagram Style Guide

**Colors Used:**
- 🔵 **Light Blue** (`#e3f2fd`) - Start/End, User, Client-side
- 🟡 **Light Yellow** (`#fff9c4`, `#fffacd`) - Process, Decision, Server-side
- 🟠 **Light Orange** (`#fff3e0`) - API, Services
- 🟢 **Light Green** (`#e8f5e9`, `#c8e6c9`) - Database, Success, Complete
- 🔴 **Light Red** (`#ffcdd2`) - Errors, Failures, Blocked

**Symbols:**
- 👤 = User/Actor
- 🖥️ = Frontend
- ⚙️ = Backend/Service
- 🗄️ = Database
- 🌐 = Network/Internet
- 📄 = Files/PDF
- 🔐 = Security
- ✅ = Success
- ❌ = Failure
- ⚠️ = Warning/Alert

