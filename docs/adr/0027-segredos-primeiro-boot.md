# ADR-027: Segredos gerados no primeiro boot

- **Status:** Aceito
- **Data:** 2026-09-24

## Contexto

`TICKETHUB_JWT_SECRET` e `TICKETHUB_TICKETS_SIGNATURE_SECRET` tinham default
dev no `application.yml`: deploy sem env usava segredo conhecido (sessões
forjáveis, QRs falsificáveis). O checklist pedia troca manual — falhava
aberta.

## Decisão

- **Defaults vazios** no `application.yml`; `GeneratedSecrets` resolve:
  configurado → usa; em branco + Mongo → gera (256 bits, base64url),
  persiste em `app_secrets` e reusa (WARN alto); em branco sem Mongo
  (slices) → efêmero em memória.
- `SecurityConfiguration` (decoder/encoder) e `HmacTicketSigner` consomem
  o provider; checagens de tamanho/não-branco continuam como defesa.
- Testes com segredos fixos (`ControllerTest`, `ContainerSupport`); geração
  coberta com `MongoTemplate` mockado.

## Consequências

- **Pró:** seguro por padrão; QRs (longos) estáveis via persistência;
  primeiro boot sem env continua saudável.
- **Contra:** operador sem env só descobre pelo warn; segredo gerado exige
  backup do volume `mongo_data` junto (perder = sessões/QRs inválidos).
- **Evolução:** rotação de segredos com janela dupla; mover para vault.
