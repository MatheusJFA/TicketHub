# ADR-022: Email de confirmação com ingressos

- **Status:** Aceito
- **Data:** 2026-09-23

## Contexto

O ingresso era emitido em silêncio: o comprador via o pedido `PAID`, mas
nunca recebia os códigos. O email real do `Customer` já era resolvido no
gateway de pagamento (ADR-015).

## Decisão

- **`OrderConfirmationMailer`** (fail-open: falha loga, confirmação segue):
  pedido → email do customer → `TicketGateway.findByOrderId` (método novo;
  índice `idx_tickets_orderId` já existia) → texto com um código por
  ingresso. Chamado pelos dois webhooks após `PAID`.
- **SMTP via `spring.mail.*`** (default `localhost:1025`); Mailpit no
  compose (1025 + UI 8025); `MAIL_FROM` configurável.
- **`management.health.mail.enabled: false`:** o starter-mail registra um
  health indicator que derrubava readiness sem SMTP — email best-effort não
  pode afundar saúde (achado em smoke local).

## Consequências

- **Pró:** comprador recebe os códigos sem depender do app; sem nova
  dependência além do starter.
- **Contra:** texto puro (sem HTML/QR em anexo); sem retry de envio;
  sem rastro de "email enviado" (só log).
- **Evolução:** template HTML + QR em anexo, fila/retry via Kafka,
  endpoint de reenvio.
