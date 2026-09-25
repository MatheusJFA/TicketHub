# ADR-028: Webhooks de saída para o parceiro (venda/reembolso)

- **Status:** Aceito
- **Data:** 2026-09-24

## Contexto

Venda/reembolso terminavam no Mongo: `OrderPaid`/`OrderRefunded` eram
registrados no agregado mas nunca publicados (`KafkaDomainEventPublisher`
ignorava). O parceiro não tinha como ser avisado.

## Decisão

- **Config:** `Partner.webhookUrl` + `webhookSecret` (nulos por padrão;
  URL http(s), segredo obrigatório com URL). `PUT /partners/{id}/webhook`
  (`partner:write` + dono ou admin).
- **Eventos:** `PublishingSaleRecorder` (infra) decora o `SaleRecorder` e
  descarrega `order.publishDomainEvents` — cobre liquidação (webhook de
  pagamento e reconciliação) e reembolso sem tocar os casos de uso. Publisher
  serializa `OrderPaid`/`OrderRefunded` no tópico existente.
- **Entrega:** `PartnerWebhookListener` em grupo próprio
  (`tickethub-webhooks`; grupo compartilhado dividiria partições e perderia
  mensagens do outro listener) resolve pedido → show → parceiro e POSTa
  `{eventId, type, orderId, showId, total, currency, orderStatus, occurredOn}`
  com `X-Tickethub-Event/Delivery/Signature` (HMAC-SHA256 hex do corpo).
  Retry limitado (Resilience4j, `tickethub.webhooks.*`); falta de dados pula
  com log, falha HTTP após retries estoura para redelivery do Kafka
  (limitado pelo error handler padrão). Sem webhook configurado, pula.
- **Semântica:** at-least-once; `eventId = orderId:type` para dedup no parceiro.

## Consequências

- **Pró:** parceiro reage a venda/reembolso; dinheiro nunca depende do
  webhook (reconciliação é dona da verdade).
- **Contra:** redelivery duplica POST (dedup no parceiro); grupo novo
  reprocessa o tópico no primeiro deploy (dentro da retenção).
- **Evolução:** DLQ + painel de entregas; `order.cancelled`/`ticket.validated`.
