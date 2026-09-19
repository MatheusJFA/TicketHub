# ADR-005: Resilience4j com 503

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

Falhas transitórias de MongoDB/Kafka viravam 500 direto, e uma dependência degradada podia esgotar threads do servidor. Picos de venda de ingressos exigem degradação graciosa em vez de espera até timeout.

## Decisão

Resilience4j aplicado pelo `UseCaseMonitoringAspect` (`@Around` em `*UseCase.execute`):

- **Retry** (3 tentativas, 200ms) só para falha transitória: `Left` cuja causa **não** é `DomainException`. 404/422 nunca entram no retry.
- **Circuit breaker** (50% de falha, janela 20, 10s aberto) falha rápido quando a dependência está degradada.
- Circuito aberto vira **503** (`ResponseStatusException`), reaproveitando o contrato OpenAPI. Sem persistência configurada a aplicação falha rápido no boot (sem stubs de fallback).
- Tudo configurável por env (`RESILIENCE_*`); sem política carregada, pass-through.

## Consequências

- **Pró:** um ponto único de resiliência para ~40 casos de uso, sem try/catch por endpoint; controllers chamam casos de uso diretamente.
- **Contra:** retry acontece dentro do aspecto, invisível para quem lê o caso de uso ou o controller (documentado aqui e nos testes `UseCaseMonitoringAspectTest`).
