# ADR-025: Revogação de access JWT via denylist no Redis

- **Status:** Aceito
- **Data:** 2026-09-24

## Contexto

O access JWT era stateless sem revogação (ADR-010): o logout revogava só o
refresh; o access seguia válido até expirar (janela de até 60min). Operador
bloqueado, troca de senha ou logout em dispositivo compartilhado não tinham
efeito imediato.

## Decisão

- **`jti` na emissão:** `JwtTokenIssuer` gera `jti` (UUID) em todo access.
  Tokens sem `jti` (testes, emitidos antes do deploy) nunca entram na denylist.
- **Denylist no Redis:** chave `tickethub:auth:denylist:{jti}` com TTL =
  validade restante do token. Porte `RevokedAccessTokenGateway`
  (domínio) + adapter `RedisRevokedAccessTokenGateway` (infra, fail-open
  com warn se o Redis falhar — mesma postura do mailer/auditoria).
- **Enforcement:** `RevokedAccessTokenFilter` após o
  `BearerTokenAuthenticationFilter`: `jti` negado → 401. Só atua com
  autenticação JWT presente; rotas públicas não mudam.
- **Logout:** `POST /auth/logout` aceita `accessToken` opcional
  (`LogoutRequest`); o caso de uso revoga o refresh (como antes) + o `jti`
  do access (ignora access inválido, segue 204 idempotente). Domínio segue
  framework-free via porte `AccessTokenInspector` (adapter Nimbus na infra).

## Consequências

- **Pró:** logout mata as duas pernas da sessão; base para bloqueio de
  operador e rotação forçada (basta negar o `jti`).
- **Contra:** Redis vira dependência de auth (fail-open documentado);
  refresh rotacionado não mata o access anterior (janela curta, aceita);
  cada request autenticado faz 1 `EXISTS` no Redis.
- **Evolução:** login com `jti` permite "encerrar outras sessões" (negar
  todos os `jti` do sujeito menos o atual — exige índice sujeito→jtis).
