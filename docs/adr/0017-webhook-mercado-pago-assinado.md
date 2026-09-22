# ADR-017: Webhook do Mercado Pago com assinatura verificada

- **Status:** Aceito
- **Data:** 2026-09-22

## Contexto

O `POST /payments/webhook` genérico confiava em `chargeId` + `status` vindos
do body. Com um `PaymentGateway` real, isso permite forjar liquidação: basta
postar o id de uma cobrança `PENDING` com `status: PAID` para emitir ingressos
sem aprovação do provedor — o caso de uso reconcilia via `findStatus`, mas
aplica o status **do comando**, não o do provedor.

## Decisão

- **Novo endpoint `POST /payments/mercadopago`** (`MercadoPagoWebhookAPI` +
  `MercadoPagoWebhookController`), `permitAll` no Spring Security (o MP não
  autentica) e protegido por HMAC.
- **Verify-then-fetch** (`MercadoPagoWebhookHandler`): valida a assinatura e
  só então lê o status **na API do MP** (`findStatus`); o body do callback
  nunca fornece status. Pagamento desconhecido vira 404 pela convenção
  `"not found"`; tópico diferente de `payment` recebe 200 sem efeito (para o
  MP parar de retentar).
- **Verificação** (`MercadoPagoWebhookVerifier`, spec oficial): manifest
  exato `id:<data.id>;request-id:<x-request-id>;ts:<ts>;` (segmentos ausentes
  omitidos, `data.id` alfanumérico em minúsculas), HMAC-SHA256 em hex com o
  `webhook-secret`, comparação em tempo constante (`MessageDigest.isEqual`) e
  tolerância de `ts` (5min, aceita segundos ou milissegundos). Falha = 401.
  Segredo ausente = fail-closed (tudo 401 com warn no boot).
- **Genérico preservado com kill-switch:** o `/webhook` legado continua para
  dev local, mas `PaymentController` tem
  `@ConditionalOnProperty(tickethub.payment.generic-webhook-enabled)` —
  produção desliga com `GENERIC_WEBHOOK_ENABLED=false`.
- **`notification_url` por pagamento:** `createPixPayment` envia a URL
  pública configurada (`MERCADOPAGO_NOTIFICATION_URL`, omitida se vazia), com
  `X-Idempotency-Key` = orderId.

## Alternativas consideradas

- **Polling de status (sem webhook):** descartado — latência de liquidação viraria intervalo de poll e carga contínua na API do MP; push com verify-then-fetch liquida em segundos.
- **mTLS no callback:** descartado — o MP não oferece client-certificate nesse webhook; HMAC com `webhook-secret` é o mecanismo da spec oficial.
- **Allowlist de IPs do MP:** descartada — faixas mudam sem aviso e IP spoofável não prova autenticidade do corpo; HMAC sobre o manifest prova posse do secret.
- **JWT/bearer no callback:** descartado — o MP não autentica com token nosso; a verificação HMAC-SHA256 em tempo constante cumpre o papel sem cooperação extra.
- **Confiar no status do body (genérico puro):** descartado — era a vulnerabilidade que motivou o ADR (forjar `PAID` emitia ingressos); o body nunca fornece status, só `data.id` para `findStatus`.

## Consequências

- **Pró:** callback forjado não liquida mais nada; redelivery do MP é
  idempotente (re-lê o mesmo pagamento; `ConfirmPayment` já era idempotente
  para pedido `PAID`); vetor HMAC fixo nos testes trava o formato do manifest.
- **Contra:** boot exige `MERCADOPAGO_WEBHOOK_SECRET` para o endpoint real
  funcionar (fail-closed explícito); relógio do servidor precisa estar
  razoavelmente certo (tolerância de 5min).
- **Evolução:** endurecer o caso de uso para ignorar o status do comando e
  usar sempre o do provedor (fecha a classe inteira de forja, inclusive no
  endpoint legado); renovação periódica do secret.
