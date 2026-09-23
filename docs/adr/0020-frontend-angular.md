# ADR-020: Frontend Angular (SPA)

- **Status:** Aceito
- **Data:** 2026-09-23

## Contexto

O TicketHub era só API. Para vender de verdade era preciso um storefront
(catálogo → assentos → checkout PIX → ingresso) e uma área partner mínima,
numa stack alinhada ao mercado que contrata Java no Brasil.

## Decisão

- **Angular 22 SPA standalone** (`web/`, nginx no compose em `:4200`):
  LinkedIn BR tem 2k+ vagas Java+Angular contra ~850 Java+React; globalmente
  React lidera, mas o par enterprise do Spring no Brasil é Angular.
- **Auth JWT espelhando o backend:** login/signup, interceptor Bearer com
  refresh rotativo em 401 (single-flight), guard com `returnUrl`,
  `customerId` do claim `ownerId`, link Admin por authority (`show:create`).
- **Compra:** seat-map por show (`GET /shows/{id}/sections` +
  `GET /sections/{id}/spots`), carrinho em signals, checkout com
  `Idempotency-Key` (`crypto.randomUUID`), PIX copia-e-cola + polling até
  `PAID`; consulta e cancelamento de pedido.
- **API configurável sem rebuild:** `public/app-config.json` lido via
  `APP_INITIALIZER` (fallback para `environment.ts`); CORS liberado no
  backend via `tickethub.cors.allowed-origins`.
- **Tailwind v4** (`@tailwindcss/postcss`) no lugar do SCSS artesanal.

## Consequências

- **Pró:** MVP comprável de ponta a ponta; mesma imagem Docker serve
  qualquer backend trocando um JSON.
- **Contra:** sem testes no web (scaffold com `--skip-tests`); sem QR do
  ingresso (falta endpoint de tickets por pedido); seat-map pagina
  (100 spots/seção); datas/valores com formatação mínima.
- **Evolução:** suite de testes, QR do ingresso, admin completo (CRUD e
  ownership total), i18n/acessibilidade.
