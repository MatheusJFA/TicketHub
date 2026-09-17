# ADR-002: MongoDB como banco principal

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

O modelo é orientado a documentos (shows com seções e assentos, endereços e preços embutidos) e o volume de leitura do catálogo supera o de escrita transacional. Era preciso escolher entre relacional (MySQL/Postgres) e documentos.

## Decisão

MongoDB 8.0 como banco principal, via Spring Data MongoDB (`MongoTemplate`), com:

- Uma coleção por agregado + `audit_logs` (`customers`, `partners`, `shows`, `sections`, `spots`).
- Migrações com Liquibase + extensão MongoDB (coleções e índices versionados em `db/changelog`).
- Índices únicos em `customers.cpf` e `partners.cnpj`; índices de consulta em `shows.partnerId`, `sections.showId`, `spots.sectionId`.

## Consequências

- **Pró:** modelo de leitura simples, sem joins para o catálogo; evolução de schema sem migrações pesadas.
- **Contra (mitigado):** sem joins — ownership `Spot → Section → Show` exigiria múltiplas leituras. Mitigação: `sections` e `spots` denormalizam `showId`/`sectionId`/`partnerId` no momento da persistência via `ShowMongoGateway`, de modo que ownership resolve em 1 leitura indexada (fast path por `partnerId`) com fallback para documentos legados; a leitura do grafo usa 3 queries indexadas (`show` + `sections by showId` + `spots by showId`) em vez de N+1.
- **Contra:** MongoDB standalone não tem transações multi-documento; atomicidade entre coleções exige replica set (fora do escopo atual — ver Unit of Work nos adapters).
