# Observabilidade (stack LGTM)

Métricas, logs e traces fluem por um único coletor (Grafana Alloy) até o
trio **Loki** (logs), **Tempo** (traces) e **Mimir** (métricas), tudo
visualizado no **Grafana**.

```
server --OTLP traces--> alloy --> tempo
server --prometheus --> alloy --> mimir
docker logs ---------> alloy --> loki
grafana --> loki / tempo / mimir
```

## Subir

```bash
docker compose up -d
```

Grafana em http://localhost:3000 (admin / admin-local por padrão,
sobrescreva com `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD`).
Datasources Loki, Tempo e Mimir já vêm provisionados
(`observability/grafana/provisioning`).

## O que a aplicação exporta

- `GET /actuator/prometheus` (público, só métricas): latência e throughput
  HTTP, JVM, Mongo e Kafka via Micrometer. O Alloy faz scrape a cada 15s e
  grava no Mimir.
- Traces OTLP para `OTEL_EXPORTER_OTLP_TRACING_ENDPOINT`
  (padrão `http://alloy:4318/v1/traces` no compose). Amostragem via
  `TRACING_SAMPLING_PROBABILITY` (padrão `1.0` em dev; reduza em produção).
- Logs em console com `traceId`/`spanId` no padrão
  (`logging.pattern.console`), coletados do stdout dos containers pelo Alloy
  para o Loki — dá para saltar do trace (Tempo) para os logs (Loki) pelo
  `traceId`. Cada stream leva os labels `container` e `compose_service`
  (via `discovery.relabel` no Alloy), então filtre por serviço no Grafana
  em vez de um único stream genérico.

## Erros identificáveis

- `DomainException` e filhas (`ResourceNotFoundException`,
  `SpotAlreadyUsedException`, `SpotOwnershipException`,
  `ShowOutsideCheckInDateException`, `AuthenticationException`) → 401/404/422
  com a mensagem específica no corpo.
- `InfrastructureException` e filhas (`HttpUpstreamException`,
  `EventPublishException`, `SpotGenerationException`) → **503**, com log de
  erro no servidor. Falha de dependência (Mongo, Kafka, ViaCEP) aparece como
  503, nunca como erro de validação.
