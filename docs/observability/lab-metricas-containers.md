# Laboratorio: dataset de metricas dos containers no Minikube

Objetivo: coletar um dataset simples de metricas de Kubernetes, containers e aplicacao para aprender a observar saude, carga e sinais de anomalia.

Este laboratorio usa o que o projeto ja tem:

- Spring Boot Actuator em `/actuator/prometheus`
- Prometheus dentro do namespace `app-location`
- metricas do node via kubelet
- metricas de containers via cAdvisor

## 0. Conceitos minimos

Pense em quatro camadas:

1. Aplicacao: requisicoes HTTP, latencia, erros, JVM, conexao com banco.
2. Container/pod: CPU, memoria, restarts, readiness/liveness.
3. Node: capacidade de CPU/memoria/disco e pressao do kubelet.
4. Orquestrador: deployments, replicas, eventos e status dos pods.

Para anomalia, o dataset precisa responder perguntas como:

- O servico esta UP?
- A taxa de erro HTTP aumentou?
- A latencia p95 subiu?
- CPU ou memoria cresceram fora do normal?
- Algum container reiniciou?
- Algum pod ficou sem readiness?

## 1. Subir o ambiente

```bash
minikube start
minikube update-context

kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/app.yaml
kubectl apply -f k8s/prometheus.yaml

kubectl get all -n app-location
```

Resultado esperado:

```text
app-location-db        1/1 Running
app-location-service   1/1 Running
prometheus             1/1 Running
```

## 2. Abrir Prometheus

Use port-forward, porque no macOS com Docker driver o NodePort pode nao responder direto pelo IP do Minikube.

```bash
kubectl port-forward -n app-location service/prometheus 9090:9090
```

Abra:

```text
http://127.0.0.1:9090
```

Em outro terminal, valide:

```bash
curl http://127.0.0.1:9090/-/healthy
```

## 3. Conferir targets

No Prometheus, acesse:

```text
Status > Targets
```

Procure estes jobs:

- `kubernetes-pods`: coleta `/actuator/prometheus` da aplicacao.
- `kubernetes-nodes`: coleta metricas do kubelet.
- `kubernetes-cadvisor`: coleta metricas de containers.
- `app-location-local`: opcional, para quando a aplicacao roda fora do cluster.

Se `app-location-local` estiver DOWN enquanto a aplicacao roda dentro do Kubernetes, isso nao bloqueia o laboratorio.

## 4. Primeiras consultas PromQL

Saude da aplicacao:

```promql
up{app="app-location-service"}
```

CPU por pod:

```promql
sum by (namespace, pod) (
  rate(container_cpu_usage_seconds_total{namespace="app-location", pod!=""}[2m])
)
```

Memoria por pod:

```promql
sum by (namespace, pod) (
  container_memory_working_set_bytes{namespace="app-location", pod!=""}
)
```

Requisicoes HTTP por status:

```promql
sum by (app, uri, method, status) (
  rate(http_server_requests_seconds_count{app="app-location-service"}[2m])
)
```

Latencia p95 por rota:

```promql
histogram_quantile(
  0.95,
  sum by (le, app, uri, method) (
    rate(http_server_requests_seconds_bucket{app="app-location-service"}[2m])
  )
)
```

Memoria JVM:

```promql
sum by (app, area, id) (
  jvm_memory_used_bytes{app="app-location-service"}
)
```

## 5. Gerar trafego

Abra a aplicacao:

```bash
kubectl port-forward -n app-location service/app-location-service 8081:8081
```

Em outro terminal, rode por 2 minutos:

```bash
while true; do
  curl -s "http://127.0.0.1:8081/actuator/health" > /dev/null
  curl -s "http://127.0.0.1:8081/api/list?page=0&size=10" > /dev/null
  curl -s "http://127.0.0.1:8081/api/closest/coffe?latitude=-23.55&longitude=-46.63" > /dev/null
  sleep 1
done
```

Agora volte no Prometheus e veja se as series de HTTP mudaram.

## 6. Exportar dataset para CSV

Com o port-forward do Prometheus ativo:

