# ADR-005: Resilience4j com fallback 503

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

Falhas transitórias de MongoDB/Kafka viravam 500 direto, e uma dependência degradada podia esgotar threads do servidor. Picos de venda de ingressos exigem degradação graciosa em vez de espera até timeout.

## Decisão

Resilience4j no `ApiSupport` (mesmo choke point da ADR-004):

- **Retry** (3 tentativas, 200ms) só para falha transitória: `Left` cuja causa **não** é `DomainException`. 404/422 nunca entram no retry.
- **Circuit breaker** (50% de falha, janela 20, 10s aberto) falha rápido quando a dependência está degradada.
- Circuito aberto e dependência ausente viram **503** (`ResponseStatusException`), reaproveitando o contrato OpenAPI e o `PersistenceFallbackConfiguration`.
- Tudo configurável por env (`RESILIENCE_*`); sem configuração carregada (ex.: slices de teste), pass-through.

## Consequências

- **Pró:** um ponto único de resiliência para ~40 casos de uso, sem AOP nem anotação por endpoint.
- **Contra:** retry acontece dentro do `ApiSupport`, invisível para quem lê o caso de uso (documentado aqui e nos testes `ApiSupportResilienceTest`).
