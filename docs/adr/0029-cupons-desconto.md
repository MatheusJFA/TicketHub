# ADR-029: Cupons de desconto por show ou seção

- **Status:** Aceito
- **Data:** 2026-09-24

## Contexto

Preço era só snapshot da seção: sem promoção, aniversário do parceiro ou
last-minute. Desconto precisa refletir no total cobrado, nos tickets e no
webhook — sem bifurcar a precificação.

## Decisão

- **Agregado `Coupon`:** `code` (único, normalizado upper), escopo
  (`showId` XOR `sectionId`), `PERCENT` (1–100) ou `FIXED` (Money),
  janela `validFrom/validUntil` (nula = aberta), `maxUses` (nulo = livre),
  `usedCount`. Criação só `ADMIN` (`POST /coupons`).
- **Aplicação no pedido:** `POST /orders` aceita `couponCode` opcional.
  Desconhecido/expirado/esgotado/fora do escopo/moeda divergente → 422 e
  libera as reservas. Percentual desconta itens elegíveis; fixo rateia
  proporcionalmente (resíduo no último item, teto no subtotal elegível).
  Preços com desconto viram snapshot dos `OrderItem` — total, tickets e
  webhook seguem consistentes.
- **Consumo atômico:** `CouponGateway.claimUse` (`findAndModify` com
  janela + `$expr usedCount<maxUses` server-side + `$inc`) após validar o
  pedido e antes de persistir: concorrência não estoura `maxUses`. Replay
  idempotente retorna antes do claim (sem duplo consumo).

## Consequências

- **Pró:** uma única precificação; cupom inválido nunca segura reserva.
- **Contra:** sem listagem/gestão (Mongo direto na v1); rejeitado ocupa
  código; criação restrita ao master (parceiro pede via backoffice).
- **Evolução:** `GET /coupons`, cupom por parceiro dono do show, limites
  por cliente, campanhas auto-aplicadas.
