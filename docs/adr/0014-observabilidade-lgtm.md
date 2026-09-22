# ADR-014: Observabilidade com a stack LGTM

- **Status:** Aceito
- **Data:** 2026-09-20

## Contexto

Com validação de ingressos na porta e enriquecimento de CEP via provedor
externo, diagnosticar "por que falhou?" exige correlacionar logs, métricas e
traces. O healthcheck já existia, mas nada era exportado.

## Decisão

- **Aplicação:** `micrometer-registry-prometheus` (exposição em
  `/actuator/prometheus`, público) e `spring-boot-starter-opentelemetry`
  (traces via `management.opentelemetry.tracing.export.otlp.endpoint`),
  amostragem via `TRACING_SAMPLING_PROBABILITY`, e padrão de log com
  `traceId`/`spanId`.
- **Coleta:** um único Grafana Alloy — recebe OTLP (→ Tempo), faz scrape do
  Prometheus da API (→ Mimir via remote write) e lê stdout dos containers
  (→ Loki).
- **Erros:** `InfrastructureException` e filhas mapeiam para **503** (dependência
  quebrada), separadas de erros de domínio (401/404/422) e bugs (500), com
  log de erro no servidor incluindo a mensagem específica.

## Alternativas consideradas

- **ELK (Elasticsearch + Logstash + Kibana):** descartado — pesado para operar (JVM adicional, índices caros); Loki + Mimir + Tempo cobrem logs/métricas/traces com um único Alloy.
- **APM proprietário (Datadog/New Relic):** descartado — custo por host/traço e dados fora da infra; stack LGTM é open-source e roda no Compose.
- **Só Prometheus + Grafana (sem traces/logs):** descartado — métricas sem `traceId` não respondem "por que este check-in falhou?"; OTLP→Tempo + `traceId` no log fecha a correlação.
- **Zipkin/Jaeger standalone:** descartados — mais um backend para operar; Tempo integrado ao Grafana reaproveita o mesmo Alloy e o mesmo datasource.
- **Manter só healthcheck, sem exportar nada:** descartado — era o estado anterior; diagnóstico de falha de CEP/check-in seria grep manual sem correlação.

## Consequências

- **Pró:** correlação trace→log por `traceId` no Grafana; dashboards de
  latência/erro por endpoint sem código extra (auto-instrumentação Boot).
- **Contra:** amostragem `1.0` em dev gera volume alto — reduzir em produção;
  Alloy precisa do socket Docker para ler logs dos containers.
- **Evolução:** alertas no Mimir (ex.: taxa de 503, p99 do check-in),
  dashboards versionados como JSON, exemplar trace→métrica.
