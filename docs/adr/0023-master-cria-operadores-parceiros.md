# ADR-023: Criação de parceiros e operadores só pelo master (ADMIN)

- **Status:** Aceito
- **Data:** 2026-09-24

## Contexto

O `POST /partners` nasceu como cadastro público (auto-registro, como
`POST /customers`). Com o login 100% via Mongo (sem bootstrap no
`application.yml`) e o agregado `Operator` (role `ADMIN`), o modelo de
operação mudou: parceiros são onboardados pelo backoffice, e operadores
só fazem sentido criados por quem já é admin — auto-registro de `ADMIN`
seria escalação de privilégio aberta.

## Decisão

- **Novo:** `POST /operators` (`CreateOperatorUseCase` + `OperatorController`),
  com `@PreAuthorize("hasRole('ADMIN')")`. O master cria operadores
  (`name`, `email`, `password` com hash BCrypt); o novo operador autentica
  em `POST /auth/login` com as autoridades de `ADMIN`.
- **Restrição:** `POST /partners` passa a exigir `hasRole('ADMIN')` e sai do
  `permitAll` no `SecurityConfiguration` (só `POST /customers` segue público).
- Sem token → 401; com token não-admin → 403.

## Consequências

- **Pró:** onboarding de parceiros controlado; sem rota pública que crie `ADMIN`.
- **Contra:** fluxos de teste/seed que criavam parceiro anônimo precisam de
  token admin (`ZipCodeEnrichmentE2ETest`, `PartnerControllerTest`,
  `http/partner.http` atualizados; steps Cucumber já usavam `asAdmin()`).
- **Evolução:** se o produto quiser auto-cadastro de parceiro com aprovação,
  modelar como solicitação pendente em vez de reabrir o `POST /partners`.
