# ADR-015: Pagamento PIX via Mercado Pago

- **Status:** Aceito
- **Data:** 2026-09-22

## Contexto

O checkout (`PayOrderUseCase` + `ConfirmPaymentUseCase`) nasceu sobre um
`MockPaymentGateway` em memória: a cobrança ficava `PENDING` até o webhook
simulado virar para `PAID`. Com a promoção de `Charge` a entidade com state
machine (`PENDING → PAID|FAILED`, terminal e idempotente), era preciso um PSP
real sem tocar domínio ou casos de uso — a porta `PaymentGateway`
(`createCharge`/`findStatus`) já previa essa troca. O mock foi removido; sem
provedor, a aplicação faz fail-fast na subida.

## Decisão

- **PIX via `POST /v1/payments`:** o `qr_code` (copia-e-cola) casa com o
  modelo atual (`Charge.paymentCode`); o id do pagamento vira o `ChargeID`.
  Checkout Pro descartado (retorna `init_point`, URL de redirect, não código
  de pagamento).
- **Sem SDK oficial:** usa o `RestClient` + `BaseHttpClient` existentes (mesmo
  padrão do ViaCEP, ADR-011), sem dependência nova; cobertura sem rede via
  `MockRestServiceServer`.
- **Adapter stateless (`infrastructure/payment/mercadopago/`):**
  `MercadoPagoProperties` (`tickethub.payment.mercadopago.*`: `enabled`,
  `base-url`, `access-token`, `payer-email`, timeouts) + `MercadoPagoClient`
  (cria pagamento PIX, consulta por id; erro vira `HttpUpstreamException`,
  fail-closed) + `MercadoPagoPaymentGateway` (mapeia resposta → `Charge`).
  `MercadoPagoConfiguration` só cria os beans com `access-token` configurado;
  sem token, falha na subida com mensagem pedindo `MERCADOPAGO_ACCESS_TOKEN`
  (o compose repassa a variável).
- **Mapeamento de status:** `approved → PAID`;
  `rejected/cancelled/refunded/charged_back → FAILED`;
  `pending/authorized/in_process/in_mediation → PENDING`; desconhecido falha
  fechado. O `findStatus` reconstrói a cobrança da resposta
  (`external_reference` carrega o orderId, `transaction_amount` o total).
- **Email do pagador:** resolvido do `Customer` via `OrderGateway` +
  `CustomerGateway` (fail-open com fallback para o `payer-email` configurado);
  `X-Idempotency-Key` = orderId evita cobrança dupla no retry.
- **Webhook genérico mantido** (`chargeId` + `status`): a confirmação reconcilia
  com o provedor via `findStatus`, então nenhum formato específico do MP vaza
  para os casos de uso.

## Alternativas consideradas

- **Stripe:** descartado — PIX via Stripe tem disponibilidade/cobertura menor no Brasil frente ao Mercado Pago; `qr_code` do MP casa direto com `Charge.paymentCode`.
- **PagSeguro/PagBank:** descartado — SDK e webhooks com documentação instável; API PIX do MP (`POST /v1/payments`) é direta via `RestClient` sem dependência nova.
- **Adyen/Ebanx:** descartados — enterprise/global com onboarding pesado (contrato, KYB) para o estágio atual.
- **Checkout Pro (redirect `init_point`):** descartado — retorna URL de redirect, não código de pagamento; quebraria o modelo `Charge.paymentCode` (copia-e-cola).
- **SDK oficial `mercadopago-sdk-java`:** descartado — dependência pesada para dois calls (criar + consultar); `BaseHttpClient` + `MockRestServiceServer` já seguem o padrão ViaCEP (ADR-011) sem rede nos testes.
- **Boleto/cartão como primeiro método:** descartados — boleto tem compensação lenta (incompatível com reserva de assento com TTL) e cartão exige PCI/tokenização; PIX liquida em segundos.

## Consequências

- **Pró:** domínio e aplicação intactos; falha de configuração aparece no boot
  com mensagem clara em vez de 503 em runtime; pagamento real de ponta a ponta
  (`pay → PIX → webhook → tickets`).
- **Contra:** boot exige `MERCADOPAGO_ACCESS_TOKEN` (dev local precisa de
  token de teste); `createCharge` lê pedido + cliente (2 leituras extras);
  moeda assumida `BRL` quando ausente; webhook sem verificação de assinatura
  do provedor.
- **Evolução:** adaptador de webhook no formato real do MP (`type` +
  `data.id`), verificação de assinatura do callback, cartão/Checkout Pro como
  segundo método.
