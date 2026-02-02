# CVScreen v0.1 - Multi-stage Dockerfile (CORRECTED)
# Stage 1: Build Frontend
FROM node:20-alpine AS frontend-builder

# Augmenter la mémoire disponible
ENV NODE_OPTIONS="--max-old-space-size=4096"

WORKDIR /app/frontend

# Copy package files
COPY frontend/package*.json ./

# Install dependencies (including devDependencies needed for build)
# npm ci requires package-lock.json, so we fall back to npm install if it's not present
# IMPORTANT: Do NOT use --only=production, we need devDependencies to build!
RUN if [ -f package-lock.json ]; then \
        npm ci && npm cache clean --force; \
    else \
        npm install && npm cache clean --force; \
    fi

# Copy frontend source
COPY frontend/ ./

# Build Angular application for production with verbose output
RUN npm run build -- --configuration production --verbose || \
    (echo "BUILD FAILED - Checking for errors..." && \
     npm run build -- --configuration production 2>&1 | tee /tmp/build.log && \
     exit 1)

# Vérifier que le build existe
RUN ls -la dist/cvscreen-frontend/ || (echo "ERROR: Build output not found!" && exit 1)

# Stage 2: Build Backend
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder

WORKDIR /app/backend

# Copy pom.xml first for dependency caching
COPY backend/pom.xml ./

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy backend source
COPY backend/src ./src

# Build Spring Boot application
RUN mvn clean package -DskipTests -B

# Vérifier que le JAR existe
RUN ls -la target/*.jar || (echo "ERROR: JAR not found!" && exit 1)

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine

# Install nginx
RUN apk add --no-cache nginx wget

# Create application directory
WORKDIR /app

# Copy backend JAR from builder
COPY --from=backend-builder /app/backend/target/cvscreen-backend-0.1.jar ./backend.jar

# Copy frontend build from builder
COPY --from=frontend-builder /app/frontend/dist/cvscreen-frontend/browser /usr/share/nginx/html

# Fallback: si le dossier browser n'existe pas, essayer sans
RUN if [ ! -d /usr/share/nginx/html ] || [ -z "$(ls -A /usr/share/nginx/html)" ]; then \
        rm -rf /usr/share/nginx/html && \
        mkdir -p /usr/share/nginx/html; \
    fi

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
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/api/auth/login || exit 1

# Run startup script
CMD ["/app/startup.sh"]