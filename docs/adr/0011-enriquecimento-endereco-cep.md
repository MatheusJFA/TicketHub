# ADR-011: Enriquecimento de endereço via CEP (ViaCEP)

- **Status:** Aceito
- **Data:** 2026-09-19

## Contexto

Cadastro de parceiros e shows exige endereço completo, mas o usuário nem
sempre sabe rua/bairro/cidade/UF de cabeça — o CEP (zipCode) resolve isso via
APIs públicas como o ViaCEP (gratuito, sem chave). Era preciso um ponto único
e organizado para chamadas HTTP de saída, sem espalhar `RestClient` pelos
adapters.

## Decisão

- **Base HTTP (`shared/http/BaseHttpClient`):** monta `RestClient` com
  base URL, timeouts configuráveis, `Accept: JSON` e `User-Agent`; oferece
  `getOptional` (fail-open: loga e retorna vazio em 4xx/5xx/timeout) e
  `getRequired` (fail-closed via `HttpUpstreamException`). A fábrica de
  request fica com o chamador para permitir `MockRestServiceServer` nos testes.
- **Domínio (`domain.geo`):** porta `CepLookup` (contrato fail-open: nunca
  lança, vazio = segue com dados do usuário) + `CepAddress` (normaliza o CEP
  para 8 dígitos) + `Address.enrichedWith` (sobrescreve rua/bairro/cidade/UF/
  país/CEP, preserva número e complemento; campos em branco do provedor mantêm
  o valor atual).
- **Adapter (`cep/ViaCepLookup` + `ViaCepClient`):** `tickethub.cep.*`
  (`enabled`, `base-url`, timeouts); `erro:true`/400/desabilitado = vazio.
- **Aplicação:** criação de parceiro/show, alteração e update de endereço do
  parceiro enriquecem antes de persistir. Falha do provedor não quebra o
  cadastro (fail-open, exigido para não travar vendas/cadastros).

## Consequências

- **Pró:** um lugar só para evoluir HTTP de saída (retry/CB podem entrar na
  base depois); cobertura sem rede via `MockRestServiceServer` + E2E fail-open
  com provider inalcançável.
- **Contra:** enriquecimento é síncrono na requisição (latência do ViaCEP;
  timeout padrão 5s); sem cache — CEPs repetidos reconsultam.
- **Evolução:** cache com TTL, retry com backoff na base, segundo provedor
  como fallback.
