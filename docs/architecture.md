# TicketHub — Arquitetura

> Stack: Java 25 · Spring Boot 4 · MongoDB 8.0 · Kafka 3.9.1 (KRaft) · Maven multi-módulo.
> Decisões arquiteturais detalhadas em [`adr/`](./adr/).

## 1. Visão geral — módulos e infraestrutura

```mermaid
flowchart TB
    subgraph Clients["Clients"]
        HTTP["http/*.http\ncustomer • partner • show • section • spot • auth"]
    end

    subgraph Server["server (Maven aggregator)"]
        subgraph Infra["infrastructure — Spring Boot"]
            API["Controllers + *API interfaces\n@ControllerTest slices"]
            MAP["mapping (MapStruct)\nrequests/commands • outputs/responses"]
            ASP["UseCaseMonitoringAspect\nresilience + audit (@Around)"]
            SEC["Security\nJWT HS256 • Roles • Permissions • Ownership"]
            RES["ResiliencePolicy\nRetry + CircuitBreaker → 503"]
            AUD["Audit\nMDC correlation/actor → audit_logs"]
            CFG["Config\nMongo • Kafka • Liquibase • OpenAPI"]
            PERS["persistence adapters\nDocuments + Gateways"]
        end
        APP["application\n~40 Use Cases + Either"]
        DOM["domain (puro, sem framework)\nAggregates • VOs • Notification • Gateway ports"]
    end

    subgraph Ext["External — docker-compose"]
        MONGO[("MongoDB 8.0\ncustomers • partners • shows\nsections • spots • audit_logs")]
        KAFKA["Kafka 3.9.1 KRaft\ntickethub.events (3 partições)"]
    end

    HTTP --> API
    API --> MAP
    API --> SEC
    API --> APP
    APP --> DOM
    ASP --> APP
    ASP --> RES
    ASP --> AUD
    AUD --> MONGO
    CFG --> MONGO
    CFG --> KAFKA
    PERS -.->|"implementa"| DOM
    PERS -.->|"usa"| MONGO
```

Regras de dependência (verificadas por ArchUnit em `ArchitectureTest`):

- `infrastructure → application → domain`. `domain` não conhece nenhum framework.
- Gateways (`*Gateway`) são **ports** definidos no `domain` e implementados na `infrastructure`.
- Controllers são finos: contrato/OpenAPI nas interfaces `*API`, conversão via MapStruct, chamadas diretas aos casos de uso com tradução `Either → HTTP` pelo `HttpResults`; resiliência e auditoria via aspecto.

## 2. Fluxo de uma requisição (ex.: `POST /spots`)

```mermaid
sequenceDiagram
    participant C as Client
    participant F as CorrelationIdFilter
    participant S as Spring Security
    participant CT as SpotController
    participant A as UseCaseMonitoringAspect
    participant R as ResiliencePolicy
    participant U as DefaultCreateSpotUseCase
    participant G as SpotGateway
    participant M as MongoDB audit_logs

    C->>F: POST /spots + Bearer JWT
    F->>F: MDC actor = sub do JWT
    F->>S: filter chain
    S->>S: hasAuthority('spot:write')
    S->>CT: autorizado
    CT->>A: execute(command)
    A->>R: decorate (retry 3x + circuit breaker)
    R->>U: execute
    U->>G: create(spot)
    G-->>U: ok
    U-->>A: Right(output)
    A->>M: audit action + SUCCESS + actor
    A-->>CT: output
    CT-->>C: 201 IdResponse
```

Notas do fluxo:

- **Resiliência**: só falha transitória entra no retry/circuito — `Left` cuja causa **não** é `DomainException`. Erro de validação (404/422) passa direto, sem retry.
- **Auditoria**: toda execução gera `AuditEntry` (action, correlationId, actor, input, outcome, error, durationMs). A escrita na trilha é best-effort: nunca quebra a requisição.
- **Segurança**: JWT `sub` vira o ator da auditoria (precedência sobre `X-Actor`).

## 3. Modelo de domínio

```mermaid
flowchart LR
    subgraph Agg["Aggregate Roots"]
        Customer["Customer\ncpf • name"]
        Partner["Partner\ncnpj • name • address"]
        Show["Show\nsections • partnerId"]
    end

    subgraph Ent["Entities (filhas)"]
        Section["Section\nspots • price"]
        Spot["Spot\nlocation"]
    end

    Show --> Section
    Section --> Spot
    Partner -->|createShow| Show

    subgraph VO["Value Objects"]
        Name
        Text
        CPF
        CNPJ
        Address
        Location
        Money
    end

    Agg --> VO
    Ent --> VO

    subgraph Ports["Ports — *Gateway"]
        CG[CustomerGateway]
        PG[PartnerGateway]
        SHG[ShowGateway]
        SEG[SectionGateway]
        SPG[SpotGateway]
    end

    Agg -.->|persistido via| Ports
    Ent -.->|persistido via| Ports
```

Entidades carregam auditoria de domínio (`createdAt`, `updatedAt`, `deletedAt`, `createdBy`, `lastModifiedBy`).

## 4. Autorização

| Recurso | Conta própria | Outro dono | Listagem |
|---|---|---|---|
| Customer | `customer:write`/`delete` + dono | 403 | só `ADMIN` |
| Partner | `partner:write`/`delete` + dono | 403 | só `ADMIN` |
| Show | dono do `partnerId` (`@showAccess`, claim `ownerId`) | 403 | pública (catálogo) |
| Section / Spot | dono via `partnerId` denormalizado (`@showAccess.canWriteSection/canWriteSpot`, fallback `showId`; órfão nega) | 403 | pública (catálogo) |

Cadastro (`POST /customers`, `POST /partners`) e login são públicos.

## 5. Testes

| Camada | Padrão | Onde |
|---|---|---|
| Unit (domain/application) | JUnit + Mockito | `*/src/test` |
| Slices MVC | `@ControllerTest` + JWT real | `infrastructure` |
| Arquitetura | ArchUnit | `ArchitectureTest` |
| Integração | `@IntegrationTest` + Testcontainers | `*IT` (failsafe, tag `integrationTest`) |
| Ponta a ponta | `@E2ETest` + MockMvc + Testcontainers | `*E2ETest` (failsafe, tag `e2eTest`) |

`MongoCleanUpExtension` limpa `audit_logs` antes de cada teste de integração. `mise run test` não exige Docker; `mise run integration` exige.
