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

## Alternativas consideradas

- **Spring Retry (`@Retryable` por adapter):** descartado — exigiria anotar cada adapter individualmente e não oferece circuit breaker nativo; a cobertura transversal via aspecto cobre ~40 casos de uso num ponto só.
- **Netflix Hystrix:** descartado — em modo de manutenção/descontinuado; Resilience4j é o sucessor leve e suportado.
- **Retry manual com `try/catch` nos casos de uso:** descartado — poluiria `application` com política de infra, violando o ADR-001 (aplicação framework-free).
- **Service mesh (Istio/Linkerd) para retry/CB:** descartado — desloca resiliência para a malha, mas exige Kubernetes + sidecars fora do escopo do Compose atual; retry de camada 7 não distingue 404/422 de falha transitória sem inspeção de corpo.
- **Fail-fast sem retry nem breaker:** descartado — qualquer soluço de MongoDB/Kafka viraria 500 imediato e picos de venda esgotariam threads.

## Consequências

- **Pró:** um ponto único de resiliência para ~40 casos de uso, sem try/catch por endpoint; controllers chamam casos de uso diretamente.
- **Contra:** retry acontece dentro do aspecto, invisível para quem lê o caso de uso ou o controller (documentado aqui e nos testes `UseCaseMonitoringAspectTest`).