```bash
python3 scripts/metrics/export_prometheus_range.py \
  --query-name pod_cpu_seconds_rate \
  --window 30m \
  --step 15s \
  --output datasets/metrics/pod_cpu.csv
```

Outros datasets:

```bash
python3 scripts/metrics/export_prometheus_range.py \
  --query-name pod_memory_working_set_bytes \
  --window 30m \
  --step 15s \
  --output datasets/metrics/pod_memory.csv

python3 scripts/metrics/export_prometheus_range.py \
  --query-name app_http_requests_rate \
  --window 30m \
  --step 15s \
  --output datasets/metrics/http_rate.csv

python3 scripts/metrics/export_prometheus_range.py \
  --query-name app_http_latency_p95_seconds \
  --window 30m \
  --step 15s \
  --output datasets/metrics/http_latency_p95.csv

python3 scripts/metrics/export_prometheus_range.py \
  --query-name app_jvm_memory_used_bytes \
  --window 30m \
  --step 15s \
  --output datasets/metrics/jvm_memory.csv

python3 scripts/metrics/export_prometheus_range.py \
  --query-name target_up \
  --window 30m \
  --step 15s \
  --output datasets/metrics/target_up.csv
```

Formato do CSV:

```text
query_name,timestamp_unix,timestamp_iso,value,series,labels_json
```

Cada linha e uma medicao de uma serie temporal.

## 7. Como interpretar anomalias

Use regras simples antes de pensar em machine learning:

- `up == 0`: Prometheus nao conseguiu coletar o alvo.
- pod com `Ready=false`: aplicacao pode estar viva, mas nao pronta para receber trafego.
- aumento em `http_server_requests_seconds_count{status=~"5.."}`: erro de servidor.
- aumento persistente no p95: degradacao de latencia.
- CPU alta com latencia alta: possivel saturacao de CPU.
- memoria crescendo sem voltar: possivel vazamento ou cache sem limite.
- restarts subindo em `kubectl get pods -n app-location`: crash, OOM ou liveness matando container.

Observacao: restart count como metrica Prometheus normalmente vem de `kube-state-metrics`, que ainda nao esta instalado neste laboratorio. Por enquanto, olhe restarts com `kubectl`; depois ele pode virar mais uma fonte do dataset.

## 8. Exercicios guiados

### Exercicio A: baseline

1. Deixe o ambiente sem trafego por 5 minutos.
2. Exporte CPU, memoria, HTTP rate e latencia.
3. Esse e o comportamento normal parado.

### Exercicio B: carga leve

1. Rode o loop de trafego por 5 minutos.
2. Exporte os mesmos CSVs.
3. Compare com o baseline.

Perguntas:

- A CPU subiu em qual pod?
- A memoria ficou estavel?
- A latencia p95 mudou?
- As rotas aparecem separadas por `uri`?

### Exercicio C: erro controlado

Faça chamadas invalidas:

```bash
while true; do
  curl -s -o /dev/null -w "%{http_code}\n" "http://127.0.0.1:8081/api/list?page=x&size=10"
  sleep 1
done
```

Depois consulte:

```promql
sum by (uri, method, status) (
  rate(http_server_requests_seconds_count{app="app-location-service"}[2m])
)
```

Pergunta: o status de erro ficou visivel na serie?

### Exercicio D: restart

Reinicie a aplicacao:

```bash
kubectl rollout restart deployment/app-location-service -n app-location
```

Observe:

```bash
kubectl get pods -n app-location -w
```

Perguntas:

- O Prometheus registrou janela com `up == 0`?
- A latencia mudou durante a inicializacao?
- O contador de restarts mudou?

## 9. Proximo passo

Depois de entender os CSVs separados, o proximo passo e criar um dataset unico por timestamp contendo:

- `pod_cpu_seconds_rate`
- `pod_memory_working_set_bytes`
- `http_requests_rate`
- `http_latency_p95_seconds`
- `jvm_memory_used_bytes`
- `target_up`

Com isso voce consegue treinar regras simples de anomalia, por exemplo:

- valor atual maior que media movel + 3 desvios
- p95 acima de um limite fixo
- erro HTTP 5xx maior que zero por mais de N minutos
- CPU alta + latencia alta no mesmo intervalo
