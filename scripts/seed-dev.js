// Carga inicial de desenvolvimento: garante operator, customer e partner com
// credenciais conhecidas para login em POST /auth/login. Idempotente —
// pode rodar quantas vezes quiser.
// O login é 100% via Mongo (operators/customers/partners); não há mais
// usuários bootstrap no application.yml (tickethub.security.users removido).
//
// Se o volume já tiver um documento com o mesmo CPF/CNPJ sob outro email
// (ex.: dados de smoke test), o script adota esse documento e atualiza
// email/senha para os valores conhecidos abaixo, em vez de falhar com
// duplicate key.
//
// Uso (com o compose no ar):
//   docker compose exec -T mongo mongosh \
//     -u "$MONGO_USERNAME" -p "$MONGO_PASSWORD" --authenticationDatabase admin \
//     tickethub < scripts/seed-dev.js
// Com os defaults do compose (credenciais padrão de dev):
//   docker compose exec -T mongo mongosh \
//     -u tickethub -p tickethub-local --authenticationDatabase admin \
//     tickethub < scripts/seed-dev.js
//
// Logins garantidos (todos via Mongo, sem bootstrap no application.yml):
//   admin@tickethub.local    / admin-local     (role ADMIN, collection operators)
//   customer@tickethub.local / customer-local  (role CUSTOMER, collection customers)
//   partner@tickethub.local  / partner-local   (role PARTNER, collection partners)
// Hashes bcrypt de dev (nunca use em produção):
//   admin-local    -> $2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS
//   customer-local -> $2a$10$8oOcWuB1hV9DFQk73vuOr.4NE22eOqPObY/YeQBli2zYPuo4he2d2
//   partner-local  -> $2a$10$yd53Z0ydlKB9/DaDw/jHFO8eWLtgbWsNjjYH9eSY1dvKPRBkC2B3a

db = db.getSiblingDB("tickethub");

const now = new Date();

// UUID v4 textual (xxxxxxxx-xxxx-...). NÃO usar hex de ObjectId (24 chars):
// o Spring Data converte ids de 24 hex em ObjectId no findById e o lookup
// por _id falha para documentos seedados (login funciona via findByEmail,
// mas GET /customers/{id}, create-show etc. retornam 404).
function uuid() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = Math.floor(Math.random() * 16);
    return ((c === "x" ? r : (r & 0x3) | 0x8)).toString(16);
  });
}

function ensureAccount(collection, identityFilter, fixedFields, credentialFields) {
  const existing = collection.findOne({ $or: identityFilter });
  if (existing) {
    if (/^[0-9a-f]{24}$/i.test(existing._id)) {
      // Id legado quebrado (hex de ObjectId): recria com UUID.
      collection.deleteOne({ _id: existing._id });
      print(`${collection.getName()}: removed legacy id=${existing._id} email=${credentialFields.email}`);
    } else {
      collection.updateOne(
        { _id: existing._id },
        { $set: { ...credentialFields, updatedAt: now, lastModifiedBy: "seed-dev" } }
      );
      print(`${collection.getName()}: kept id=${existing._id} email=${credentialFields.email}`);
      return;
    }
  }
  const id = uuid();
  collection.insertOne({
    _id: id,
    ...fixedFields,
    ...credentialFields,
    createdAt: now,
    updatedAt: now,
    deletedAt: null,
    createdBy: "seed-dev",
    lastModifiedBy: "seed-dev",
  });
  print(`${collection.getName()}: created id=${id} email=${credentialFields.email}`);
}

ensureAccount(
  db.operators,
  [{ email: "admin@tickethub.local" }],
  { name: "Admin Local" },
  {
    email: "admin@tickethub.local",
    // senha: admin-local
    passwordHash: "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS",
  }
);

ensureAccount(
  db.customers,
  [{ email: "customer@tickethub.local" }, { cpf: "52998224725" }],
  { cpf: "52998224725", name: "Maria Silva" },
  {
    email: "customer@tickethub.local",
    // senha: customer-local
    passwordHash: "$2a$10$8oOcWuB1hV9DFQk73vuOr.4NE22eOqPObY/YeQBli2zYPuo4he2d2",
  }
);

ensureAccount(
  db.partners,
  [{ email: "partner@tickethub.local" }, { cnpj: "04252011000110" }],
  {
    name: "Cinema Nova",
    cnpj: "04252011000110",
    // Solicitações via POST /partners nascem PENDING; o seed grava direto como ACTIVE.
    status: "ACTIVE",
    address: {
      street: "Rua Augusta",
      number: "100",
      complement: "Sala 10",
      neighborhood: "Centro",
      city: "São Paulo",
      state: "SP",
      country: "Brasil",
      zipCode: "01305-000",
    },
  },
  {
    email: "partner@tickethub.local",
    // senha: partner-local
    passwordHash: "$2a$10$yd53Z0ydlKB9/DaDw/jHFO8eWLtgbWsNjjYH9eSY1dvKPRBkC2B3a",
  }
);

// Garante o seed como ACTIVE mesmo em volume pré-existente (o update acima só toca credenciais).
db.partners.updateOne(
  { email: "partner@tickethub.local" },
  { $set: { status: "ACTIVE", updatedAt: now, lastModifiedBy: "seed-dev" } }
);
