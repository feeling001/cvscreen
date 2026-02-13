# CVScreen Installation Guide

This guide will help you set up CVScreen v0.1 on your system.

## System Requirements

- **Java:** 17 or higher
- **Node.js:** 18 or higher
- **PostgreSQL:** 14 or higher
- **Maven:** 3.8 or higher
- **npm:** 8 or higher

## Step-by-Step Installation

### 1. Database Setup

#### Install PostgreSQL

If PostgreSQL is not installed, install it:

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**macOS (using Homebrew):**
```bash
brew install postgresql
brew services start postgresql
```

**Windows:**
Download and install from https://www.postgresql.org/download/windows/

#### Create Database

```bash
# Connect to PostgreSQL
sudo -u postgres psql

# In PostgreSQL shell:
CREATE DATABASE cvscreen;
CREATE USER cvscreen WITH PASSWORD 'cvscreen';
GRANT ALL PRIVILEGES ON DATABASE cvscreen TO cvscreen;
\c cvscreen
GRANT ALL ON SCHEMA public TO cvscreen;
\q
```

Or use the provided SQL script:
```bash
psql -U postgres -f backend/database/setup.sql
```

### 2. Backend Setup

#### Navigate to backend directory
```bash
cd cvscreen/backend
```

#### Build the application
```bash
mvn clean install
```

#### Run the backend
```bash
mvn spring-boot:run
```

The backend will start on port 8081 and create the database tables automatically.

Default admin user will be created:
- Username: `admin`
- Password: `admin123`

### 3. Frontend Setup

#### Navigate to frontend directory
```bash
cd cvscreen/frontend
```

#### Install dependencies
```bash
npm install
```

#### Start development server
```bash
npm start
```

The frontend will start on port 8082.

### 4. Access the Application

Open your browser and navigate to:
```
http://localhost:8082
```

Login with:
- Username: `admin`
- Password: `admin123`

## Quick Start (Using Script)

Alternatively, you can use the provided startup script:

```bash
cd cvscreen
./start.sh
```

This script will:
1. Check PostgreSQL is running
2. Create database if needed
3. Start backend
4. Start frontend

## Troubleshooting

### Port Already in Use

If ports 8081 or 8082 are already in use, you can change them:

**Backend:** Edit `backend/src/main/resources/application.properties`
```properties
server.port=8081
```

**Frontend:** Edit `frontend/package.json`
```json
"start": "ng serve --port 8082"
```

### Database Connection Issues

Check your database configuration in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cvscreen
spring.datasource.username=cvscreen
spring.datasource.password=cvscreen
```

### CORS Issues

If you encounter CORS errors, verify the allowed origins in:
- `backend/src/main/resources/application.properties`
- `backend/src/main/java/com/cvscreen/config/SecurityConfig.java`

### Build Errors

**Backend:**
```bash
cd backend
mvn clean install -U
```

**Frontend:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

## Production Deployment

### Backend (Production Build)

```bash
cd backend
mvn clean package -DskipTests
java -jar target/cvscreen-backend-0.1.jar
```

### Frontend (Production Build)

```bash
cd frontend
npm run build
```

Serve the `dist/cvscreen-frontend` directory with a web server (nginx, Apache, etc.).

### Environment Variables

For production, use environment variables instead of hardcoded values:

```bash
export DB_HOST=your-db-host
export DB_PORT=5432
export DB_NAME=cvscreen
export DB_USER=cvscreen
export DB_PASSWORD=your-secure-password
export JWT_SECRET=your-very-long-random-secret
```

## Next Steps

- Change the default admin password
- Configure backup for the database
- Set up SSL/HTTPS
- Configure proper logging
- Set up monitoring

## Support

For issues or questions, refer to the README files in backend and frontend directories.
