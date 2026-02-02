#!/bin/sh

echo "=== CVScreen v0.1 Startup ==="
echo "Starting services..."

# Start nginx in background
echo "Starting nginx..."
nginx

# Wait a moment for nginx to start
sleep 2

# Check if nginx started successfully
if ! pgrep nginx > /dev/null; then
    echo "ERROR: nginx failed to start"
    exit 1
fi

echo "✓ nginx started successfully"

# Start Spring Boot application
echo "Starting Spring Boot backend..."
echo "Database: ${DB_HOST:-localhost}:${DB_PORT:-5432}/${DB_NAME:-cvscreen}"

exec java \
    -Djava.security.egd=file:/dev/./urandom \
    -Dserver.port=8081 \
    -Dspring.datasource.url=jdbc:postgresql://${DB_HOST:-localhost}:${DB_PORT:-5432}/${DB_NAME:-cvscreen} \
    -Dspring.datasource.username=${DB_USER:-cvscreen} \
    -Dspring.datasource.password=${DB_PASSWORD:-cvscreen} \
    -jar /app/backend.jar
