#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
NAMESPACE="${NAMESPACE:-app-location}"
IMAGE_NAME="${IMAGE_NAME:-app-location-service:local}"
SKIP_BUILD="${SKIP_BUILD:-false}"
SKIP_TESTS="${SKIP_TESTS:-true}"

log() {
  printf "\n[%s] %s\n" "$(date '+%H:%M:%S')" "$*"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf "Erro: comando obrigatorio nao encontrado: %s\n" "$1" >&2
    exit 1
  fi
}

require_command minikube
require_command kubectl
require_command docker

cd "$ROOT_DIR"

log "Iniciando Minikube"
minikube start

log "Atualizando contexto do kubectl"
minikube update-context

if [ "$SKIP_BUILD" != "true" ]; then
  if [ ! -x ./mvnw ]; then
    printf "Erro: ./mvnw nao encontrado ou sem permissao de execucao.\n" >&2
    printf "Tente: chmod +x mvnw\n" >&2
    exit 1
  fi

  log "Gerando jar da aplicacao"
  if [ "$SKIP_TESTS" = "true" ]; then
    ./mvnw clean package -DskipTests
  else
    ./mvnw clean package
  fi

  log "Gerando imagem Docker dentro do ambiente do Minikube: $IMAGE_NAME"
  eval "$(minikube docker-env)"
  docker build -t "$IMAGE_NAME" .
fi

log "Aplicando manifests Kubernetes"
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/app.yaml
kubectl apply -f k8s/prometheus.yaml

log "Aguardando deployments ficarem prontos"
kubectl rollout status "deployment/app-location-db" -n "$NAMESPACE" --timeout=180s
kubectl rollout status "deployment/app-location-service" -n "$NAMESPACE" --timeout=180s
kubectl rollout status "deployment/prometheus" -n "$NAMESPACE" --timeout=180s

log "Aguardando pods ficarem Ready"
kubectl wait --for=condition=Ready pod -l app=app-location-db -n "$NAMESPACE" --timeout=180s
kubectl wait --for=condition=Ready pod -l app=app-location-service -n "$NAMESPACE" --timeout=180s
kubectl wait --for=condition=Ready pod -l app=prometheus -n "$NAMESPACE" --timeout=180s

log "Estado atual"
kubectl get pods,svc -n "$NAMESPACE"

cat <<EOF

Servicos prontos.

Para acessar a aplicacao:
  kubectl port-forward -n $NAMESPACE service/app-location-service 8081:8081
  http://127.0.0.1:8081/actuator/health

Para acessar o Prometheus:
  kubectl port-forward -n $NAMESPACE service/prometheus 9090:9090
  http://127.0.0.1:9090

Para pular build/imagem em uma proxima execucao:
  SKIP_BUILD=true ./scripts/k8s/start-services.sh

Para rodar testes antes de empacotar:
  SKIP_TESTS=false ./scripts/k8s/start-services.sh
EOF
