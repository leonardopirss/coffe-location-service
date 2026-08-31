# Postman

Importe os dois arquivos no Postman:

- `app-location-service.postman_collection.json`
- `app-location-service.postman_environment.json`

Selecione o environment `App Location Service - Local`.

## Variaveis principais

- `base_url`: URL da App Location Service. Padrao: `http://localhost:8081`
- `kube_proxy_url`: URL do `kubectl proxy`. Padrao: `http://localhost:8001`
- `kube_node`: nome do node Minikube. Padrao: `minikube`
- `namespace`: namespace Kubernetes. Padrao: `app-location`
- `pod_name`: nome do pod para buscar logs
- `metric_name`: metrica do Actuator para buscar em `/actuator/metrics/{metric_name}`
- `jwt_token`: preenchida automaticamente pelo request `App - Users / Login`

## Para endpoints Kubernetes

Rode o proxy antes de usar a pasta `Kubernetes - kubectl proxy`:

```bash
kubectl proxy
```

Em outro terminal, descubra o nome real do pod e atualize `pod_name` no environment:

```bash
kubectl get pods -n app-location
```
