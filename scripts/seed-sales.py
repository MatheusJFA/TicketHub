#!/usr/bin/env python3
"""Simula vendas no catálogo de dev: ocupa assentos e contadores com níveis
variados (vazio, parcial, quase lotado, LOTADO) + 1 pedido PAID com ingressos
para customer@tickethub.local.

Idempotente: contadores são recalculados a partir dos spots; re-rodar mantém.
Nunca rode em produção.
"""

import subprocess

PLAN = {
    ("Rock na Praça", "Pista"): 45,
    ("Rock na Praça", "Camarote"): 20,      # LOTADO
    ("Samba de Raiz", "Mesa"): 10,
    ("Samba de Raiz", "Pista"): 48,         # quase lotado
    ("Festival Jazz e Blues", "Plateia"): 5,
    ("Festival Jazz e Blues", "Balcão"): 12,
    ("Noite Eletrônica Neon", "Pista"): 80,  # LOTADO
    ("Noite Eletrônica Neon", "Front Stage"): 22,
    ("Noite Eletrônica Neon", "Backstage"): 3,
    ("Orquestra Sinfônica", "Plateia Central"): 40,  # LOTADO
    ("Orquestra Sinfônica", "Lateral"): 0,
    ("Comédia Stand-Up", "Fileira A-C"): 18,
    ("Comédia Stand-Up", "Fileira D-F"): 6,
}

MONGO = [
    "docker", "compose", "exec", "-T", "mongo", "mongosh",
    "-u", "tickethub", "-p", "tickethub-local",
    "--authenticationDatabase", "admin", "tickethub", "--quiet", "--eval",
]


def run(js):
    r = subprocess.run(MONGO + [js], capture_output=True, text=True)
    if r.returncode != 0:
        raise SystemExit(f"mongo falhou: {r.stderr[:500]}")
    return r.stdout.strip()


JS = """
const plan = %s;
for (const [key, wanted] of Object.entries(plan)) {
  const [showName, sectionName] = key.split("||");
  const show = db.shows.findOne({name: showName});
  const sec = db.sections.findOne({showId: show._id, name: sectionName});
  const avail = db.spots.find({sectionId: sec._id, available: true, published: true})
    .sort({location: 1}).toArray();
  const need = Math.max(0, wanted - db.spots.countDocuments({sectionId: sec._id, available: false}));
  for (const s of avail.slice(0, need)) {
    db.spots.updateOne({_id: s._id}, {$set: {available: false, reserved: true}});
  }
  const sold = db.spots.countDocuments({sectionId: sec._id, available: false});
  db.sections.updateOne({_id: sec._id}, {$set: {totalSpotsSold: sold}});
}
for (const showName of [...new Set(Object.keys(plan).map(k => k.split("||")[0]))]) {
  const show = db.shows.findOne({name: showName});
  const secs = db.sections.find({showId: show._id}).toArray();
  const sold = secs.reduce((a, s) => a + (s.totalSpotsSold || 0), 0);
  db.shows.updateOne({_id: show._id}, {$set: {totalSpotsSold: sold}});
  print(showName + ": " + sold + " vendidos");
}
// pedido PAID + ingressos de vitrine para customer@ (assinatura fake: só leitura)
const cust = db.customers.findOne({email: "customer@tickethub.local"});
if (!db.orders.findOne({_id: "dev-paid-order-1"})) {
  const rock = db.shows.findOne({name: "Rock na Praça"});
  const pista = db.sections.findOne({showId: rock._id, name: "Pista"});
  const two = db.spots.find({sectionId: pista._id, available: false}).limit(2).toArray();
  db.orders.insertOne({_id: "dev-paid-order-1", customerId: cust._id,
    items: two.map(s => ({spotId: s._id, price: {value: NumberDecimal("80.00"), currency: "BRL"}})),
    total: {value: NumberDecimal("160.00"), currency: "BRL"},
    status: "PAID", expiresAt: new Date("2027-06-01T00:00:00Z"), chargeId: "ch_dev_fixture",
    createdAt: new Date(), updatedAt: new Date(), createdBy: "seed-sales", lastModifiedBy: "seed-sales",
    _class: "com.tickethub.infrastructure.order.persistence.OrderDocument"});
  const tclass = db.tickets.findOne()._class;
  two.forEach((s, i) => db.tickets.insertOne({_id: "dev-ticket-" + (i+1), orderId: "dev-paid-order-1",
    spotId: s._id, customerId: cust._id, code: "DEV123" + i, signature: "dev-fake",
    status: "ISSUED", createdAt: new Date(), updatedAt: new Date(), deletedAt: null,
    createdBy: "seed-sales", lastModifiedBy: "seed-sales", _class: tclass}));
  print("pedido dev-paid-order-1 + 2 ingressos criados");
} else {
  print("pedido dev já existe");
}
""" % ("{\n" + ",\n".join(f'  "{s}||{sec}": {n}' for (s, sec), n in PLAN.items()) + "\n}")

print(run(JS))

# limpa o cache de catálogo para a API servir os números novos na hora
r = subprocess.run(
    ["docker", "exec", "tickethub-redis-1", "redis-cli", "--scan", "--pattern", "*shows*"],
    capture_output=True, text=True)
keys = [k for k in r.stdout.split() if k]
if keys:
    subprocess.run(["docker", "exec", "tickethub-redis-1", "redis-cli", "del", *keys],
                   capture_output=True)
    print(f"cache redis limpo: {len(keys)} chaves")
else:
    print("cache redis já vazio")
