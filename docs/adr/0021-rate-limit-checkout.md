# ADR-021: Rate limit no checkout

- **Status:** Aceito
- **Data:** 2026-09-23

## Contexto

Só o login tinha flood protection (`AuthRateLimitFilter`). Em on-sale,
`POST /orders` e `POST /orders/{id}/pay` abertos aceitam enxurrada de bots;
leituras (catálogo, seat-map) não precisavam de limite.

## Decisão

- **Base compartilhada** (`AbstractRateLimitFilter`, Resilience4j por
  IP/`X-Forwarded-For`, 429 no envelope padrão): `AuthRateLimitFilter`
  migrado sem mudar comportamento; novo `CheckoutRateLimitFilter` para
  `POST /orders` e `POST /orders/{id}/pay` (cancel e leituras ilimitados).
- **`tickethub.checkout.order-rate-limit-per-minute`** (default 30,
  propositalmente generoso — trava bot, não comprador); E2Es sobem para
  1000 (contexto compartilhado).

## Consequências

- **Pró:** um mecanismo para todos os limites; estoque e PIX protegidos de
  flood.
- **Contra:** limite por IP atrás de NAT/CGNAT divide a cota entre
  compradores (mitigado pelo valor generoso); sem `Retry-After` (igual ao
  filtro de auth).
- **Evolução:** `Retry-After`, limite por usuário autenticado além de IP,
  allowlist de healthchecks.
