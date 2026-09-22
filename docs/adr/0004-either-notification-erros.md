# ADR-004: `Either<Notification, Output>` e envelope de erros

- **Status:** Aceito
- **Data:** 2026-09-17

## Contexto

Casos de uso precisam distinguir três situações: sucesso, erro de validação/regra (incluindo 404) e falha de infraestrutura — sem espalhar `try/catch` e sem vazar exceção de domínio para o HTTP.

## Decisão

- Todo caso de uso retorna `Either<Notification, Output>` (`Optional<Notification>` nos deletes via `UnitUseCase`); `Notification` acumula `Error`s (pattern do Full Cycle).
- `HttpResults` traduz o resultado em valor ou exceção tipada (`require`/`requireEmpty`); o aspecto `UseCaseMonitoringAspect` aplica resiliência e auditoria a cada execução.
- `GlobalExceptionHandler` mapeia para o envelope `ErrorResponse`: 404 (`"<Recurso> not found: <id>"`), 422 (validação), 400 (sintaxe/bean), 401/403 (segurança), 503 (indisponível), 500 genérico sem vazar stack.
- Controllers finos: contrato/OpenAPI nas interfaces `*API`, comandos e respostas convertidos por mappers MapStruct (`infrastructure.mapping`), lógica delegada aos casos de uso.

## Alternativas consideradas

- **Exceções de domínio propagadas até o controller:** descartado — espalharia `try/catch` por ~40 casos de uso e vazaria tipos de domínio para o HTTP, quebrando o envelope uniforme.
- **Biblioteca Vavr (`Validation`/`Either`):** descartada — dependência externa no `domain`/`application` (que devem ser framework-free) por ganho marginal sobre o `Either` próprio já testado.
- **`Optional` + exceções para tudo:** descartado — `Optional.empty()` não carrega motivo (404 vs. 422 vs. 500), forçando parse de mensagem no handler.
- **Bean Validation + `ResponseEntityExceptionHandler` do Spring na aplicação:** descartado — amarraria casos de uso ao Spring e impediria acumular múltiplos erros de regra (Notification acumula `Error`s).

## Consequências

- **Pró:** contrato de erro uniforme em todos os recursos; testes de controller simples (mock do caso de uso + `fold`).
- **Contra:** `Notification` carrega `cause` de infra junto com erros de domínio — `ApiSupport` precisa inspecionar a causa para separar 500 de 422.
- **Contra:** strings de "not found" acoplam handler e casos de uso (mitigado por casamento genérico `^. + not found: .*$`).
