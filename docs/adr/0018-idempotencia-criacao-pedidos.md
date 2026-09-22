# ADR-018: Idempotency-Key na criação de pedidos

- **Status:** Aceito
- **Data:** 2026-09-22

## Contexto

Duplo clique ou retry de rede em `POST /orders` abria dois pedidos e travava
assentos duas vezes (até duas cobranças PIX). O `pay` já era idempotente
(pedido com charge retorna a cobrança atual); faltava a criação.

## Decisão

- **Chave no pedido, não tabela separada:** `Order.idempotencyKey`
  (nullable) + `OrderGateway.findByIdempotencyKey` + índice único esparso
  `uq_orders_idempotencyKey` (mesmo padrão do `uq_orders_chargeId`).
- **Lookup-first + corrida via índice:** com chave, o caso de uso retorna o
  pedido original antes de reservar; em corrida concorrente o índice rejeita
  o segundo insert e o código relê o vencedor. Sem chave, comportamento
  anterior (sem dedup). O catch usa `RuntimeException` + releitura de
  propósito: a camada application segue framework-free (ADR-001), sem
  importar `DuplicateKeyException` do Spring.
- **Header `Idempotency-Key` opcional** em `POST /orders`; replay responde
  201 com o pedido original (documentado no OpenAPI).
- **Reserva nunca passa pelo replay:** a releitura só devolve pedido já
  persistido; a decisão de reserva continua atômica no Mongo.

## Alternativas consideradas

- **Tabela separada `idempotency_keys`:** descartada — nova coleção + ciclo de vida (TTL, limpeza) para o que um campo nullable + índice esparso em `orders` já resolve, seguindo o padrão `uq_orders_chargeId`.
- **Chave de idempotência no Redis:** descartada — introduziria dependência de escrita no caminho do checkout (e janelas de divergência Redis↔Mongo); o índice único do Mongo é a própria trava atômica.
- **Token de idempotência no body:** descartado — exige mudar contrato de criação e cada cliente gerar UUID; header `Idempotency-Key` é a convenção (Stripe/MP) e mantém o body intacto.
- **Dedup por (customerId + showId + spots):** descartado — recompra legítima dos mesmos assentos em sessões diferentes seria falsamente deduplicada; chave explícita define a intenção.
- **Sem idempotência (culpar o cliente):** descartado — era o estado anterior; duplo clique/retry abria dois pedidos, travava assentos duas vezes e gerava duas cobranças PIX.

## Consequências

- **Pró:** retry seguro no checkout; corrida resolve para um único pedido;
  chave ausente = zero mudança de comportamento.
- **Contra:** mesma chave com carrinho diferente replays o primeiro pedido
  (semântica padrão: chave define a intenção); índice a mais em `orders`.
- **Evolução:** TTL/expiração de chaves antigas, 200 em replay em vez de 201,
  idempotência no `pay` via chave do MP (`X-Idempotency-Key` já = orderId).
