# language: pt
@e2eTest
Funcionalidade: Gestão de spots
  Como operador quero criar e relocalizar spots,
  com código gerado quando a localização não é informada.

  Cenário: Criar spot sem localização gera código curto
    Dado que sou o administrador
    Quando crio o spot sem localização
    Então a resposta deve ter status 201
    Quando guardo o spot criado
    Então o spot deve ter localização gerada

  Cenário: Relocalizar spot próprio
    Dado que sou o administrador
    Quando crio o spot na localização "A1"
    E guardo o spot criado
    E altero a localização do spot para "B2"
    Então a resposta deve ter status 200
    E o spot deve ter localização "B2"

  Cenário: Spot de show alheio é protegido por dono
    Dado que existe o parceiro "Rock Produções" com cnpj "11222333000181"
    E que estou autenticado como dono do parceiro "Rock Produções"
    Quando crio o show "Rock in Rio" do parceiro "Rock Produções"
    E guardo o show criado
    E adiciono a seção "Pista" com 3 lugares
    E que uso o primeiro spot da busca "A0000"
    E que estou autenticado como outro parceiro
    Quando altero a localização do spot para "Z9"
    Então a resposta deve ter status 403
