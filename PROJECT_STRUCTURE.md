# CVScreen v0.1 - Project Structure

## Overview

CVScreen is a full-stack IT recruitment management application consisting of:
- **Backend:** Spring Boot REST API (Port 8081)
- **Frontend:** Angular SPA (Port 8082)
- **Database:** PostgreSQL

## Directory Structure

```
cvscreen/
├── backend/                          # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cvscreen/
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   │   ├── DataInitializer.java
│   │   │   │   │   └── SecurityConfig.java
│   │   │   │   ├── controller/      # REST API Controllers
│   │   │   │   │   ├── ApplicationController.java
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── CandidateController.java
│   │   │   │   │   ├── CandidateReviewController.java
│   │   │   │   │   ├── CompanyController.java
│   │   │   │   │   ├── CSVImportController.java
│   │   │   │   │   └── JobController.java
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   │   ├── ApplicationDTO.java
│   │   │   │   │   ├── ApplicationSummaryDTO.java
│   │   │   │   │   ├── CandidateDTO.java
│   │   │   │   │   ├── CandidateReviewDTO.java
│   │   │   │   │   ├── CompanyDTO.java
│   │   │   │   │   ├── CreateApplicationRequest.java
│   │   │   │   │   ├── CreateCandidateRequest.java
│   │   │   │   │   ├── CreateJobRequest.java
│   │   │   │   │   ├── CreateReviewRequest.java
│   │   │   │   │   ├── JobDTO.java
│   │   │   │   │   ├── JwtResponse.java
│   │   │   │   │   └── LoginRequest.java
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   │   │   ├── Application.java
│   │   │   │   │   ├── Candidate.java
│   │   │   │   │   ├── CandidateReview.java
│   │   │   │   │   ├── Company.java
│   │   │   │   │   ├── Job.java
│   │   │   │   │   └── User.java
│   │   │   │   ├── exception/       # Custom Exceptions
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   └── ResourceNotFoundException.java
│   │   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   │   │   ├── ApplicationRepository.java
│   │   │   │   │   ├── CandidateRepository.java
│   │   │   │   │   ├── CandidateReviewRepository.java
│   │   │   │   │   ├── CompanyRepository.java
│   │   │   │   │   ├── JobRepository.java
│   │   │   │   │   └── UserRepository.java
│   │   │   │   ├── security/        # Security Components
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   ├── JwtUtils.java
│   │   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   │   ├── service/         # Business Logic Services
│   │   │   │   │   ├── ApplicationService.java
│   │   │   │   │   ├── CandidateReviewService.java
│   │   │   │   │   ├── CandidateService.java
│   │   │   │   │   ├── CompanyService.java
│   │   │   │   │   ├── CSVImportService.java
│   │   │   │   │   └── JobService.java
│   │   │   │   └── CVScreenApplication.java  # Main Application Class
│   │   │   └── resources/
│   │   │       └── application.properties    # Configuration
│   │   └── test/                    # Unit Tests
│   ├── pom.xml                      # Maven Dependencies
│   └── README.md                    # Backend Documentation
│
├── frontend/                        # Angular Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/          # UI Components
│   │   │   │   ├── applications/
│   │   │   │   ├── candidates/
│   │   │   │   │   ├── candidates.component.css
│   │   │   │   │   ├── candidates.component.html
│   │   │   │   │   └── candidates.component.ts
│   │   │   │   ├── companies/
│   │   │   │   ├── dashboard/
│   │   │   │   │   ├── dashboard.component.css
│   │   │   │   │   ├── dashboard.component.html
│   │   │   │   │   └── dashboard.component.ts
│   │   │   │   ├── jobs/
│   │   │   │   └── login/
│   │   │   │       ├── login.component.css
│   │   │   │       ├── login.component.html
│   │   │   │       └── login.component.ts
│   │   │   ├── guards/              # Route Guards
│   │   │   │   └── auth.guard.ts
│   │   │   ├── interceptors/        # HTTP Interceptors
│   │   │   │   └── jwt.interceptor.ts
│   │   │   ├── models/              # TypeScript Interfaces
│   │   │   │   ├── application.model.ts
│   │   │   │   ├── auth.model.ts
│   │   │   │   ├── candidate.model.ts
│   │   │   │   ├── company.model.ts
│   │   │   │   └── job.model.ts
│   │   │   ├── services/            # API Services
│   │   │   │   ├── application.service.ts
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── candidate.service.ts
│   │   │   │   ├── company.service.ts
│   │   │   │   └── job.service.ts
│   │   │   ├── app.component.ts     # Root Component
│   │   │   ├── app.config.ts        # App Configuration
│   │   │   └── app.routes.ts        # Routing Configuration
│   │   ├── environments/            # Environment Configs
│   │   │   └── environment.ts
│   │   ├── assets/                  # Static Assets
│   │   ├── index.html               # Main HTML
│   │   ├── main.ts                  # App Bootstrap
│   │   └── styles.css               # Global Styles
│   ├── angular.json                 # Angular CLI Config
│   ├── package.json                 # npm Dependencies
│   ├── tsconfig.json                # TypeScript Config
│   └── README.md                    # Frontend Documentation
│
├── .gitignore                       # Git Ignore Rules
├── INSTALLATION.md                  # Installation Guide
├── README.md                        # Main Documentation
├── sample-import.csv                # Sample CSV for Import
└── start.sh                         # Startup Script
```

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.2.1
- **Language:** Java 17
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security + JWT
- **Build Tool:** Maven
- **CSV Processing:** OpenCSV

