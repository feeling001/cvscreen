# CVScreen - IT Recruitment Application

**Version:** 0.1  
**Language:** English

## Overview

CVScreen is a comprehensive IT recruitment management application designed to track job applications, manage candidates, and provide historical decision support for recruiters.

## Key Features

- Job offer management
- Candidate tracking with complete history
- Application status management
- Consultancy company management
- CSV bulk import
- Candidate reviews and collaboration
- Advanced search and filtering

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.x
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven

### Frontend
- Angular 17+
- TypeScript
- Angular Material

## Architecture

```
cvscreen/
├── backend/          # Spring Boot application
└── frontend/         # Angular application
```

## Database Configuration

- **Database:** PostgreSQL
- **Name:** cvscreen
- **User:** cvscreen
- **Password:** cvscreen
- **Host:** localhost
- **Port:** 5432

## Application Ports

- **Backend:** http://localhost:8081
- **Frontend:** http://localhost:8082

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+ and npm
- PostgreSQL 14+
- Maven 3.8+

### Database Setup

```bash
# Create PostgreSQL database
createdb -U postgres cvscreen
psql -U postgres -d cvscreen -c "CREATE USER cvscreen WITH PASSWORD 'cvscreen';"
psql -U postgres -d cvscreen -c "GRANT ALL PRIVILEGES ON DATABASE cvscreen TO cvscreen;"
psql -U postgres -d cvscreen -c "GRANT ALL ON SCHEMA public TO cvscreen;"
```

### Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will be available at http://localhost:8081

### Frontend Setup

```bash
cd frontend
npm install
ng serve --port 8082
```

The frontend will be available at http://localhost:8082

## Default Credentials

- **Username:** admin
- **Password:** admin123

## Documentation

See individual README files in backend and frontend directories for more details.

## License

Proprietary - Internal Use Only
