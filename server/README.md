# TicketHub Server

Projeto Maven com três módulos, usando Java 25.

- `domain`: entidades, agregados, objetos de valor, validações e contratos de gateways. Depende apenas do Java em produção.
- `application`: casos de uso e sua orquestração. Depende de `domain`, sem frameworks.
- `infrastructure`: inicialização Spring Boot, configuração e futuras implementações de gateways e adaptadores de entrada/saída. Depende de `application` e `domain`.

As dependências apontam para dentro: `infrastructure → application → domain`. As interfaces de gateways ficam no domínio; suas implementações ficam na infraestrutura.

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
| `mise run build` | Compila, testa, empacota e verifica todos os módulos; `mise run verify` é um alias. |
| `mise run install` | Verifica e instala todos os módulos no repositório Maven local, útil para resolver dependências no editor. |
| `mise run clean` | Limpa os diretórios `target` dos módulos, preservando os backups de recuperação no `target` da raiz. |
| `mise run versions` | Mostra as versões e o caminho do Java e Maven em uso. |
| `mise tasks ls` | Lista os comandos disponíveis. |

A aplicação atual contém apenas a inicialização do Spring Boot, sem servidor HTTP; ela pode encerrar normalmente após iniciar. O comando `application` executa o JAR gerado em `infrastructure/target`.

Para usar o mesmo JDK no editor, execute `mise where java` e configure o caminho retornado como JDK do projeto nas extensões Java do VS Code. As tarefas do mise não alteram automaticamente o JDK do editor.

Os testes de arquitetura verificam a direção das dependências, impedem bibliotecas externas em `domain` e `application` e exigem que as classes de produção pertençam a uma das três camadas.
