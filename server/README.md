# TicketHub Server

Projeto Maven com três módulos, usando Java 25 e Spring Boot 4.0.8. O Swagger usa springdoc 3.0.3; as migrações MongoDB usam Liquibase 5.0.3.

- `domain`: entidades, agregados, objetos de valor, validações e contratos de gateways. Depende apenas do Java em produção.
- `application`: casos de uso e sua orquestração. Depende de `domain`, sem frameworks.
- `infrastructure`: inicialização Spring Boot, configuração e futuras implementações de gateways e adaptadores de entrada/saída. Depende de `application` e `domain`.

As dependências apontam para dentro: `infrastructure → application → domain`. As interfaces de gateways ficam no domínio; suas implementações ficam na infraestrutura.

Todos os recursos possuem casos de uso de criação, busca por ID, listagem paginada e exclusão. As alterações são expressas por ações específicas:

| Recurso | Ações adicionais |
| --- | --- |
| Customer | Alterar nome. CPF imutável. |
| Partner | Alterar nome e endereço. CNPJ imutável. |
| Show | Alterar nome e descrição, reagendar, adicionar seção, publicar e despublicar (individualmente ou com suas seções). Partner imutável. |
| Section | Alterar nome, descrição e preço, publicar e despublicar (individualmente ou com seus spots). |
| Spot | Alterar localização, publicar e despublicar. |

As consultas recebem `SearchQuery` ou um ID e devolvem DTOs, com paginação na listagem. Os casos de uso retornam `Either<Notification, Output>` para representar erros e resultados. A exclusão delega a `deleteById` do gateway, seguindo o contrato existente. Os adaptadores de persistência ainda estão pendentes.

## API HTTP e Swagger

As interfaces em `infrastructure.api` concentram `@RequestMapping`, `@Operation`, `@ApiResponses` e `@Tag`. Os controllers em `infrastructure.api.controllers` implementam esses contratos e convertem os DTOs HTTP para comandos dos casos de uso. Swagger UI: http://localhost:8080/swagger-ui/index.html. OpenAPI JSON: http://localhost:8080/v3/api-docs.

Os recursos `/customers`, `/partners`, `/shows`, `/sections` e `/spots` oferecem criação (`POST`, resposta `201` com `Location`), listagem (`GET`), consulta por ID (`GET /{id}`) e exclusão (`DELETE /{id}`, resposta `204`). As listagens aceitam `search`, `page` (padrão 0), `perPage` (padrão 10, máximo 100), `sort` e `dir` (`asc` ou `desc`).

As alterações usam `PATCH /{id}/name`, `/description`, `/address`, `/date`, `/price` ou `/location`, conforme as ações de cada recurso. Publicação usa `POST /{id}/publish` e `/unpublish`; shows e sections também oferecem `/publish-all` e `/unpublish-all`. Seções são adicionadas ao show por `POST /shows/{id}/sections`.

Endereços são objetos com `street`, `number`, `complement`, `neighborhood`, `city`, `state`, `country` e `zipCode`. Preços usam `{"value":50.00,"currency":"BRL"}`, e localização usa uma string, como `"A1"`. Datas usam ISO 8601 com offset, preservado na entrada. `totalSpots` omitido na criação de show assume zero.

JSON inválido retorna `400`, entidade ausente retorna `404`, validações retornam `422` e falhas inesperadas retornam `500` sem expor detalhes internos. As notificações atuais de entidade ausente são reconhecidas pelo formato `Resource not found: id`. A exclusão mantém a semântica do gateway existente.

As classes `CustomerUseCaseConfig`, `PartnerUseCaseConfig`, `ShowUseCaseConfig`, `SectionUseCaseConfig` e `SpotUseCaseConfig`, em `configuration.usecases`, recebem os gateways pelo construtor e expõem métodos públicos `@Bean`. Cada configuração é ativada quando seus gateways estão disponíveis; shows exigem `ShowGateway` e `PartnerGateway`. Até a implementação desses adaptadores, as operações dependentes retornam `503`, mantendo Swagger acessível. Os testes HTTP usam casos de uso mockados; os testes de configuração verificam a ligação dos casos de uso reais aos gateways.

O POM raiz agrega os módulos e centraliza versões. Apenas `infrastructure` gera um JAR executável Spring Boot. Cada módulo mantém seus próprios testes; os testes de arquitetura ficam em `infrastructure`, onde as três camadas estão disponíveis.

