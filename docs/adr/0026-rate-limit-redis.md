# ADR-026: Rate limit distribuído no Redis

- **Status:** Aceito
- **Data:** 2026-09-24

## Contexto

O rate limit era Resilience4j em memória por réplica (`AbstractRateLimitFilter`):
com N réplicas, o orçamento efetivo virava N× o configurado. Flood no
`/auth/**` e no checkout (`POST /orders`, `/pay`) precisa valer no cluster.

## Decisão

- **Porte `RateLimitBudget`** (infra/web) + `RedisRateLimitBudget`: fixed
  window de 60s por `{filtro}:{cliente}` via script Lua atômico
  (`INCR` + `EXPIRE` 70s na primeira contagem). Mesma envelope 429.
- **Fail-open** com warn se o Redis falhar (mesma postura da ADR-025).
- Resilience4j segue para retry/circuit-breaker (ADR-0005); sai só do rate limit.

## Consequências

- **Pró:** orçamento único no cluster; sem estado local por IP (sem vazamento
  de mapa em memória).
- **Contra:** fixed window permite rajada de até 2× no limite da janela;
  aceito — o limite é generoso de propósito (para bots, não compradores).
- **Evolução:** sliding window (ZSET) se a rajada de borda virar problema.
