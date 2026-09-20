# ADR-012: Validação de ingresso via QR code na porta

- **Status:** Aceito
- **Data:** 2026-09-20

## Contexto

Na porta do show, um segurança escaneia o QR code do convidado e precisa
responder rápido: o ingresso existe mesmo para aquele show? Sem isso, um QR
de outro show/setor (ou inventado) poderia passar. Além de conferir, a
leitura deve dar baixa no ingresso para impedir reuso do mesmo QR.

## Decisão

- **QR composto:** o payload carrega `showId:sectionId:spotId`; o app do
  segurança faz o parse e chama `POST /shows/{showId}/tickets/validate`
  com `{sectionId, spotId}`.
- **Aplicação (`ticket/validate/DefaultValidateTicketUseCase`):** carrega o
  spot com seus vínculos (`SpotGateway.findPlacement`, lidos do
  `SpotDocument` desnormalizado: `showId`/`sectionId`), confere o vínculo
  ("Spot does not belong to the given show and section"), confere a data
  (check-in só no dia do show, no fuso do show: "Show is outside the
  check-in date") e dá baixa via `Spot.checkIn()` (segunda leitura falha
  com "Spot is already used").
- **Domínio (`Spot.checkIn`):** marca `available=false`; reler um spot já
  usado lança `DomainException`.
- **Segurança:** nova autoridade `ticket:validate` (partner + admin) +
  `@showAccess.canWrite(#showId)` — o segurança só valida shows do próprio
  parceiro.
- **Respostas:** 200 válido (detalhes + `checkedInAt`), 404 spot/show
  inexistente, 422 vínculo/data/reuso.

## Consequências

- **Pró:** validação e baixa atômicas no caso de uso; sem estado novo no
  domínio (reusa `available`); data comparada no fuso do show.
- **Contra:** tolerância zero de data (só o dia do show); show sem data
  nunca valida — virar regra de negócio explícita se necessário.
- **Evolução:** idempotência por dispositivo, janela de tolerância
  (ex.: ±horas), trilha de auditoria de check-ins.
