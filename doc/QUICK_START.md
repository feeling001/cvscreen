# CVScreen v0.1 - Quick Start Guide

## Prerequisites Checklist

Before starting, ensure you have:

- [ ] Java 17 or higher installed (`java -version`)
- [ ] Maven 3.8+ installed (`mvn -version`)
- [ ] Node.js 18+ installed (`node -version`)
- [ ] npm installed (`npm -version`)
- [ ] PostgreSQL 14+ installed and running (`pg_isready`)

## 5-Minute Setup

### Step 1: Database Setup (1 minute)

```bash
# Create database
createdb -U postgres cvscreen

# Create user and grant permissions
psql -U postgres -d cvscreen << EOF
CREATE USER cvscreen WITH PASSWORD 'cvscreen';
GRANT ALL PRIVILEGES ON DATABASE cvscreen TO cvscreen;
GRANT ALL ON SCHEMA public TO cvscreen;
EOF
```

### Step 2: Start Backend (2 minutes)

```bash
cd cvscreen/backend
mvn clean install
mvn spring-boot:run
```

Wait until you see: `Started CVScreenApplication in X.XXX seconds`

The backend is now running on http://localhost:8081/api

### Step 3: Start Frontend (2 minutes)

Open a new terminal:

```bash
cd cvscreen/frontend
npm install
npm start
```

Wait until you see: `Application bundle generation complete.`

The frontend is now running on http://localhost:8082

### Step 4: Login

1. Open browser: http://localhost:8082
2. Login with:
   - Username: `admin`
   - Password: `admin123`

🎉 **You're ready to go!**

## Alternative: One-Command Startup

If you prefer, use the startup script:

```bash
cd cvscreen
chmod +x start.sh
./start.sh
```

This will automatically:
1. Check PostgreSQL
2. Create database if needed
3. Start backend
4. Start frontend

## What You Can Do Now

### 1. View Candidates
- Click "Candidates" in the sidebar
- Search for candidates
- View candidate details

### 2. Import Sample Data

Use the provided sample CSV:

```bash
# In the UI, use the import feature (when implemented)
# Or use curl:
curl -X POST http://localhost:8081/api/import/csv \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@sample-import.csv"
```

### 3. Explore the API

Get JWT token first:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Then use the token to access protected endpoints:
```bash
# Get all candidates
curl http://localhost:8081/api/candidates \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get all jobs
curl http://localhost:8081/api/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get all applications
curl http://localhost:8081/api/applications \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Common Issues & Solutions

### Issue: Port 8081 already in use
**Solution:** Change backend port in `backend/src/main/resources/application.properties`
```properties
server.port=8090
```

### Issue: Port 8082 already in use
**Solution:** Change frontend port in `frontend/package.json`
```json
"start": "ng serve --port 4200"
```

### Issue: Database connection failed
**Solution:** Check PostgreSQL is running and credentials are correct
```bash
psql -U cvscreen -d cvscreen -h localhost
```

### Issue: "Cannot find module" in frontend
**Solution:** Delete and reinstall node_modules
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### Issue: Maven build fails
**Solution:** Clean and rebuild
```bash
cd backend
mvn clean install -U
```

## Project Structure at a Glance

```
cvscreen/
├── backend/          → Spring Boot API (port 8081)
│   ├── src/
│   └── pom.xml
├── frontend/         → Angular UI (port 8082)
│   ├── src/
│   └── package.json
├── README.md         → Main documentation
├── INSTALLATION.md   → Detailed setup guide
├── PROJECT_STRUCTURE.md → Complete architecture
└── start.sh          → One-command startup
```

## Next Steps

1. **Explore the code:**
   - Backend: `backend/src/main/java/com/cvscreen/`
   - Frontend: `frontend/src/app/`

2. **Read the documentation:**
   - README.md - Overview
   - INSTALLATION.md - Detailed setup
   - PROJECT_STRUCTURE.md - Architecture details
   - backend/README.md - Backend specifics
   - frontend/README.md - Frontend specifics

3. **Customize:**
   - Add more fields to entities
   - Create additional components
   - Implement remaining CRUD operations
   - Add more business logic

4. **Test:**
   - Import sample data
   - Create candidates manually
   - Link applications to jobs
   - Add reviews to candidates

## Key Features Implemented

✅ Authentication (JWT)
✅ Candidate management
✅ Job management
✅ Application management
✅ Company management
✅ Candidate reviews
✅ CSV import
✅ Search functionality
✅ Historical tracking

## Development Tips

### Backend Hot Reload
Spring Boot DevTools is included. Changes to Java files will auto-reload.

### Frontend Hot Reload
Angular CLI watches for changes automatically. Save a file and see changes instantly.

### Database Inspection
```bash
# Connect to database
psql -U cvscreen -d cvscreen

# List tables
\dt

# View candidates
SELECT * FROM candidates;

# View applications with details
SELECT 
  a.id,
  c.first_name || ' ' || c.last_name as candidate,
  j.reference as job,
  a.status
FROM applications a
JOIN candidates c ON a.candidate_id = c.id
LEFT JOIN jobs j ON a.job_id = j.id;
```

### API Testing with Postman
1. Import the endpoints listed in README.md
2. Create environment with:
   - BASE_URL: http://localhost:8081/api
   - TOKEN: (get from login response)
3. Use {{BASE_URL}} and {{TOKEN}} in requests

## Getting Help

- **Backend issues:** Check `backend/README.md`
- **Frontend issues:** Check `frontend/README.md`
- **Database issues:** Check `INSTALLATION.md`
- **Architecture questions:** Check `PROJECT_STRUCTURE.md`

## Default Credentials (Important!)

⚠️ **Change these in production!**

- Username: `admin`
- Password: `admin123`
- Database password: `cvscreen`
- JWT secret: (in application.properties)

## Have Fun Building! 🚀

This is your foundation for a complete IT recruitment management system. The architecture is clean, scalable, and ready for you to extend with additional features.

Happy coding!
