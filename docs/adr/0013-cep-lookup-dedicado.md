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

- **Removido** `CepLookup` de `DefaultCreatePartnerUseCase`,
  `DefaultUpdatePartnerUseCase`, `DefaultChangePartnerAddressUseCase` e
  `DefaultCreateShowUseCase` (e das configs): create/update persistem o
  endereço exatamente como enviado.
- **Novo caso de uso** `cep/lookup/DefaultLookupCepUseCase` + endpoint
  público `GET /cep/{zipCode}` (200 com o endereço, 404 se
  desconhecido/inválido): o front usa para autofill do formulário *antes*
  do cadastro.
- **Mantidos** `CepLookup`/`ViaCepLookup`/`CepAddress`/`Address.enrichedWith`
  como porta + adapter do lookup dedicado (contrato fail-open continua
  valendo).

## Consequências

- **Pró:** cadastro sem dependência de rede do provedor; UX de autofill
  preservada via endpoint dedicado; testes de create/update sem mocks de
  CEP.
- **Contra:** clientes que dependiam do enriquecimento automático passam a
  receber o endereço cru — precisam chamar `GET /cep` antes, se quiserem.
- **Evolução:** cache com TTL no lookup dedicado (mesma evolução já prevista
  no ADR-011).
