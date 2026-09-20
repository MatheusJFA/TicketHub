# TicketHub

## Rodar com Docker Compose

Com o Docker Desktop iniciado em modo de containers Linux, execute na raiz do repositório:

```shell
docker-compose up -d
```

O Compose compila os três módulos Maven dentro do Docker e inicia o backend em http://localhost:8080. Não é necessário instalar Java, Maven ou mise no computador. A primeira execução baixa as imagens e dependências; as próximas reutilizam o cache. O mesmo comando reconstrói a imagem quando os fontes mudam.

O [Dockerfile](server/Dockerfile) usa build em múltiplos estágios: `build` compila com Maven e JDK 25, reutilizando o cache de dependências; `runtime` recebe apenas o JAR da aplicação e executa com JRE 25 e usuário sem privilégios de root. O Compose seleciona o estágio `runtime`, mantendo Maven e os fontes fora da imagem final.

Verifique a saúde da aplicação em http://localhost:8080/actuator/health. A resposta esperada é `{"status":"UP"}`. A documentação interativa está em http://localhost:8080/swagger-ui/index.html e o contrato OpenAPI em http://localhost:8080/v3/api-docs. As rotas de negócio estão definidas, mas retornam `503` enquanto os gateways de persistência não forem implementados; `/` retorna 404.

```shell
docker-compose ps
docker-compose logs -f server
docker-compose down
```

Para mudar a porta local, defina `SERVER_PORT` no terminal antes de iniciar. Exemplo em PowerShell:

```powershell
$env:SERVER_PORT = "8081"
docker-compose up -d
```

`docker compose` também pode ser usado no lugar de `docker-compose`.

O serviço `server` executa o módulo `infrastructure`, que utiliza `domain` e `application` como bibliotecas. O backend aguarda MongoDB e Kafka ficarem saudáveis antes de iniciar. Ainda não há frontend configurado.

## MongoDB

O MongoDB sobe junto com o backend usando o mesmo `docker-compose up -d`. Os dados ficam no volume `mongo_data` e são preservados por `docker-compose down`.

Para conectar pelo MongoDB Compass, use:

```text
mongodb://tickethub:tickethub-local@localhost:27017/tickethub?authSource=admin
```

Essas são credenciais padrão de desenvolvimento local. Você pode definir `MONGO_USERNAME`, `MONGO_PASSWORD` e `MONGO_PORT` no terminal antes da primeira inicialização. As credenciais de inicialização só são aplicadas quando o volume está vazio; mudar essas variáveis não altera usuários de um banco existente.

O Spring Boot 4 está conectado ao banco `tickethub`, e `/actuator/health` também verifica a conexão com o MongoDB. O Liquibase cria as coleções iniciais e registra as migrações ao iniciar a aplicação. As implementações dos gateways para persistir as entidades ainda não foram criadas. `LIQUIBASE_ENABLED=false` desabilita as migrações; o padrão é `true`. Veja o [guia de migrações](server/README.md#migrações-com-liquibase).

Para iniciar apenas o banco e executar o backend localmente com mise:

```shell
docker-compose up -d mongo
```

A configuração local usa `localhost:27017` e as mesmas credenciais padrão.

## Kafka

O Kafka 3.9.1 sobe com o mesmo `docker-compose up -d`, em modo KRaft com um único broker, sem ZooKeeper. Usa a [imagem oficial do Apache Kafka](https://kafka.apache.org/39/getting-started/quickstart/). Os dados ficam no volume `kafka_data` e são preservados por `docker-compose down`.

Clientes na máquina local usam `localhost:9092`; o backend no Docker usa `kafka:19092`. Para alterar a porta local, defina `KAFKA_PORT` antes de iniciar. O backend executado localmente também lê essa variável; `KAFKA_BOOTSTRAP_SERVERS` permite informar outro endereço completo.

Para iniciar as dependências e executar o backend localmente com mise:

```shell
docker-compose up -d mongo kafka
```

Para verificar o broker e listar os tópicos:

```shell
docker-compose ps kafka
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:19092 --list
```

O módulo `infrastructure` configura o produtor, a fábrica de consumidores e cria o tópico `tickethub.events` na inicialização, com três partições e uma réplica. O backend falha na inicialização se o Kafka estiver indisponível. Produtores e consumidores de eventos de negócio ainda não foram implementados. O healthcheck do Compose verifica o broker; `/actuator/health` não verifica Kafka. A configuração é para desenvolvimento local, com conexão PLAINTEXT sem autenticação e porta exposta apenas em loopback.

Defina `KAFKA_TOPIC`, `KAFKA_TOPIC_PARTITIONS`, `KAFKA_TOPIC_REPLICAS` e `KAFKA_CONSUMER_GROUP` para personalizar a configuração. O ambiente local possui um único broker e usa uma réplica. Veja os clientes disponíveis e os testes de integração no [guia do servidor](server/README.md).

## Observabilidade (stack LGTM)

O mesmo `docker-compose up -d` sobe Loki (logs), Tempo (traces), Mimir (métricas) e Grafana, com o Alloy como coletor único:

```text
server --OTLP traces--> alloy --> tempo
server --/actuator/prometheus--> alloy --> mimir
docker logs ---------> alloy --> loki
grafana --> loki / tempo / mimir
```

| Serviço | URL local | Variável de porta |
| --- | --- | --- |
| Grafana | http://localhost:3000 | `GRAFANA_PORT` |
| Loki | http://localhost:3100 | `LOKI_PORT` |
| Tempo | http://localhost:3200 | `TEMPO_PORT` |
| Mimir | http://localhost:9009 | `MIMIR_PORT` |
| Alloy UI | http://localhost:12345 | `ALLOY_PORT` |
| OTLP gRPC/HTTP | localhost:4317 / localhost:4318 | `OTEL_GRPC_PORT` / `OTEL_HTTP_PORT` |

O Grafana já vem com os datasources Loki, Tempo e Mimir provisionados (`observability/grafana/provisioning`). O login padrão é `admin` / `admin-local`, sobrescreva com `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD`. Os logs carregam os labels `container` e `compose_service`, e as linhas da aplicação incluem `traceId`/`spanId`, permitindo saltar do trace (Tempo) para os logs (Loki). Amostragem de traces via `TRACING_SAMPLING_PROBABILITY` (padrão `1.0` em dev). Detalhes em [docs/observability.md](docs/observability.md).

O build da imagem não executa testes por padrão. Para incluí-los, execute `docker compose build --build-arg SKIP_TESTS=false server`. Para desenvolvimento local e execução da suíte, consulte [server/README.md](server/README.md).
