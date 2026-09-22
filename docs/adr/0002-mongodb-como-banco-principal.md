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

## Alternativas consideradas

- **PostgreSQL/MySQL (relacional):** descartado — o catálogo (show + sections + spots) exigiria joins em toda leitura quente e migrações de schema mais rígidas; o modelo é lido como documento agregado.
- **DynamoDB:** descartado — single-table design imporia remodelagem completa e custo/lock-in de nuvem sem ganho para o volume atual; índices locais/secundários complicariam ownership.
- **Cassandra/ScyllaDB:** descartado — otimizado para escrita massiva distribuída, exagero para catálogo com leitura dominante e consultas ad-hoc por `partnerId`/`showId`.
- **SQLite/H2 embarcado:** descartado — serve para protótipo local, mas sem replicação, índices parciais avançados nem paridade com produção.

## Consequências

- **Pró:** modelo de leitura simples, sem joins para o catálogo; evolução de schema sem migrações pesadas.
- **Contra (mitigado):** sem joins — ownership `Spot → Section → Show` exigiria múltiplas leituras. Mitigação: `sections` e `spots` denormalizam `showId`/`sectionId`/`partnerId` no momento da persistência via `ShowMongoGateway`, de modo que ownership resolve em 1 leitura indexada (fast path por `partnerId`) com fallback para documentos legados; a leitura do grafo usa 3 queries indexadas (`show` + `sections by showId` + `spots by showId`) em vez de N+1.
- **Contra:** MongoDB standalone não tem transações multi-documento; atomicidade entre coleções exige replica set (fora do escopo atual — ver Unit of Work nos adapters).
