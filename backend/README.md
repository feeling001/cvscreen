# CVScreen Backend

Spring Boot REST API for IT recruitment management application.

## Technology Stack

- Java 17
- Spring Boot 3.2.1
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Maven

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+

## Database Setup

1. Create PostgreSQL database:

```bash
createdb -U postgres cvscreen
```

2. Create user and grant permissions:

```sql
CREATE USER cvscreen WITH PASSWORD 'cvscreen';
GRANT ALL PRIVILEGES ON DATABASE cvscreen TO cvscreen;
GRANT ALL ON SCHEMA public TO cvscreen;
```

## Configuration

Database configuration is in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cvscreen
spring.datasource.username=cvscreen
spring.datasource.password=cvscreen
```

## Build and Run

### Using Maven

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

### Using JAR

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/cvscreen-backend-0.1.jar
```

The application will start on port 8081: http://localhost:8081/api

## Default Credentials

- **Username:** admin
- **Password:** admin123

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login and get JWT token

### Candidates
- `GET /api/candidates` - Get all candidates
- `GET /api/candidates/{id}` - Get candidate by ID
- `GET /api/candidates/search?q={term}` - Search candidates
- `POST /api/candidates` - Create new candidate
- `PUT /api/candidates/{id}` - Update candidate
- `DELETE /api/candidates/{id}` - Delete candidate

### Jobs
- `GET /api/jobs` - Get all jobs
- `GET /api/jobs/{id}` - Get job by ID
- `GET /api/jobs/reference/{reference}` - Get job by reference
- `GET /api/jobs/search?q={term}` - Search jobs
- `POST /api/jobs` - Create new job
- `PUT /api/jobs/{id}` - Update job
- `DELETE /api/jobs/{id}` - Delete job

### Applications
- `GET /api/applications` - Get all applications
- `GET /api/applications/{id}` - Get application by ID
- `GET /api/applications/search` - Search applications with filters
- `POST /api/applications` - Create new application
- `PUT /api/applications/{id}` - Update application
- `DELETE /api/applications/{id}` - Delete application
- `POST /api/applications/{id}/cv` - Upload CV file

### Companies
- `GET /api/companies` - Get all companies
- `GET /api/companies/{id}` - Get company by ID
- `GET /api/companies/search?q={term}` - Search companies
- `POST /api/companies` - Create new company
- `PUT /api/companies/{id}` - Update company
- `DELETE /api/companies/{id}` - Delete company

### Candidate Reviews
- `GET /api/candidates/{candidateId}/reviews` - Get reviews for candidate
- `POST /api/candidates/{candidateId}/reviews` - Create review
- `DELETE /api/candidates/{candidateId}/reviews/{reviewId}` - Delete review

### CSV Import
- `POST /api/import/csv` - Import applications from CSV file

## CSV Import Format

CSV file should contain the following columns:

```
firstName,lastName,company,jobReference,dailyRate,applicationDate,status,conclusion,roleCategory
```

Example:
```
John,Doe,Accenture,I01234,650,2024-01-15,CV_REVIEWED,Good profile,System Architect
```

## Security

- JWT-based authentication
- Token expiration: 24 hours
- Password encryption: BCrypt
- CORS enabled for http://localhost:8082

## File Storage

CVs are stored in `./cvs` directory relative to the application working directory.

## Testing

Run tests with:

```bash
mvn test
```

## Logging

Logs are configured for:
- Application: DEBUG level
- Spring Security: DEBUG level
- Hibernate SQL: DEBUG level

## Future Enhancements

- Role-based access control (RBAC)
- OAuth2/SSO integration
- Email notifications
- Advanced reporting
- API documentation with Swagger/OpenAPI