### Frontend
- **Framework:** Angular 17
- **Language:** TypeScript
- **UI Library:** Angular Material
- **HTTP Client:** Angular HttpClient
- **State Management:** RxJS
- **Build Tool:** Angular CLI

## Key Features Implemented

### Authentication & Security
- JWT-based authentication
- Password encryption with BCrypt
- HTTP interceptor for token injection
- Route guards for protected pages
- CORS configuration

### Candidate Management
- CRUD operations for candidates
- Search by name
- View complete application history
- Add reviews/comments
- Track historical evaluations

### Job Management
- CRUD operations for jobs
- Unique job references (e.g., I01234)
- Job status tracking (OPEN, CLOSED, ON_HOLD)
- Category-based organization

### Application Management
- Link applications to jobs or create spontaneous applications
- Mandatory role/category
- Optional company association
- Daily rate tracking
- Application status workflow
- CV file upload
- Evaluation notes and conclusions

### Company Management
- CRUD operations for consultancy companies
- Search by name
- Track applications per company

### Reviews & Collaboration
- Multiple users can review candidates
- Timestamped and attributed comments
- Immutable review history
- Visible across candidate details

### CSV Import
- Bulk import applications
- Auto-create missing entities
- Reuse existing candidates, jobs, companies
- Flexible date format parsing

## Database Schema

```sql
-- Main entities
candidates (id, first_name, last_name, global_notes, created_at, updated_at)
users (id, username, display_name, password, enabled, created_at)
jobs (id, reference, title, category, publication_date, status, source, description, created_at, updated_at)
companies (id, name, notes, created_at)

-- Core entity
applications (
  id, candidate_id, job_id, role_category, company_id,
  daily_rate, application_date, status, conclusion,
  evaluation_notes, cv_file_path, created_at, updated_at
)

-- Reviews
candidate_reviews (id, candidate_id, user_id, comment, created_at)
```

## API Endpoints

### Authentication
- POST `/api/auth/login` - Login and get JWT token

### Candidates
- GET `/api/candidates` - List all
- GET `/api/candidates/{id}` - Get by ID
- GET `/api/candidates/search?q={term}` - Search
- POST `/api/candidates` - Create
- PUT `/api/candidates/{id}` - Update
- DELETE `/api/candidates/{id}` - Delete

### Jobs
- GET `/api/jobs` - List all
- GET `/api/jobs/{id}` - Get by ID
- GET `/api/jobs/reference/{ref}` - Get by reference
- GET `/api/jobs/search?q={term}` - Search
- POST `/api/jobs` - Create
- PUT `/api/jobs/{id}` - Update
- DELETE `/api/jobs/{id}` - Delete

### Applications
- GET `/api/applications` - List all
- GET `/api/applications/{id}` - Get by ID
- GET `/api/applications/search` - Advanced search with filters
- POST `/api/applications` - Create
- PUT `/api/applications/{id}` - Update
- DELETE `/api/applications/{id}` - Delete
- POST `/api/applications/{id}/cv` - Upload CV

### Companies
- GET `/api/companies` - List all
- GET `/api/companies/{id}` - Get by ID
- GET `/api/companies/search?q={term}` - Search
- POST `/api/companies` - Create
- PUT `/api/companies/{id}` - Update
- DELETE `/api/companies/{id}` - Delete

### Reviews
- GET `/api/candidates/{candidateId}/reviews` - Get reviews
- POST `/api/candidates/{candidateId}/reviews` - Add review
- DELETE `/api/candidates/{candidateId}/reviews/{reviewId}` - Delete review

### Import
- POST `/api/import/csv` - Import applications from CSV

## Configuration

### Backend Ports
- Application: 8081
- Context path: `/api`

### Frontend Ports
- Development server: 8082

### Database
- Host: localhost
- Port: 5432
- Database: cvscreen
- User: cvscreen
- Password: cvscreen

### Default User
- Username: admin
- Password: admin123

## Development Workflow

1. Start PostgreSQL
2. Run backend: `cd backend && mvn spring-boot:run`
3. Run frontend: `cd frontend && npm start`
4. Access at: http://localhost:8082

Or use the quick start script:
```bash
./start.sh
```

## Next Steps

### Phase 2 Features
- Complete all CRUD forms in Angular
- Advanced filtering and sorting
- Dashboard with statistics
- Role-based access control
- OAuth2/SSO integration
- Email notifications
- Export functionality
- Advanced reporting

### Technical Improvements
- Unit and integration tests
- API documentation (Swagger/OpenAPI)
- Docker containerization
- CI/CD pipeline
- Performance optimization
- Error handling improvements
- Logging and monitoring

## License

Proprietary - Internal Use Only
