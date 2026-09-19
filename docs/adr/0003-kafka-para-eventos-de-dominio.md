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
- Geração assíncrona de spots: `POST /shows/{id}/sections` com `totalSpots >= 1000`
  persiste a section como casca (sem spots) e publica `SpotsGenerationRequested`
  (chave `showId`, mesma partição do show). O `SpotGenerationListener` materializa
  os spots via `GenerateSectionSpotsUseCase`, que insere em lote com
  `ShowGateway.appendSpots` (bulk insert + `$push` dos ids) em vez de reescrever o
  grafo. O consumidor é idempotente (pula sections completas); falha de publicação
  degrada para geração síncrona no próprio request.

## Consequências

- **Pró:** desacoplamento entre escrita e reações; replay de eventos.
- **Pró:** shows grandes não estouram o request de criação de section; spots grandes seguem num único bulk insert.
- **Contra:** sem atomicidade MongoDB ↔ Kafka hoje (sem outbox transacional — evolução futura).
- **Contra:** sem autenticação/TLS no ambiente local (aceitável só para desenvolvimento).
- **Contra:** consistência eventual dos spots (a section existe antes dos spots); sem DLT — mensagem venenosa retenta até expirar (evolução futura).
