#!/usr/bin/env python3
"""Seed de catálogo para desenvolvimento (test-drive do produto).

Cria vários shows com setores publicados via API pública, então o
cache do Redis é invalidado/evitado pelos próprios endpoints (igual ao
uso real). Idempotente por nome: re-rodar não duplica.

Uso:
    python3 scripts/seed-catalog.py [http://localhost:8080]

Requer os usuários do scripts/seed-dev.js (partner + admin).
Nunca rode em produção.
"""

import base64
import json
import sys
import urllib.request

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

JUNK_NAMES = {"Smoke Show", "Show Teste"}

SHOWS = [
    {
        "name": "Rock na Praça",
        "description": "Festival de rock com 5 bandas autorais e praça de alimentação.",
        "date": "2026-11-14T19:00:00-03:00",
        "address": {
            "street": "Praça da Sé",
            "number": "s/n",
            "complement": None,
            "neighborhood": "Sé",
            "city": "São Paulo",
            "state": "SP",
            "country": "Brasil",
            "zipCode": "01001-000",
        },
        "sections": [
            {"name": "Pista", "description": "Em pé, perto do palco", "totalSpots": 60, "price": 80.0},
            {"name": "Camarote", "description": "Área elevada com bar exclusivo", "totalSpots": 20, "price": 220.0},
        ],
    },
    {
        "name": "Samba de Raiz",
        "description": "Roda de samba tradicional com convidados surpresa.",
        "date": "2026-10-24T18:00:00-03:00",
        "address": {
            "street": "Rua do Lavradio",
            "number": "72",
            "complement": None,
            "neighborhood": "Lapa",
            "city": "Rio de Janeiro",
            "state": "RJ",
            "country": "Brasil",
            "zipCode": "20230-070",
        },
        "sections": [
            {"name": "Mesa", "description": "Mesas para 4 pessoas", "totalSpots": 32, "price": 120.0},
            {"name": "Pista", "description": "Em pé na roda", "totalSpots": 50, "price": 45.0},
        ],
    },
    {
        "name": "Festival Jazz e Blues",
        "description": "Três noites de jazz, blues e soul no teatro histórico.",
        "date": "2026-12-05T20:30:00-03:00",
        "address": {
            "street": "Praça Ramos de Azevedo",
            "number": "s/n",
            "complement": None,
            "neighborhood": "República",
            "city": "São Paulo",
            "state": "SP",
            "country": "Brasil",
            "zipCode": "01037-010",
        },
        "sections": [
            {"name": "Plateia", "description": "Cadeiras numeradas", "totalSpots": 48, "price": 150.0},
            {"name": "Balcão", "description": "Vista panorâmica", "totalSpots": 24, "price": 95.0},
        ],
    },
    {
        "name": "Noite Eletrônica Neon",
        "description": "DJs nacionais e internacionais até o amanhecer.",
        "date": "2027-01-16T23:00:00-03:00",
        "address": {
            "street": "Rua Sacadura Cabral",
            "number": "22",
            "complement": "Armazém 22",
            "neighborhood": "Saúde",
            "city": "Rio de Janeiro",
            "state": "RJ",
            "country": "Brasil",
            "zipCode": "20081-261",
        },
        "sections": [
            {"name": "Pista", "description": "Pista geral", "totalSpots": 80, "price": 70.0},
            {"name": "Front Stage", "description": "Em frente à cabine", "totalSpots": 30, "price": 160.0},
            {"name": "Backstage", "description": "Acesso ao backstage", "totalSpots": 10, "price": 400.0},
        ],
    },
    {
        "name": "Orquestra Sinfônica",
        "description": "Clássicos de Beethoven e Villa-Lobos.",
        "date": "2026-11-28T20:00:00-03:00",
        "address": {
            "street": "Praça Júlio Prestes",
            "number": "16",
            "complement": None,
            "neighborhood": "Campos Elíseos",
            "city": "São Paulo",
            "state": "SP",
            "country": "Brasil",
            "zipCode": "01218-020",
        },
        "sections": [
            {"name": "Plateia Central", "description": "Melhor acústica", "totalSpots": 40, "price": 180.0},
            {"name": "Lateral", "description": "Visão lateral", "totalSpots": 24, "price": 110.0},
        ],
    },
    {
        "name": "Comédia Stand-Up",
        "description": "Noite de humor com elenco rotativo.",
        "date": "2026-10-31T21:00:00-03:00",
        "address": {
            "street": "Rua Augusta",
            "number": "1500",
            "complement": "Subsolo",
            "neighborhood": "Consolação",
            "city": "São Paulo",
            "state": "SP",
            "country": "Brasil",
            "zipCode": "01304-100",
        },
        "sections": [
            {"name": "Fileira A-C", "description": "Primeiras fileiras", "totalSpots": 24, "price": 60.0},
            {"name": "Fileira D-F", "description": "Fundo do teatro", "totalSpots": 24, "price": 40.0},
        ],
    },
]


