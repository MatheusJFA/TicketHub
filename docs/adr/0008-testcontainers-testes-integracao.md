# ADR-008: Testcontainers nos testes de integração

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

Os testes `*IT` dependiam do Compose levantado manualmente (`docker-compose up` antes do `mise run integration`), o que quebrava CI e a experiência local. O projeto de referência (FC3 catálogo de vídeos) usa containers autocontidos nos testes.

## Decisão

Seguir o padrão FC3 adaptado para MongoDB/Kafka:

- Meta-anotações `@IntegrationTest` (contexto sem web) e `@E2ETest` (contexto web + MockMvc), com tags `integrationTest`/`e2eTest`.
- Base `ContainerSupport`: MongoDB 8.0 + Kafka 3.9.1 (mesmas imagens do Compose) via Testcontainers, propriedades por `@DynamicPropertySource`. Sem Docker, os testes são ignorados (`disabledWithoutDocker`).
- `MongoCleanUpExtension` (equivalente ao `MySQLCleanUpExtension` do FC3) limpa `audit_logs` antes de cada teste.
- Failsafe roda as tags no perfil `integration`; Surefire as exclui da fase `test`.
- Escopo inicial: conexões (`InfrastructureConnectionIT`), migrações (`LiquibaseMigrationIT`), trilha (`MongoAuditTrailIT`) e fluxo HTTP (`SpotE2ETest`: login, 401/503, auditoria). Gateway tests entram com os adapters de persistência.

Notas de versão: BOM Testcontainers **2.0.5** com artefatos `testcontainers-*` (na 1.21 os nomes são curtos — `mongodb`, `kafka`); docker-java precisa falar API ≥ 1.40 com o daemon atual.

## Consequências

- **Pró:** `mise run integration` funciona só com Docker, sem Compose; paridade de versões com produção.
- **Contra:** primeira execução baixa imagens (~1 GB, Kafka); suíte de integração mais lenta que unitários.
