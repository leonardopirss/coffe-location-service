# App Location no Minikube com Prometheus

Este diretório tem manifests para coletar métricas da aplicação Spring Boot e do Minikube no mesmo Prometheus.

## Aplicação local, fora do Kubernetes

Com a aplicação rodando no host em `localhost:8081`, o Prometheus dentro do Minikube coleta:

- `host.minikube.internal:8081/actuator/prometheus`
- métricas dos pods anotados
- métricas dos nodes e cAdvisor do Minikube

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/prometheus.yaml
minikube service prometheus -n app-location
```

No Prometheus, valide `Status > Targets`. O target `app-location-local` deve ficar `UP`.

## Aplicação dentro do Minikube

Suba tudo com o script:

```bash
./scripts/k8s/start-services.sh
```

O script inicia o Minikube, atualiza o contexto do `kubectl`, gera o jar, cria a imagem `app-location-service:local` dentro do Docker do Minikube, aplica os manifests e aguarda os deployments.

Para pular o build em execucoes seguintes:

```bash
SKIP_BUILD=true ./scripts/k8s/start-services.sh
```

Ou faca manualmente:

Compile o jar e crie a imagem dentro do Docker do Minikube:

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.8/libexec/openjdk.jdk/Contents/Home sh ./mvnw clean package -DskipTests
eval $(minikube docker-env)
docker build -t app-location-service:local .
```

Suba banco, app e Prometheus:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/app.yaml
kubectl apply -f k8s/prometheus.yaml
```

Acesse:

```bash
minikube service app-location-service -n app-location
minikube service prometheus -n app-location
```

## Consultas iniciais

Saude da aplicacao:

```promql
up{app="app-location-service"}
```

Requisicoes HTTP por pod:

```promql
sum by (namespace, pod, uri, method, status) (rate(http_server_requests_seconds_count{app="app-location-service"}[5m]))
```

CPU por pod no Minikube:

```promql
sum by (namespace, pod) (rate(container_cpu_usage_seconds_total{namespace="app-location", container!="POD", container!=""}[5m]))
```

Memoria por pod:

```promql
sum by (namespace, pod) (container_memory_working_set_bytes{namespace="app-location", container!="POD", container!=""})
```