def api(method, path, token=None, body=None):
    req = urllib.request.Request(
        BASE + path,
        data=json.dumps(body).encode() if body is not None else None,
        method=method,
        headers={"Content-Type": "application/json"},
    )
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req) as res:
            raw = res.read().decode()
            return res.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as err:
        detail = err.read().decode()[:300]
        return err.code, {"_error": detail}


def owner_id(access_token):
    payload = access_token.split(".")[1]
    payload += "=" * (-len(payload) % 4)
    return json.loads(base64.urlsafe_b64decode(payload))["ownerId"]


def main():
    _, admin = api("POST", "/auth/login", body={"identifier": "admin@tickethub.local", "password": "admin-local"})
    _, partner = api("POST", "/auth/login", body={"identifier": "partner@tickethub.local", "password": "partner-local"})
    if not admin or "_error" in admin:
        sys.exit(f"login admin falhou (rode scripts/seed-dev.js antes): {admin}")
    if not partner or "_error" in partner:
        sys.exit(f"login partner falhou (rode scripts/seed-dev.js antes): {partner}")
    admin_token, partner_token = admin["accessToken"], partner["accessToken"]
    partner_id = owner_id(partner_token)

    _, listing = api("GET", "/shows?search=&page=0&perPage=100&sort=name&dir=asc")
    existing = {s["name"]: s["id"] for s in listing["items"]}

    for junk in JUNK_NAMES & set(existing):
        code, _ = api("DELETE", f"/shows/{existing[junk]}", token=admin_token)
        print(f"removido show de teste: {junk} (HTTP {code})")
        del existing[junk]

    created, skipped = 0, 0
    for show in SHOWS:
        if show["name"] in existing:
            print(f"já existe, pulando: {show['name']}")
            skipped += 1
            continue
        code, res = api("POST", "/shows", token=partner_token, body={
            "partnerId": partner_id,
            "name": show["name"],
            "description": show["description"],
            "date": show["date"],
            "address": show["address"],
        })
        assert code in (200, 201), f"criar show falhou: {code} {res}"
        show_id = res["id"]
        for sec in show["sections"]:
            code, res = api("POST", f"/shows/{show_id}/sections", token=partner_token, body={
                "name": sec["name"],
                "description": sec["description"],
                "totalSpots": sec["totalSpots"],
                "price": {"value": sec["price"], "currency": "BRL"},
            })
            assert code in (200, 201), f"criar setor falhou: {code} {res}"
        # NOTA: POST /shows/{id}/sections retorna o id do SHOW, não do setor;
        # por isso listamos os setores para obter os ids reais.
        _, sec_list = api("GET", f"/shows/{show_id}/sections?page=0&perPage=50", token=partner_token)
        for sec_doc in sec_list["items"]:
            code, _ = api("POST", f"/sections/{sec_doc['id']}/publish-all", token=partner_token, body={})
            assert code == 200, f"publicar setor falhou: {code}"
        code, _ = api("POST", f"/shows/{show_id}/publish-all", token=partner_token, body={})
        assert code == 200, f"publicar show falhou: {code}"
        print(f"criado e publicado: {show['name']}")
        created += 1

    _, listing = api("GET", "/shows?search=&page=0&perPage=100&sort=name&dir=asc")
    public = [s for s in listing["items"] if s["published"]]
    print(f"\nresumo: {created} criados, {skipped} já existiam, {len(public)} publicados no total")


if __name__ == "__main__":
    main()
