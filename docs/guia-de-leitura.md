# Guia de leitura do TicketHub

Como navegar no projeto em ordem, do geral ao detalhe, e onde estão os pontos
de interesse. Comece por [`architecture.md`](./architecture.md) (visão com
diagramas) e volte aqui para o passo a passo.

## 1. Mapa rápido

```text
TicketHub/
├── docs/                # architecture.md, este guia e adr/ (decisões)
├── http/                # requisições REST de exemplo (auth, customers, audit-logs...)
├── docker-compose.yml   # backend + MongoDB + Kafka para desenvolvimento
└── server/              # Maven multi-módulo (domain, application, infrastructure)
    ├── domain/          # regras puras, sem framework (entidades, VOs, gateways/portas)
    ├── application/     # casos de uso (~40) retornando Either<Notification, Output>
    └── infrastructure/  # Spring Boot: HTTP, MongoDB, Kafka, segurança, auditoria
```

Regra de dependência: `infrastructure → application → domain`. O `domain`
não conhece framework (verificado por `ArchitectureTest`).

## 2. Roteiro sugerido

1. **Contratos HTTP**: `server/infrastructure/src/main/java/.../api/*API.java`
   (ex.: `SpotAPI.java`) — rotas, OpenAPI e `@PreAuthorize` num só lugar.
2. **Controllers finos**: `.../api/controllers/SpotController.java` — converte
   DTO ↔ comando via MapStruct e traduz `Either → HTTP` com `HttpResults`.
3. **Casos de uso**: `server/application/src/main/java/.../spot/create/DefaultCreateSpotUseCase.java`
   — orquestração sobre os gateways do domínio, sem Spring.
4. **Domínio**: `server/domain/src/main/java/.../core/spot/Spot.java` (+ `Section`,
   `Show`, `Customer`, `Partner`) e os value objects em `.../domain/shared/`.
5. **Persistência**: `.../spot/SpotMongoGateway.java` + `.../spot/persistence/SpotDocument.java`
   (conversão entidade ↔ documento, com carimbo de `createdBy`/`lastModifiedBy`).
6. **Segurança**: `.../configuration/SecurityConfiguration.java`,
   `.../security/Permission.java`, `Role.java`, `ShowAccess.java` (dono via
   `partnerId`), `.../web/CorrelationIdFilter.java` (correlação + ator no MDC).
7. **Auditoria**: `.../audit/` (aspecto, `AuditSanitizer`, `MongoAuditTrail`,
   leitura em `MongoAuditLogReader`, `GET /audit-logs` em `AuditAPI.java`).
8. **Infra externa**: `docker-compose.yml`, `.../configuration/MongoConfiguration.java`,
   `KafkaConfiguration.java`, `db/changelog/` (Liquibase).
9. **Testes**: `mise run test` (unit + arquitetura), `mise run integration`
   (Testcontainers: `*IT`, `*E2ETest`, Cucumber em `features/`).

## 3. Pontos de interesse

| Tema | Onde olhar |
|---|---|
| Fluxo de uma requisição (`POST /spots`) | `architecture.md` §2, `SpotController`, `UseCaseMonitoringAspect`, `ResiliencePolicy` |
| Erros (`Either`, 404/422/503) | `adr/0004-either-notification-erros.md`, `HttpResults.java`, `GlobalExceptionHandler.java` |
| Autorização (RBAC + dono) | `adr/0007-jwt-rbac-ownership.md`, `Permission.java`, `ShowAccess.java`, `ApiAuthorizationTest.java` |
| Login, refresh rotativo, logout | `adr/0010-login-email-refresh-logout.md`, `application/authentication/`, `http/auth.http` |
| Trilha de auditoria | `adr/0006-trilha-de-auditoria.md`, `infrastructure/audit/`, `GET /audit-logs`, `http/audit-logs.http` |
| Resiliência (retry/circuito) | `adr/0005-resilience4j-retry-circuit-breaker.md`, `ResiliencePolicy.java` |
| MongoDB e migrações | `adr/0002-mongodb-como-banco-principal.md`, `MongoConfiguration.java`, `db/changelog/`, `LiquibaseMigrationIT.java` |
| Kafka e eventos | `adr/0003-kafka-para-eventos-de-dominio.md`, `KafkaConfiguration.java`, `SpotGenerationListener.java` |
| Estratégia de testes | `adr/0008-testcontainers-testes-integracao.md`, `adr/0009-cucumber-testes-aceitacao.md`, `ControllerTest.java`, `ContainerSupport.java` |
| Produção | `production-checklist.md` (segredos, tempos, backup, alertas) |
| Enriquecimento de endereço (ZIP code) | `adr/0011-enriquecimento-endereco-cep.md`, `ViaCepLookup.java` |
| Estilo de código | `Optional` em vez de ternário de `null`; `isNull`/`nonNull` (`java.util.Objects`, import estático); `StringUtils`/`CollectionUtils` (Apache Commons, import estático); sem nomes abreviados (`INFRASTRUCTURE_ERROR`, não `INFRA_ERROR`) |

## 4. Comandos úteis

```shell
mise run test         # unit + arquitetura (sem Docker)
mise run integration  # tudo, com MongoDB/Kafka via Testcontainers (exige Docker)
mise run it-only 'SpotE2ETest'  # um IT/E2E específico
docker-compose up -d  # backend + dependências locais
```
