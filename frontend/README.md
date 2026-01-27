# CVScreen Frontend

Angular application for IT recruitment management.

## Technology Stack

- Angular 17
- Angular Material
- TypeScript
- RxJS

## Prerequisites

- Node.js 18+ and npm
- Angular CLI 17+

## Installation

```bash
npm install
```

## Development Server

```bash
npm start
```

The application will be available at http://localhost:8082

## Build

```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.

## Running Tests

```bash
npm test
```

## Project Structure

```
src/
├── app/
│   ├── components/      # UI components
│   │   ├── login/
│   │   ├── dashboard/
│   │   ├── candidates/
│   │   ├── jobs/
│   │   ├── applications/
│   │   └── companies/
│   ├── services/        # API services
│   ├── models/          # TypeScript interfaces
│   ├── guards/          # Route guards
│   ├── interceptors/    # HTTP interceptors
│   ├── app.routes.ts    # Application routes
│   └── app.config.ts    # Application configuration
├── environments/        # Environment configurations
├── assets/             # Static assets
└── styles.css          # Global styles
```

## Features

### Authentication
- JWT-based authentication
- Login page with credentials
- Auth guard for protected routes
- Automatic token injection

### Candidates Management
- View all candidates
- Search candidates by name
- View candidate details with application history
- Add reviews/comments to candidates
- Delete candidates

### Jobs Management
- View all jobs
- Search jobs by reference, title, or category
- Create and edit jobs
- Track job status (OPEN, CLOSED, ON_HOLD)

### Applications Management
- View all applications
- Advanced search and filtering
- Create applications (linked or spontaneous)
- Track application status
- Upload CV files

### Companies Management
- View all consultancy companies
- Search companies
- Create and edit companies

## API Integration

The frontend communicates with the backend API at `http://localhost:8081/api`

Authentication is handled via JWT tokens stored in localStorage.

## Styling

The application uses Angular Material with the Indigo-Pink theme.

## Default Credentials

- **Username:** admin
- **Password:** admin123

## Future Enhancements

- Complete CRUD operations for all entities
- CSV import interface
- Advanced filtering and sorting
- Dashboard with statistics
- File download for CVs
- Drag and drop for CV upload
- Notification system
- Dark theme support
