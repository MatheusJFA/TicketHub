# ADR-003: Kafka para eventos de domínio

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

Publicação de shows, venda de ingressos e auditoria precisam notificar outros componentes de forma assíncrona e desacoplada (ex.: reserva de assento → emissão de ingresso → notificação).

## Decisão

Apache Kafka 3.9.1 em modo KRaft (broker único, sem ZooKeeper), tópico `tickethub.events` (3 partições, 1 réplica), com:

- Produtor idempotente (`acks=all`, `enable.idempotence=true`).
- Consumidor com commit manual (`enable-auto-commit=false`, `ack-mode: record`).
- Criação do tópico via `KafkaAdmin` na inicialização (`fail-fast`).

## Consequências

- **Pró:** desacoplamento entre escrita e reações; replay de eventos.
- **Contra:** sem atomicidade MongoDB ↔ Kafka hoje (sem outbox transacional — evolução futura).
- **Contra:** sem autenticação/TLS no ambiente local (aceitável só para desenvolvimento).
