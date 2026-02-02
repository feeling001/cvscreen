# CVScreen v0.1 - Makefile
.PHONY: help build up down restart logs shell db-shell backup clean

# Variables
APP_NAME=cvscreen
VERSION=0.1
BACKUP_DIR=./backups

help: ## Afficher l'aide
	@echo "CVScreen v$(VERSION) - Commandes disponibles:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Builder l'image Docker
	@echo "Building $(APP_NAME):$(VERSION)..."
	docker compose build

up: ## Démarrer les services
	@echo "Starting services..."
	docker compose up -d
	@echo "✓ Services started"
	@echo "Access the application at: http://localhost"

down: ## Arrêter les services
	@echo "Stopping services..."
	docker compose down
	@echo "✓ Services stopped"

restart: ## Redémarrer les services
	@echo "Restarting services..."
	docker compose restart
	@echo "✓ Services restarted"

logs: ## Voir les logs (Ctrl+C pour quitter)
	docker compose logs -f

logs-app: ## Voir les logs de l'application uniquement
	docker compose logs -f app

logs-db: ## Voir les logs de la base de données
	docker compose logs -f database

status: ## Voir l'état des services
	docker compose ps

shell: ## Ouvrir un shell dans le conteneur app
	docker compose exec app sh

db-shell: ## Ouvrir un shell PostgreSQL
	docker compose exec database psql -U cvscreen -d cvscreen

backup: ## Créer un backup de la base de données
	@mkdir -p $(BACKUP_DIR)
	@echo "Creating backup..."
	@docker compose exec -T database pg_dump -U cvscreen cvscreen > $(BACKUP_DIR)/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "✓ Backup created in $(BACKUP_DIR)"

restore: ## Restaurer un backup (usage: make restore FILE=backup.sql)
	@if [ -z "$(FILE)" ]; then \
		echo "Error: Please specify FILE=backup.sql"; \
		exit 1; \
	fi
	@echo "Restoring backup from $(FILE)..."
	@docker compose exec -T database psql -U cvscreen cvscreen < $(FILE)
	@echo "✓ Backup restored"

clean: ## Nettoyer les images et volumes inutilisés
	@echo "Cleaning up..."
	docker system prune -f
	@echo "✓ Cleanup complete"

clean-all: ## Nettoyer tout (⚠️  ATTENTION: supprime les données)
	@echo "⚠️  WARNING: This will delete all data!"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker compose down -v; \
		docker system prune -a -f; \
		echo "✓ Full cleanup complete"; \
	fi

rebuild: ## Rebuild complet sans cache
	@echo "Rebuilding from scratch..."
	docker compose build --no-cache
	@echo "✓ Rebuild complete"

dev: ## Démarrer en mode développement avec logs
	docker compose up --build

prod: ## Démarrer en mode production
	docker compose up -d --build
	@echo "✓ Production deployment complete"
	@echo "Access the application at: http://localhost"

health: ## Vérifier la santé des services
	@echo "Checking service health..."
	@docker compose ps
	@echo ""
	@curl -s -o /dev/null -w "App Health: %{http_code}\n" http://localhost/api/auth/login || echo "App: Not responding"

install: ## Installation initiale complète
	@echo "=== CVScreen Installation ==="
	@echo "1. Building images..."
	@make build
	@echo "2. Starting services..."
	@make up
	@echo "3. Waiting for services to be ready..."
	@sleep 10
	@echo "4. Checking health..."
	@make health
	@echo ""
	@echo "✓ Installation complete!"
	@echo "Access the application at: http://localhost"
	@echo "Default credentials: admin / admin123"

update: ## Mettre à jour l'application
	@echo "Updating application..."
	@git pull
	@make down
	@make build
	@make up
	@echo "✓ Update complete"

# Commandes avancées
db-backup-all: ## Backup base de données + CVs
	@mkdir -p $(BACKUP_DIR)
	@echo "Creating full backup..."
	@docker compose exec -T database pg_dump -U cvscreen cvscreen > $(BACKUP_DIR)/db_backup_$$(date +%Y%m%d_%H%M%S).sql
	@docker cp cvscreen-app:/app/cvs $(BACKUP_DIR)/cvs_backup_$$(date +%Y%m%d_%H%M%S)
	@echo "✓ Full backup created"

monitor: ## Afficher l'utilisation des ressources
	docker stats cvscreen-app cvscreen-db

nginx-reload: ## Recharger la configuration nginx
	docker compose exec app nginx -s reload
	@echo "✓ Nginx configuration reloaded"

nginx-test: ## Tester la configuration nginx
	docker compose exec app nginx -t
