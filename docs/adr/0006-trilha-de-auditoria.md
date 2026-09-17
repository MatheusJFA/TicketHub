# ADR-006: Trilha de auditoria (MDC + `audit_logs`)

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

Operações financeiras (venda, republicação, reembolso futuro) exigem saber quem fez o quê, quando e com que resultado — para suporte, conciliação e investigação.

## Decisão

Auditoria em três camadas, sem tocar nos casos de uso:

1. **Contexto:** `CorrelationIdFilter` propaga `X-Correlation-ID` (gera UUID se ausente) e resolve o ator — principal autenticado (JWT `sub`) tem precedência sobre `X-Actor`, com fallback `system`. Tudo no MDC.
2. **Registro:** `ApiSupport` grava `AuditEntry` (action, correlationId, actor, input, outcome, error, durationMs) a cada execução, com `outcome` em `SUCCESS | NOT_FOUND | VALIDATION_ERROR | UNAVAILABLE | INFRA_ERROR`.
3. **Persistência:** porta `AuditTrail` com implementação `MongoAuditTrail` (coleção `audit_logs`, índices em `occurredAt` e `correlationId`), desligável via `AUDIT_ENABLED=false`.
4. **Domínio:** entidades carregam `createdBy`/`lastModifiedBy` (+ `markAsUpdatedBy`), prontos para `@CreatedBy` quando os documents chegarem.

A escrita na trilha é **best-effort**: falha de auditoria loga `warn` e nunca quebra a requisição.

## Consequências

- **Pró:** auditoria transversal sem poluir domínio/aplicação; quando o login chegar, a trilha vira por-usuário sem mudança (o ator passa a ser o `sub` do token).
- **Contra:** escrita síncrona adiciona latência a cada request (evoluir para assíncrono/outbox se necessário).
- **Contra:** `input.toString()` pode carregar dados sensíveis (ex.: senha no login — login não passa pelo `ApiSupport`, mas revisar ao adicionar novos fluxos).
