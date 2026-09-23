# TicketHub Web (Angular)

SPA do TicketHub: catálogo de shows, escolha de assentos, checkout PIX e
acompanhamento do pedido.

## Desenvolvimento

```shell
npm install
npm start
```

Abre em http://localhost:4200 com a API em http://localhost:8080
(`public/app-config.json`, lido no boot — a mesma imagem Docker aponta para
outro backend trocando o arquivo, sem rebuild). O backend libera CORS para
`http://localhost:4200` (`tickethub.cors.allowed-origins`).

## Produção

```shell
npm run build
```

O `docker-compose up -d` da raiz já inclui o serviço `web` (nginx servindo o
build em http://localhost:4200, `WEB_PORT` para trocar a porta).

## Fluxo de compra

Shows (público) → mapa de assentos do show (`/shows/:id/seats`, agrupado por
setor via `GET /shows/{id}/sections` + `GET /sections/{id}/spots`) → login → checkout (cria o
pedido com `Idempotency-Key`, paga e exibe o PIX copia e cola) → polling do
status até `PAID`. O `customerId` do pedido sai do claim `ownerId` do JWT;
entre com um usuário cliente (ex.: `maria@domain.com`).
