# TicketHub

## Rodar com Docker Compose

Com o Docker Desktop iniciado em modo de containers Linux, execute na raiz do repositório:

```shell
docker-compose up -d
```

O Compose compila os três módulos Maven dentro do Docker e inicia o backend em http://localhost:8080. Não é necessário instalar Java, Maven ou mise no computador. A primeira execução baixa as imagens e dependências; as próximas reutilizam o cache. O mesmo comando reconstrói a imagem quando os fontes mudam.

Verifique a saúde da aplicação em http://localhost:8080/actuator/health. A resposta esperada é `{"status":"UP"}`. As rotas de negócio ainda não foram implementadas; `/` retorna 404.

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

O Spring Boot já está conectado ao banco `tickethub`, e `/actuator/health` também verifica a conexão com o MongoDB. As implementações dos gateways para persistir as entidades ainda não foram criadas. O banco aparecerá no Compass quando receber a primeira coleção ou gravação.

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

A dependência `spring-kafka` e o endereço do broker estão configurados no módulo `infrastructure`. Tópicos e produtores/consumidores de eventos de negócio ainda não foram implementados. O healthcheck do Compose verifica o broker; `/actuator/health` não verifica Kafka. A configuração é para desenvolvimento local, com conexão PLAINTEXT sem autenticação e porta exposta apenas em loopback.

O build da imagem não executa testes. Para desenvolvimento local e execução da suíte, consulte [server/README.md](server/README.md).
