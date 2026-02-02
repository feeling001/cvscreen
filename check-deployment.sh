#!/bin/bash

# CVScreen v0.1 - Pre-deployment Check Script
# Ce script vérifie que tout est prêt pour le déploiement Docker

set -e

echo "==================================="
echo "CVScreen v0.1 - Pre-deployment Check"
echo "==================================="
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Compteurs
ERRORS=0
WARNINGS=0

check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 is installed"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is NOT installed"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1 exists"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is MISSING"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1 exists"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is MISSING"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

# 1. Vérifier les commandes requises
echo "1. Checking required commands..."
check_command docker

# Vérifier Docker Compose v2 (intégré à Docker)
if docker compose version &> /dev/null; then
    echo -e "${GREEN}✓${NC} docker compose (v2) is available"
    COMPOSE_VERSION=$(docker compose version --short)
    echo "   Docker Compose version: $COMPOSE_VERSION"
else
    echo -e "${RED}✗${NC} docker compose (v2) is NOT available"
    echo "   Please install Docker 24.0+ with integrated Compose v2"
    ERRORS=$((ERRORS + 1))
fi

DOCKER_VERSION=$(docker --version | grep -oE '[0-9]+\.[0-9]+')
echo "   Docker version: $DOCKER_VERSION"

echo ""

# 2. Vérifier la structure des fichiers
echo "2. Checking project structure..."
check_file "Dockerfile"
check_file "docker-compose.yml"
check_file ".dockerignore"
check_dir "docker"
check_file "docker/nginx.conf"
check_file "docker/default.conf"
check_file "docker/startup.sh"
check_dir "frontend"
check_file "frontend/package.json"
check_dir "backend"
check_file "backend/pom.xml"

echo ""

# 3. Vérifier les permissions
echo "3. Checking file permissions..."
if [ -x "docker/startup.sh" ]; then
    echo -e "${GREEN}✓${NC} docker/startup.sh is executable"
else
    echo -e "${YELLOW}⚠${NC} docker/startup.sh is not executable (will be fixed during build)"
    WARNINGS=$((WARNINGS + 1))
fi

echo ""

# 4. Vérifier l'espace disque
echo "4. Checking disk space..."
AVAILABLE_SPACE=$(df -BG . | tail -1 | awk '{print $4}' | sed 's/G//')
if [ "$AVAILABLE_SPACE" -gt 5 ]; then
    echo -e "${GREEN}✓${NC} Sufficient disk space: ${AVAILABLE_SPACE}GB available"
else
    echo -e "${YELLOW}⚠${NC} Low disk space: only ${AVAILABLE_SPACE}GB available (5GB recommended)"
    WARNINGS=$((WARNINGS + 1))
fi

echo ""

# 5. Vérifier les ports
echo "5. Checking port availability..."
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠${NC} Port $1 is already in use"
        WARNINGS=$((WARNINGS + 1))
        lsof -Pi :$1 -sTCP:LISTEN | grep LISTEN
    else
        echo -e "${GREEN}✓${NC} Port $1 is available"
    fi
}

check_port 80
check_port 5432

echo ""

# 6. Vérifier la configuration Docker
echo "6. Checking Docker configuration..."
if docker info >/dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Docker daemon is running"
    
    # Vérifier les ressources
    MEMORY=$(docker info --format '{{.MemTotal}}' | awk '{print int($1/1024/1024/1024)}')
    if [ "$MEMORY" -ge 2 ]; then
        echo -e "${GREEN}✓${NC} Docker has ${MEMORY}GB RAM allocated"
    else
        echo -e "${YELLOW}⚠${NC} Docker has only ${MEMORY}GB RAM allocated (2GB recommended)"
        WARNINGS=$((WARNINGS + 1))
    fi
else
    echo -e "${RED}✗${NC} Docker daemon is NOT running"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# 7. Vérifier les fichiers de configuration sensibles
echo "7. Checking for sensitive data..."
if grep -r "admin123" docker-compose.yml >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠${NC} Default password detected in docker-compose.yml (should be changed for production)"
    WARNINGS=$((WARNINGS + 1))
fi

if [ -f ".env" ]; then
    echo -e "${GREEN}✓${NC} .env file found (good for production)"
else
    echo -e "${YELLOW}⚠${NC} No .env file found (recommended for production)"
    WARNINGS=$((WARNINGS + 1))
fi

echo ""

# 8. Test de build (optionnel)
echo "8. Testing Docker build (optional)..."
read -p "Do you want to test the Docker build? This may take 5-10 minutes. [y/N] " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Starting build test..."
    if docker compose build --no-cache 2>&1 | tee /tmp/docker-build.log; then
        echo -e "${GREEN}✓${NC} Docker build successful"
    else
        echo -e "${RED}✗${NC} Docker build failed. Check /tmp/docker-build.log"
        ERRORS=$((ERRORS + 1))
    fi
fi

echo ""
echo "==================================="
echo "Check Summary"
echo "==================================="
echo -e "Errors: ${RED}${ERRORS}${NC}"
echo -e "Warnings: ${YELLOW}${WARNINGS}${NC}"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo ""
    echo "You can now deploy with:"
    echo "  docker compose up -d --build"
    echo "or"
    echo "  make install"
    exit 0
else
    echo -e "${RED}✗ Some checks failed. Please fix the errors before deploying.${NC}"
    exit 1
fi
