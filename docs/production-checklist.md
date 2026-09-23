# Checklist de produção — TicketHub

O que trocar/confirmar antes de expor o backend. Todos os valores abaixo
têm default de desenvolvimento no `application.yml` / `docker-compose.yml`.

## Segredos (trocar todos, nunca commitar)

| Variável | Default dev | Notas |
|---|---|---|
| `TICKETHUB_JWT_SECRET` | `dev-only-change-me…` | ≥ 32 bytes; invalida todas as sessões ao trocar |
| `TICKETHUB_TICKETS_SIGNATURE_SECRET` | `dev-only…` | Invalida QRs já emitidos ao trocar |
| `MERCADOPAGO_ACCESS_TOKEN` | vazio (fail-fast no boot) | Token de produção da conta MP |
| `MERCADOPAGO_WEBHOOK_SECRET` | vazio (webhook responde 401) | Painel MP → Sua integração → Webhooks |
| `MERCADOPAGO_NOTIFICATION_URL` | vazio (sem callback dirigido) | URL pública `https://…/payments/mercadopago` |
| `MONGO_USERNAME`/`MONGO_PASSWORD` | `tickethub`/`tickethub-local` | Só valem com volume vazio |
| `GRAFANA_ADMIN_USER`/`GRAFANA_ADMIN_PASSWORD` | `admin`/`admin-local` | |
| Senhas seed (`*_PASSWORD_HASH`) | bcrypt de `admin-local` etc. | Trocar ou remover usuários bootstrap |

## Endpoints e integrações

- `GENERIC_WEBHOOK_ENABLED=false` — o `/payments/webhook` genérico confia
  no body e só serve para dev local.
- `CORS_ALLOWED_ORIGINS` — listar só os domínios do frontend
  (default `http://localhost:4200`).
- `MAIL_HOST`/`MAIL_PORT`/`MAIL_FROM` — SMTP real (dev usa Mailpit na
  porta 1025, UI na 8025). Sem SMTP, confirmações logam warn e seguem.
- `MERCADOPAGO_PAYER_EMAIL` é só fallback; o email real vem do Customer.

## Tempos e limites (revisar contra o negócio)

| Config | Default | Efeito |
|---|---|---|
| `TICKETHUB_ORDERS_RESERVATION_TTL` | 15m | Janela de pagamento do PIX |
| `TICKETHUB_ORDERS_EXPIRE_INTERVAL` | 1m | Sweeper de abandonados |
| `PAYMENT_RECONCILE_INTERVAL` | 2m | Liquida aprovados / estorna tardios |
| `CACHE_SHOWS_TTL` / `CACHE_SECTIONS_TTL` | 5m | Staleness do catálogo |
| `CACHE_SPOTS_TTL` | 15s | Staleness de disponibilidade |
| `TICKETHUB_AUTH_LOGIN_RATE_LIMIT_PER_MINUTE` | 10 | Por IP |
| `CHECKOUT_ORDER_RATE_LIMIT_PER_MINUTE` | 30 | Por IP, create + pay |
| `MERCADOPAGO_WEBHOOK_TOLERANCE` | 5m | Exige relógio sincronizado (NTP) |
| `SHUTDOWN_TIMEOUT` / `stop_grace_period` | 30s / 40s | Grace precisa `grace > timeout` |

## Dados e mensageria

- MongoDB: backup do volume `mongo_data`; `LIQUIBASE_ENABLED=true` mantém
  migrações (nunca editar changeset aplicado — só adicionar).
- Redis: cache puro (perda = miss, sem drama); `redis_data` opcional.
- Kafka: tópico `tickethub.events` (3 partições/1 réplica no compose);
  ajustar partições/réplicas e `KAFKA_CONSUMER_GROUP` por ambiente.
- Confirmar rotação do webhook secret sem downtime: atualizar MP e env
  juntos (não há janela de dois segredos).

## Observabilidade

- `TRACING_SAMPLING_PROBABILITY=1.0` é de dev — reduzir em produção.
- Alertas sugeridos (Mimir): conversão checkout (ADR-016/017/019 e
  `docs/observability.md`), `refunded > 0` por rodada de reconciliação,
  401/min no webhook (tentativa de forja), 429/min (flood ou limite baixo).
