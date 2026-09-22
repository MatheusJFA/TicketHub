# ADR-011: Enriquecimento de endereço via CEP (ViaCEP)

- **Status:** Parcialmente superseded pelo ADR-013 (provider/contrato mantidos; enriquecimento removido dos creates/updates)
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
- **Domínio (`domain.geography`):** porta `ZipCodeLookup` (contrato fail-open: nunca
  lança, vazio = segue com dados do usuário) + `ZipCodeAddress` (normaliza o CEP
  para 8 dígitos) + `Address.enrichedWith` (sobrescreve rua/bairro/cidade/UF/
  país/CEP, preserva número e complemento; campos em branco do provedor mantêm
  o valor atual).
- **Adapter (`zipcode/ViaCepLookup` + `ViaCepClient`):** `tickethub.zipcode.*`
  (`enabled`, `base-url`, timeouts); `erro:true`/400/desabilitado = vazio.
- **Aplicação:** criação de parceiro/show, alteração e update de endereço do
  parceiro enriquecem antes de persistir. Falha do provedor não quebra o
  cadastro (fail-open, exigido para não travar vendas/cadastros).

## Alternativas consideradas

- **BrasilAPI como provedor principal:** descartada — API agregadora com rate-limit mais agressivo e contrato menos estável que o ViaCEP para o caso simples de CEP→endereço.
- **API dos Correios (oficial):** descartada — exige contrato/chave e tem disponibilidade irregular; ViaCEP gratuito sem chave zera o atrito.
- **OpenCEP/BuscaCEP agregadores pagos:** descartados — custo sem necessidade; o fail-open já tolera a gratuidade do ViaCEP.
- **Enriquecimento no frontend (browser chama ViaCEP direto):** descartado na época — duplicaria a integração em cada cliente e impediria normalização central (`ZipCodeAddress`, `Address.enrichedWith`); a decisão foi revista no ADR-013 com o endpoint dedicado `GET /zipcode`.
- **Fail-closed (rejeitar cadastro se o provedor falhar):** descartado — indisponibilidade do ViaCEP não pode travar vendas/cadastros; contrato fail-open foi exigido.

## Consequências

- **Pró:** um lugar só para evoluir HTTP de saída (retry/CB podem entrar na
  base depois); cobertura sem rede via `MockRestServiceServer` + E2E fail-open
  com provider inalcançável.
- **Contra:** enriquecimento é síncrono na requisição (latência do ViaCEP;
  timeout padrão 5s); sem cache — CEPs repetidos reconsultam.
- **Evolução:** cache com TTL, retry com backoff na base, segundo provedor
  como fallback.
