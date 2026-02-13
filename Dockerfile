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
COPY --from=backend-builder /app/backend/target/*.jar backend.jar

# Copy nginx configuration FILES (BOTH!)
COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY docker/default.conf /etc/nginx/http.d/default.conf

# Copy startup script
COPY docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

# Create directory for CVs
RUN mkdir -p /app/cvs

# Expose ports
EXPOSE 80 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/auth/login || exit 1

# Use startup script
CMD ["/app/startup.sh"]