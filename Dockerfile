# ==========================================
# Multi-stage Dockerfile
# Angular 21 + Spring Boot 4.0.2
# ==========================================

# ==========================================
# Stage 1: Build Angular Frontend
# ==========================================
FROM node:22-alpine AS frontend-builder

WORKDIR /app/frontend

# Copy package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copy frontend source
COPY frontend/ ./

# Build Angular application for production
# FIXED: Use npx to run ng command (Angular CLI not installed globally)
RUN npx ng build --configuration production

# ==========================================
# Stage 2: Build Spring Boot Backend
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder

WORKDIR /app/backend

# Copy pom.xml first (for better layer caching)
COPY backend/pom.xml ./

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy backend source
COPY backend/src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 3: Runtime Image
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install nginx for serving Angular frontend
RUN apk add --no-cache nginx

# Copy built Angular app
COPY --from=frontend-builder /app/frontend/dist/cvscreen-frontend/browser /usr/share/nginx/html

# Copy built Spring Boot jar
COPY --from=backend-builder /app/backend/target/*.jar app.jar

# Copy nginx configuration
COPY docker/nginx.conf /etc/nginx/nginx.conf

# Create directory for CVs
RUN mkdir -p /app/cvs

# Expose ports
EXPOSE 80 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/auth/login || exit 1

# Start both nginx and Spring Boot
CMD nginx && java -jar app.jar