# ADR-016: Cache compartilhado de leitura em Redis

- **Status:** Aceito
- **Data:** 2026-09-22

## Contexto

Toda leitura de catálogo (`GET /shows`, `/sections`, `/spots` + detalhes)
batia no MongoDB a cada request. A disponibilidade de spots muda a cada compra
(`reserveIfAvailable` atômico em `CreateOrderUseCase`), então cachear sem
estratégia de invalidação mostraria assento ocupado como livre — e a decisão
de reserva nunca pode sair do cache.

## Decisão

- **Redis compartilhado via Spring Cache** (`spring-boot-starter-data-redis`,
  Lettuce; serviço `redis` no compose; Testcontainers nos ITs). Memcached
  descartado (sem integração nativa com Spring Cache).
- **3 caches com TTL por volatilidade** (`tickethub.cache.*`):
  `shows` 5min, `sections` 5min, `spots` 15s; `enabled` permite desligar.
- **Anotações nos controllers** (a aplicação segue framework-free, ADR-001):
  `@Cacheable` em `getById`/`list`, `@CacheEvict(allEntries=true)` em toda
  escrita do agregado. Compra (`createOrder`/`payOrder`), cancelamento e
  webhook de pagamento evictam `spots`; escritas de catálogo evictam o
  próprio agregado.
- **Evict amplo de propósito:** as chaves de `list` variam por
  `(search, page, perPage, sort, direction)` e não são enumeráveis a partir
  de um write/compra — `allEntries` é a única invalidação correta. O custo é
  baixo: escrita de catálogo é rara e o TTL curto reconstrói o cache em
  segundos.
- **Compra nunca lê cache:** o fluxo de reserva vai direto ao Mongo; o cache
  cobre só leitura HTTP, então stale exibe dado velho por segundos sem risco
  de overbooking.
- **Serialização JSON com type info** (`JavaTimeModule` + default typing):
  os DTOs são records com `Instant`, que o serializer padrão do Spring Data
  Redis não suporta — sem isso, a primeira escrita no cache quebra.

## Alternativas consideradas

- **Caffeine local (in-process):** descartado — cada réplica teria visão própria; invalidação (`@CacheEvict`) não atravessa instâncias e o stale divergiria entre pods.
- **Memcached:** descartado — sem integração nativa com Spring Cache; exigiria adapter manual para o que `spring-boot-starter-data-redis` entrega pronto.
- **Hazelcast/Infinispan (grid embarcado):** descartados — protocolo de cluster próprio e operação mais pesada; Redis no Compose + Testcontainers já cobre compartilhamento com TTL.
- **Read replicas do MongoDB:** descartadas — aliviam primário, mas sem TTL/invalidação por agregado; spots continuariam batendo no banco a cada request.
- **Sem cache (só índices Mongo):** descartado — era o estado anterior; todo `GET` de catálogo pagava round-trip ao Mongo no caminho quente.

## Consequências

- **Pró:** leitura de catálogo sai do Mongo no caminho quente; invalidação
  simples e obviamente correta; cobertura sem Redis via teste de
  serialização + `ApplicationContextRunner`.
- **Contra:** Redis vira dependência de boot (compose cobre; fora dele,
  `CACHE_ENABLED=false`); geração async de spots via Kafka fica stale até o
  TTL de 15s; cada write/compra zera o hit rate do agregado.
- **Evolução:** evict programático por chave se o TTL crescer, métricas de
  hit/miss no Mimir, lock distribuído se algum dia cachear escrita.
