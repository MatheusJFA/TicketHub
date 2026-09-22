# ADR-019: Reconciliação de pagamentos e auto-refund

- **Status:** Aceito
- **Data:** 2026-09-22

## Contexto

PIX aprovado cujo webhook nunca chegou deixava dinheiro capturado com pedido
`PENDING` expirado: sem ingresso e sem devolução. E aprovação dentro do TTL
com callback atrasado era rejeitada pelo relógio de processamento
(`markAsPaid` usa "agora"), punindo quem pagou em dia.

## Decisão

- **TTL vale para a captura, não para o processamento**
  (`Order.markAsPaidAt(approvedAt, clock)`): aprovação até `expiresAt`
  liquida e emite ingressos mesmo com callback tardio; após, expira.
- **Reconciliação periódica** (`ReconcileOrdersUseCase` + scheduler a cada
  2min, `tickethub.payment.reconcile-interval`) sobre pedidos `PENDING`
  expirados **com** cobrança (sem cobrança é do sweeper): aprovado em dia →
  liquida; aprovado tardio → `paymentGateway.refund` + `EXPIRED` → `REFUNDED`
  (novo status) + assentos liberados; demais → expira. Falha por pedido é
  pulada e retentada na próxima rodada (pedido segue elegível).
- **Reembolso confirma estado** (`POST /v1/payments/{id}/refunds` + releitura;
  nunca confia só no 2xx). Chamada dupla é segura: após o primeiro estorno o
  MP reporta `refunded` (→ `FAILED` local) e o pedido sai da elegibilidade.
- **`Charge.approvedAt`** (transiente, sem migração: cobranças não são
  persistidas); ausente = trata como agora (conservador: reembolsa).
- Scheduler limpa o cache `spots` só quando a rodada mudou disponibilidade.

## Consequências

- **Pró:** dinheiro nunca estranda (liquida ou devolve); webhook perdido vira
  atraso de até 2min, não prejuízo; idempotente por construção.
- **Contra:** `markAsPaidAt` confia no `date_approved` do provedor; janela
  entre estorno no MP e update local reprocessa sem efeito (listas de saída
  evitam duplicar ingresso no mesmo processo, mas crash entre emissão e
  update pode duplicar — mesma característica do webhook atual).
- **Evolução:** endpoint manual de reembolso (`POST /orders/{id}/refund` a
  partir de `PAID`), outbox transacional para emissão de ingressos, alerta
  quando `refunded > 0` numa rodada.
