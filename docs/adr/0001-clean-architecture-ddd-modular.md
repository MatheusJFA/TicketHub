# ADR-001: Clean Architecture + DDD em módulos Maven

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

O TicketHub precisa evoluir (persistência, mensageria, segurança, auditoria) sem que cada nova infraestrutura contamine as regras de negócio. Era preciso uma separação que permitisse trocar MongoDB/Kafka no futuro e testar o domínio sem Spring.

## Decisão

Organizar o backend em três módulos Maven com regra de dependência estrita:

- `domain` — DDD tático puro (agregados, value objects, Notification pattern, ports `*Gateway`). Zero dependência de framework.
- `application` — casos de uso (`UseCase` + `Either`), orquestrando agregados e gateways.
- `infrastructure` — tudo de framework: Spring MVC, MongoDB, Kafka, segurança, resiliência, auditoria.

A direção `infrastructure → application → domain` é verificada por testes ArchUnit (`ArchitectureTest`).

## Consequências

- **Pró:** domínio testável com JUnit puro; trocar MongoDB por outro banco afeta só `infrastructure`.
- **Pró:** ports (`*Gateway`) permitem stubs de fallback (503) enquanto os adapters não existem.
- **Contra:** mais boilerplate (Command/Output por caso de uso) e navegação entre módulos.
