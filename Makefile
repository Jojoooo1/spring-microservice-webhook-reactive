SHELL := /bin/bash

help: ## Show this help message.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m\033[0m\n"} /^[$$()% a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

run: ## Start Spring project
	@./mvnw spring-boot:run -Dspring.profiles.active=dev

start: ## Start docker-compose
	docker compose up -d

start-infra: ## Run required infrastructure with docker compose
	$(MAKE) kill start-rabbitmq

start-rabbitmq : ## Run rabbitmq
	@docker compose up -d rabbitmq

kill-rabbitmq : ## Kill rabbitmq
	@docker compose rm -sf rabbitmq
	@docker volume rm -f webhook_rabbitmq

kill: ## Kill and reset project
	@docker compose down
	$(MAKE) kill-rabbitmq

release: ## Create release
	./scripts/create_release.sh

hotfix: ## Create hotfix
	./scripts/create_hotfix.sh