# ADR-010: Login por email + refresh rotativo com revogação

- **Status:** Aceito
- **Data:** 2026-09-19

## Contexto

O login nasceu sobre usuários bootstrap via env (`ADR-007`): `POST /auth/login`
com `username/senha` direto no `AuthService` da infraestrutura, sem vínculo com
o cadastro (`POST /customers`, `POST /partners` públicos não criavam credencial
e o `ownerId` era provisionado manualmente). Faltavam sessão renovável, logout
com revogação e proteção contra brute-force.

## Decisão

- **Credencial no domínio:** `Customer`/`Partner` ganham `Email` (identificador
  único de login, normalizado) + `PasswordHash` (só BCrypt). Índices únicos
  parciais (`005-auth-credentials`): documentos legados sem email continuam
  legíveis e falham fechado apenas no login.
- **Casos de uso em `application`:** `LoginUseCase`, `RefreshTokenUseCase` e
  `LogoutUseCase` retornando `Either<Notification, Output>`; 401 via
  `AuthenticationException` (mapeada em `HttpResults`/`GlobalExceptionHandler`,
  auditada como `UNAUTHORIZED` em vez de `INFRASTRUCTURE_ERROR`).
- **Lookup unificado:** porta `AuthAccountGateway` (customers → partners →
  fallback bootstrap por username, mantendo o `ADMIN` operador até existir
  agregado próprio). `PasswordHasher`, `TokenIssuer` e `RefreshSessionGateway`
  são ports do domínio; BCrypt, JWT HS256 e Mongo são adapters.
- **Sessão:** access JWT curto (15–60min, mesmos claims) + refresh opaco
  rotativo em `refresh_sessions` (só hash SHA-256, `familyId`, TTL com expiração
  no Mongo). Reuso de token rotacionado revoga a família inteira (roubo
  presumido). `POST /auth/refresh`, `POST /auth/logout` (204, idempotente).
- **Brute-force:** `AuthRateLimitFilter` (resilience4j-ratelimiter, 10 req/min
  por IP em `/auth/**` → 429), configurável via `tickethub.auth.*`.
- **Utilitários:** commons-lang3/collections4 liberados no domínio/aplicação
  (puros, sem framework) para guards de null/string/coleção; `ArchitectureTest`
  atualizado.

## Alternativas consideradas

- **JWT longo sem refresh (ex.: 24h):** descartado — janela de abuso grande após vazamento, sem revogação; access curto + refresh rotativo limita o dano.
- **Sessão opaca só no servidor (sem JWT):** descartada — cada request exigiria lookup em `refresh_sessions`; JWT stateless mantém leitura sem I/O, com refresh opaco só na renovação.
- **Refresh JWT (stateless) em vez de opaco:** descartado — token stateless não é revogável; logout real exige identificador persistido (hash SHA-256 + `familyId`) para revogar a família.
- **Keycloak/OIDC como IdP:** descartado por enquanto — segue como evolução; IdP externo adiciona operação (realm, clientes) antes de existir sequer cadastro local com `ownerId`.
- **Rate-limit distribuído (Redis) já de início:** descartado — filtro por IP em memória basta para o volume atual; estado distribuído entra quando houver múltiplas réplicas.

## Consequências

- **Pró:** cadastro vira conta de verdade (`ownerId` = ID do agregado, sem
  provisionamento manual); autenticação testável sem Spring; logout real.
- **Contra:** JWT segue sem revogação de access (janela = expiração curta);
  admin ainda é bootstrap; filtro é por IP em memória (sem estado distribuído).
- **Evolução:** Keycloak/OIDC segue registrado como alternativa futura.
