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

## Alternativas consideradas

- **Monólito em camadas clássico (controller → service → repository, módulo único):** descartado — services tendem a acumular regra de negócio misturada com Spring/JPA, dificultando trocar MongoDB/Kafka e testar o domínio sem subir contexto.
- **Pacotes hexagonais em módulo único:** descartado — separa por convenção de pacote, mas nada impede import indevido sem ArchUnit multi-módulo; a barreira de compilação dos três módulos Maven é mais forte.
- **Microsserviços por agregado (customers, shows, orders):** descartado — excesso operacional para o estágio atual (rede, versionamento de contratos, transações distribuídas) sem necessidade de escala independente.
- **Entidades de framework como domínio (Spring Data @Document com lógica):** descartado — acoplaria agregados a anotações e ao ciclo de vida do Mongo, vazando infraestrutura para as regras.

## Consequências

- **Pró:** domínio testável com JUnit puro; trocar MongoDB por outro banco afeta só `infrastructure`.
- **Contra:** mais boilerplate (Command/Output por caso de uso) e navegação entre módulos.
