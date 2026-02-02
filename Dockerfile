# CVScreen v0.1 - Multi-stage Dockerfile
# Stage 1: Build Frontend
FROM node:24-alpine AS frontend-builder

WORKDIR /app/frontend

# Copy package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci --silent

# Copy frontend source
COPY frontend/ ./

# Build Angular application for production
RUN npm run build

# Stage 2: Build Backend
FROM maven:3.9-eclipse-temurin-17-alpine AS backend-builder

WORKDIR /app/backend

# Copy pom.xml first for dependency caching
COPY backend/pom.xml ./

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy backend source
COPY backend/src ./src

# Build Spring Boot application
RUN mvn clean package -DskipTests -B

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine

# Install nginx
RUN apk add --no-cache nginx

# Create application directory
WORKDIR /app

# Copy backend JAR from builder
COPY --from=backend-builder /app/backend/target/cvscreen-backend-0.1.jar ./backend.jar

# Copy frontend build from builder
COPY --from=frontend-builder /app/frontend/dist/cvscreen-frontend /usr/share/nginx/html

# Copy nginx configuration
COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY docker/default.conf /etc/nginx/http.d/default.conf

# Create directory for CVs
RUN mkdir -p /app/cvs && chmod 755 /app/cvs

# Create startup script
COPY docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

# Expose port 80 (nginx)
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/api/auth/login || exit 1

# Run startup script
CMD ["/app/startup.sh"]