O [mise](https://mise.jdx.dev/) gerencia Java 25.0.2 e Maven 3.9.16. Com o mise instalado, execute na pasta `server`:

```shell
mise trust
mise install
mise run test
mise run application
```

Os comandos usam automaticamente o Java e o Maven configurados em `mise.toml`, sem precisar configurar `JAVA_HOME` no terminal.

| Comando | Ação |
| --- | --- |
| `mise run application` | Compila os módulos necessários sem executar testes e inicia a aplicação Spring Boot. |
| `mise run test` | Executa os testes de todos os módulos, incluindo os testes de arquitetura. |
| `mise run integration` | Executa a verificação completa e os testes reais de MongoDB e Kafka; requer os serviços do Compose iniciados. |
| `mise run build` | Compila, testa, empacota e verifica todos os módulos; `mise run verify` é um alias. |
| `mise run install` | Verifica e instala todos os módulos no repositório Maven local, útil para resolver dependências no editor. |
| `mise run clean` | Limpa os diretórios `target` dos módulos, preservando os backups de recuperação no `target` da raiz. |
| `mise run versions` | Mostra as versões e o caminho do Java e Maven em uso. |
| `mise tasks ls` | Lista os comandos disponíveis. |

A aplicação inicia o servidor HTTP na porta 8080 e disponibiliza `/actuator/health`. O comando `application` executa o JAR gerado em `infrastructure/target`. Para compilar e iniciar tudo pelo Docker, execute `docker-compose up -d` na raiz do repositório; veja o [guia do Docker Compose](../README.md).

Para usar o mesmo JDK no editor, execute `mise where java` e configure o caminho retornado como JDK do projeto nas extensões Java do VS Code. As tarefas do mise não alteram automaticamente o JDK do editor.

Os testes de arquitetura verificam a direção das dependências, impedem bibliotecas externas em `domain` e `application` e exigem que as classes de produção pertençam a uma das três camadas.

## Clientes MongoDB e Kafka

O projeto usa os starters modulares do Spring Boot 4 para Web MVC, Kafka e Spring Data MongoDB, com Jackson 3. As propriedades de conexão MongoDB ficam em `spring.mongodb`; a preservação do offset das datas fica em `spring.jackson.datatype.datetime`. Referência: [guia de migração do Spring Boot 4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide).

O Spring Boot fornece `MongoTemplate` e `MongoClient` a partir de `spring.mongodb`. `MongoConfiguration` ajusta os timeouts de conexão, leitura e seleção de servidor, preservando a configuração automática de autenticação. `MONGO_DATABASE` e `MONGO_AUTH_DATABASE` selecionam o banco e o banco de autenticação. `MONGO_CONNECT_TIMEOUT`, `MONGO_READ_TIMEOUT` e `MONGO_SERVER_SELECTION_TIMEOUT` aceitam durações como `5s` e têm esse valor como padrão.

`KafkaConfiguration` declara o tópico `tickethub.events`, criado pelo `KafkaAdmin` ao iniciar. `KAFKA_TOPIC`, `KAFKA_TOPIC_PARTITIONS` e `KAFKA_TOPIC_REPLICAS` configuram nome, partições e réplicas. A quantidade de réplicas deve ser compatível com os brokers disponíveis. A declaração não reduz partições nem altera automaticamente a replicação de um tópico existente.

Os adaptadores podem injetar `KafkaTemplate<String, String>` para enviar mensagens e `ConsumerFactory<String, String>` para criar consumidores. Chaves e valores são strings; payloads JSON devem ser serializados explicitamente antes do envio. `sendDefault` usa o tópico configurado. Aguarde o resultado assíncrono para tratar falhas de envio. O produtor usa `acks=all` e idempotência, sem garantir atomicidade entre MongoDB e Kafka.

O grupo padrão é `tickethub`, configurável por `KAFKA_CONSUMER_GROUP`. Novos grupos começam no primeiro registro disponível. O commit automático está desabilitado; listeners Spring usam confirmação por registro após o processamento. Consumidores criados diretamente pela fábrica devem gerenciar seus próprios offsets. Nenhum listener de negócio está registrado ainda.

As configurações seguem os mecanismos oficiais do Spring: [KafkaAdmin e tópicos](https://docs.spring.io/spring-kafka/reference/kafka/configuring-topics.html) e [customização do cliente MongoDB](https://docs.spring.io/spring-boot/4.0/api/java/org/springframework/boot/mongodb/autoconfigure/MongoClientSettingsBuilderCustomizer.html).

Para validar as conexões reais, inicie as dependências na raiz:

```shell
docker-compose up -d --wait mongo kafka
```

Depois, em `server`, execute `mise run integration`. O perfil Maven `integration` executa os testes `*IT` pelo Failsafe: gravação, leitura e exclusão com `MongoTemplate`, criação de tópico e envio/consumo com os clientes Spring Kafka. Os testes usam coleção e tópico com nomes únicos e os removem ao terminar. `mise run test` e `mise run build` continuam sem exigir serviços externos.

## Migrações com Liquibase

`LiquibaseConfiguration` executa o changelog `infrastructure/src/main/resources/db/changelog/db.changelog-master.xml` durante a inicialização. Uma falha na migração impede a aplicação de iniciar. O changelog inicial cria as coleções `customers`, `partners`, `shows`, `sections` e `spots`. Liquibase mantém histórico e lock em `DATABASECHANGELOG` e `DATABASECHANGELOGLOCK`.

A integração usa `liquibase-core` e a [extensão MongoDB](https://github.com/liquibase/liquibase-mongodb), com o cliente gerenciado pelo Spring, reutilizando autenticação, TLS, banco e timeouts. A autoconfiguração JDBC de Liquibase não se aplica ao MongoDB.

Adicione novas migrações em `db/changelog/changes` e inclua-as no changelog mestre. Preserve os IDs e o conteúdo de changesets já executados. O changelog inicial pressupõe que as coleções de negócio ainda não existam; bancos preexistentes precisam de uma baseline revisada antes da primeira execução.

`LIQUIBASE_ENABLED` controla as migrações e assume `true`. Para testes HTTP sem banco, use `tickethub.liquibase.enabled=false`. O perfil `integration` também executa `LiquibaseMigrationIT` em um banco temporário exclusivo, verificando criação, histórico, reexecução sem duplicação e preservação dos documentos; o banco de teste é removido ao final.
