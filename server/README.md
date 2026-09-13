# TicketHub Server

Projeto Maven com três módulos, usando Java 25.

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

As consultas recebem `SearchQuery` ou um ID e devolvem DTOs, com paginação na listagem. Os casos de uso retornam `Either<Notification, Output>` para representar erros e resultados. A exclusão delega a `deleteById` do gateway, seguindo o contrato existente. Os adaptadores de persistência e endpoints HTTP de negócio ainda estão pendentes.

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

O Spring Boot fornece `MongoTemplate` e `MongoClient` a partir de `spring.data.mongodb`. `MongoConfiguration` ajusta os timeouts de conexão, leitura e seleção de servidor, preservando a configuração automática de autenticação. `MONGO_DATABASE` e `MONGO_AUTH_DATABASE` selecionam o banco e o banco de autenticação. `MONGO_CONNECT_TIMEOUT`, `MONGO_READ_TIMEOUT` e `MONGO_SERVER_SELECTION_TIMEOUT` aceitam durações como `5s` e têm esse valor como padrão.

`KafkaConfiguration` declara o tópico `tickethub.events`, criado pelo `KafkaAdmin` ao iniciar. `KAFKA_TOPIC`, `KAFKA_TOPIC_PARTITIONS` e `KAFKA_TOPIC_REPLICAS` configuram nome, partições e réplicas. A quantidade de réplicas deve ser compatível com os brokers disponíveis. A declaração não reduz partições nem altera automaticamente a replicação de um tópico existente.

Os adaptadores podem injetar `KafkaTemplate<String, String>` para enviar mensagens e `ConsumerFactory<String, String>` para criar consumidores. Chaves e valores são strings; payloads JSON devem ser serializados explicitamente antes do envio. `sendDefault` usa o tópico configurado. Aguarde o resultado assíncrono para tratar falhas de envio. O produtor usa `acks=all` e idempotência, sem garantir atomicidade entre MongoDB e Kafka.

O grupo padrão é `tickethub`, configurável por `KAFKA_CONSUMER_GROUP`. Novos grupos começam no primeiro registro disponível. O commit automático está desabilitado; listeners Spring usam confirmação por registro após o processamento. Consumidores criados diretamente pela fábrica devem gerenciar seus próprios offsets. Nenhum listener de negócio está registrado ainda.

As configurações seguem os mecanismos oficiais do Spring: [KafkaAdmin e tópicos](https://docs.spring.io/spring-kafka/reference/kafka/configuring-topics.html) e [customização do cliente MongoDB](https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/autoconfigure/mongo/MongoClientSettingsBuilderCustomizer.html).

Para validar as conexões reais, inicie as dependências na raiz:

```shell
docker-compose up -d --wait mongo kafka
```

Depois, em `server`, execute `mise run integration`. O perfil Maven `integration` executa os testes `*IT` pelo Failsafe: gravação, leitura e exclusão com `MongoTemplate`, criação de tópico e envio/consumo com os clientes Spring Kafka. Os testes usam coleção e tópico com nomes únicos e os removem ao terminar. `mise run test` e `mise run build` continuam sem exigir serviços externos.
