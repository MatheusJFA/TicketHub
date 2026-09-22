# ADR-009: Testes de aceitação com Cucumber

- **Status:** Aceito
- **Data:** 2026-09-18

## Contexto

Os testes ponta a ponta (`*E2ETest`) cobrem endpoints isolados, mas nenhum teste exercita jornadas completas de negócio (parceiro cria show → adiciona seções → publica → spots materializam via Kafka) contra infraestrutura real.

## Decisão

Cucumber 7 (JUnit Platform) com features em português (`infrastructure/src/test/resources/features`), executadas pelo runner `CucumberAcceptanceIT` no failsafe (tag `e2eTest`):

- Contexto Spring completo (`@SpringBootTest` + MockMvc) sobre os mesmos containers dos ITs (`ContainerSupport`: MongoDB 8.0 + Kafka 3.9.1, sem depender de docker-compose).
- Steps cunham JWTs com `TestTokens` (dono/AN outro parceiro/admin) para cobrir ownership; `Hooks` limpa todas as coleções antes de cada cenário.
- O cenário assíncrono usa o listener Kafka real do contexto e aguarda a materialização por polling (deadline de 90s).

## Alternativas consideradas

- **Só `*E2ETest` com MockMvc por endpoint:** descartado — cobre endpoints isolados, mas nenhuma jornada completa (criar show → sections → publish → spots via Kafka) nem ownership entre parceiros.
- **Karate DSL:** descartado — bom para contrato HTTP, mas steps em Gherkin português + código Java reaproveitam `TestTokens`/`ContainerSupport` já existentes; trocar de framework duplicaria base.
- **Robot Framework:** descartado — stack externa em Python fora do build Maven/Failsafe; manutenção em duas linguagens sem ganho de legibilidade sobre o Cucumber-JVM.
- **Cypress/Playwright (ponta a ponta no front):** descartado — não há front web neste estágio; teste de API com contexto Spring real dá o mesmo sinal sem browser.
- **Sem testes de aceitação (só unit + IT):** descartado — o caminho assíncrono de spots (Kafka real + polling) só é exercitado na jornada; sem ela, regressão silenciosa.

## Consequências

- **Pró:** regressão de jornadas completas, incluindo o caminho assíncrono de geração de spots, legível por não-desenvolvedores.
- **Contra:** suíte mais lenta (containers + polling Kafka); cenários devem ser independentes (limpeza total por cenário).
- **Contra:** `CucumberAcceptanceIT` precisa do `junit-platform-suite-engine` no classpath de teste.
