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
- Ownership fino de `Section`/`Spot`: `@showAccess.canWriteSection/canWriteSpot` (e variantes publish/delete) resolvem o dono via campos denormalizados (`partnerId`, fallback `showId`) — 1 leitura indexada no fast path. Updates avulsos preservam os vínculos; documentos órfãos/legados sem vínculo negam escrita para não-admin (fail-closed).
- `@EnableMethodSecurity(proxyTargetClass = true)`: sem CGLIB, os proxies JDK escondem os controllers e zeram os mappings no Spring Boot 4.

## Alternativas consideradas

- **Keycloak/OIDC externo:** descartado por enquanto — registrado como evolução (ADR-010); excesso operacional (realm, clientes, rotação) para três papéis e ownership por `partnerId`.
- **Sessão server-side (cookie + store):** descartada — exigiria store compartilhado (Redis/DB) e CSRF; JWT stateless escala sem estado de sessão.
- **RBAC só com roles (`hasRole`), sem permissões:** descartado — granularidade como `section:publish` vs. `spot:write` exigiria explosão de roles; permissões como authorities evitam isso.
- **ACL por objeto (Spring ACL):** descartada — tabela de ACEs por show/section/spot é pesada para regra simples ("dono do `partnerId`"); ownership via claim `ownerId` + leitura indexada resolve em 1 query.
- **ABAC genérico (políticas OPA/Cedar):** descartado — poder expressivo desnecessário agora; custo de policy engine externa sem casos de atributo além de dono/papel.

## Consequências

- **Pró:** granularidade sem explosão administrativa; testes com JWT real cobrem 401/403/200 e a matriz (`ApiAuthorizationTest` trava drift via reflection).
- **Contra:** `ownerId` precisa ser provisionado junto ao usuário (env) até o cadastro existir.
- **Contra:** criação avulsa (`POST /sections`, `POST /spots`) segue só por role — o vínculo de dono nasce na persistência via grafo do `Show`.
