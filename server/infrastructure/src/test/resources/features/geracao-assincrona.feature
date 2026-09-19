# language: pt
@e2eTest
Funcionalidade: Geração assíncrona de spots
  Como parceiro de um grande show quero adicionar seções enormes
  sem travar a requisição; os spots nascem em segundo plano via Kafka.

  Cenário: Seção grande materializa spots em lote
    Dado que existe o parceiro "Mega Eventos" com cnpj "11222333000181"
    E que estou autenticado como dono do parceiro "Mega Eventos"
    Quando crio o show "Mega Fest" do parceiro "Mega Eventos"
    E guardo o show criado
    E adiciono a seção "Pista" com 3 lugares
    E adiciono a seção "Arena" com 1500 lugares
    Então a resposta deve ter status 200
    E a busca por spots "" deve retornar 3
    Quando aguardo os spots da busca "" totalizarem 1503
    Então a busca por spots "B00001" deve retornar 1
    E a busca por spots "B01500" deve retornar 1
