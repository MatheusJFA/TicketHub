# ADR-007: JWT próprio + RBAC com permissões + ownership

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

A API nasceu aberta. Era preciso autenticar atores (cliente, parceiro, admin) e autorizar por recurso — incluindo o caso central do domínio: **o parceiro A não pode mexer no show do parceiro B**. Role pura não expressa dono; permissão pura não escala a administração.

## Decisão

- **Autenticação:** JWT HS256 autoemitido (`POST /auth/login`), stateless, sem CSRF. Usuários bootstrap via configuração (BCrypt, env) até existir cadastro real. Keycloak avaliado e descartado por enquanto: excesso operacional para o estágio atual (registrar como evolução).
- **Autorização em duas camadas:**
  - Roles como pacotes de permissões (`Permission`: `show:create`, `section:publish`, `spot:write`…): `CUSTOMER` (própria conta), `PARTNER` (catálogo + própria conta), `ADMIN` (tudo).
  - Enforcement por permissão (`hasAuthority`) + ownership: `@showAccess` (dono do `partnerId` via claim `ownerId`) nos shows; `@ownerAccess.isSelfOrAdmin(#id)` nas contas; listagens de contas só `ADMIN`; catálogo (`GET`) público; cadastros públicos.
- Segurança fica na `infrastructure` (method security nas interfaces `*API`); o domínio não conhece atores.
- `@EnableMethodSecurity(proxyTargetClass = true)`: sem CGLIB, os proxies JDK escondem os controllers e zeram os mappings no Spring Boot 4.

## Consequências

- **Pró:** granularidade sem explosão administrativa; testes com JWT real cobrem 401/403/200 e a matriz (`ApiAuthorizationTest` trava drift via reflection).
- **Contra:** `ownerId` precisa ser provisionado junto ao usuário (env) até o cadastro existir.
- **Contra:** sections/spots avulsos seguem só por role — ownership encadeado exige `showId` no domínio (evolução futura).
