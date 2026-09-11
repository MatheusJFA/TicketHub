# TicketHub Server

Projeto Maven com três módulos, usando Java 25.

- `domain`: entidades, agregados, objetos de valor, validações e contratos de gateways. Depende apenas do Java em produção.
- `application`: casos de uso e sua orquestração. Depende de `domain`, sem frameworks.
- `infrastructure`: inicialização Spring Boot, configuração e futuras implementações de gateways e adaptadores de entrada/saída. Depende de `application` e `domain`.

As dependências apontam para dentro: `infrastructure → application → domain`. As interfaces de gateways ficam no domínio; suas implementações ficam na infraestrutura.

O POM raiz agrega os módulos e centraliza versões. Apenas `infrastructure` gera um JAR executável Spring Boot. Cada módulo mantém seus próprios testes; os testes de arquitetura ficam em `infrastructure`, onde as três camadas estão disponíveis.

Configure `JAVA_HOME` para um JDK 25 e execute na raiz:

```shell
mvn clean verify
java -jar infrastructure/target/infrastructure-0.0.1-SNAPSHOT.jar
```

Os testes de arquitetura verificam a direção das dependências, impedem bibliotecas externas em `domain` e `application` e exigem que as classes de produção pertençam a uma das três camadas.
