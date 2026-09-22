# ADR-013: CEP como lookup dedicado (fim do enriquecimento no create/update)

- **Status:** Aceito
- **Data:** 2026-09-20
- **Supersede:** ADR-011 (parcial — mantém provider/contrato, remove o acoplamento)

## Contexto

O ADR-011 acoplou o ViaCEP aos fluxos de criação/alteração (parceiro e
show): cada cadastro fazia uma chamada síncrona ao provedor para enriquecer
o endereço. Isso adiciona latência e ponto de falha a operações que não
precisam dele — o endereço enviado pelo cliente já é suficiente para
persistir.

## Decisão

- **Removido** `ZipCodeLookup` de `DefaultCreatePartnerUseCase`,
  `DefaultUpdatePartnerUseCase`, `DefaultChangePartnerAddressUseCase` e
  `DefaultCreateShowUseCase` (e das configs): create/update persistem o
  endereço exatamente como enviado.
- **Novo caso de uso** `zipcode/lookup/DefaultLookupZipCodeUseCase` + endpoint
  público `GET /zipcode/{zipCode}` (200 com o endereço, 404 se
  desconhecido/inválido): o front usa para autofill do formulário *antes*
  do cadastro.
- **Mantidos** `ZipCodeLookup`/`ViaCepLookup`/`ZipCodeAddress`/`Address.enrichedWith`
  como porta + adapter do lookup dedicado (contrato fail-open continua
  valendo).

## Alternativas consideradas

- **Manter enriquecimento automático no create/update:** descartado — cada cadastro pagava latência do ViaCEP e herdava o ponto de falha; o endereço do cliente já basta para persistir.
- **Remover CEP por completo (sem endpoint):** descartado — perderia o autofill do formulário, UX que o ADR-011 já havia entregue; `GET /zipcode/{zipCode}` preserva o valor sem o acoplamento.
- **Frontend chamando ViaCEP direto:** descartado — cada cliente reimplementaria timeout, normalização (`ZipCodeAddress`) e tratamento de `erro:true`; o backend centraliza o contrato fail-open.
- **Enriquecimento assíncrono pós-cadastro (job/evento):** descartado — complexidade de worker + estado "endereço pendente" para ganho nulo: o dado cru já é suficiente e o autofill pré-cadastro resolve a UX.

## Consequências

- **Pró:** cadastro sem dependência de rede do provedor; UX de autofill
  preservada via endpoint dedicado; testes de create/update sem mocks de
  CEP.
- **Contra:** clientes que dependiam do enriquecimento automático passam a
  receber o endereço cru — precisam chamar `GET /zipcode` antes, se quiserem.
- **Evolução:** cache com TTL no lookup dedicado (mesma evolução já prevista
  no ADR-011).
