# Architecture Decision Records

Decisões arquiteturais do TicketHub, em ordem cronológica. Convenção: `NNNN-titulo-em-kebab-case.md`.

| ADR | Título | Status |
|---|---|---|
| [001](./0001-clean-architecture-ddd-modular.md) | Clean Architecture + DDD em módulos Maven | Aceito |
| [002](./0002-mongodb-como-banco-principal.md) | MongoDB como banco principal | Aceito |
| [003](./0003-kafka-para-eventos-de-dominio.md) | Kafka para eventos de domínio | Aceito |
| [004](./0004-either-notification-erros.md) | `Either<Notification, Output>` e envelope de erros | Aceito |
| [005](./0005-resilience4j-retry-circuit-breaker.md) | Resilience4j com 503 | Aceito |
| [006](./0006-trilha-de-auditoria.md) | Trilha de auditoria (MDC + `audit_logs`) | Aceito |
| [007](./0007-jwt-rbac-ownership.md) | JWT próprio + RBAC com permissões + ownership | Aceito |
| [008](./0008-testcontainers-testes-integracao.md) | Testcontainers nos testes de integração | Aceito |
| [009](./0009-cucumber-testes-aceitacao.md) | Testes de aceitação com Cucumber | Aceito |
| [010](./0010-login-email-refresh-logout.md) | Login por email + refresh rotativo com revogação | Aceito |
| [011](./0011-enriquecimento-endereco-cep.md) | Enriquecimento de endereço via CEP (ViaCEP) | Aceito |
| [012](./0012-validacao-ingresso-qrcode.md) | Validação de ingresso via QR code | Aceito |
| [013](./0013-cep-lookup-dedicado.md) | CEP lookup dedicado | Aceito |
| [014](./0014-observabilidade-lgtm.md) | Observabilidade LGTM | Aceito |
| [015](./0015-pagamento-pix-mercado-pago.md) | Pagamento PIX via Mercado Pago | Aceito |
| [016](./0016-cache-redis-leituras.md) | Cache compartilhado de leitura em Redis | Aceito |
| [017](./0017-webhook-mercado-pago-assinado.md) | Webhook do Mercado Pago com assinatura verificada | Aceito |
