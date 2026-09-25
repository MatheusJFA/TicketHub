# ADR-024: Aprovação de parceiros (solicitação pública, ativação pelo master)

- **Status:** Aceito
- **Data:** 2026-09-24
- **Supersede:** o trecho de restrição total da ADR-023

## Contexto

A ADR-023 fechou o `POST /partners` para `ADMIN` (só o master criava
parceiros). Na operação real, o parceiro se cadastra sozinho e o master
aprova — sem reabrir auto-registro com efeito imediato.

## Decisão

- **`Partner.status`:** `PENDING` (padrão no `Partner.create`), `ACTIVE`,
  `REJECTED`, com transições `approve()`/`reject()` só a partir de `PENDING`
  (fora disso, `DomainException`).
- **`POST /partners` público** cria solicitação `PENDING` (filtro `permitAll`
  só no path exato; `POST /partners/{id}/approve` e `/reject` exigem `ADMIN`).
- **Login fechado:** `MongoAuthAccountGateway` só autentica parceiro `ACTIVE`.
  Documentos legados sem `status` são lidos como `ACTIVE` (grandfathering);
  o seed de dev grava o parceiro com `status: "ACTIVE"`.
- **Rejeição:** mantém o documento (trilha/auditoria); o email/CNPJ segue
  único, então nova solicitação com os mesmos dados exige apagar o rejeitado.

## Consequências

- **Pró:** auto-cadastro sem escalação imediata; decisão explícita e auditada
  do master; `Partner` sem dono continua inválido para login (fail-closed).
- **Contra:** fluxo do master em duas chamadas (criar + aprovar) mesmo para
  cadastro direto; rejeitados ocupam email/CNPJ até remoção.
- **Evolução:** fila/notificação de solicitações pendentes; suspensão
  (`ACTIVE` → `SUSPENDED`) reaproveitando as transições.
