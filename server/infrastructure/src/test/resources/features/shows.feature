# language: pt
@e2eTest
Funcionalidade: Gestão de shows
  Como parceiro quero montar meu show com seções e publicá-lo,
  sem que outros parceiros alterem meu catálogo.

  Cenário: Criar show com seção pequena e publicar tudo
    Dado que existe o parceiro "Rock Produções" com cnpj "11222333000181"
    E que estou autenticado como dono do parceiro "Rock Produções"
    Quando crio o show "Rock in Rio" do parceiro "Rock Produções"
    Então a resposta deve ter status 201
    Quando guardo o show criado
    E adiciono a seção "Pista" com 3 lugares
    Então a resposta deve ter status 200
    Quando publico tudo do show
    Então a resposta deve ter status 200
    E o show deve estar publicado
    E o catálogo deve listar 1 seções
    E a busca por spots "A0000" deve retornar 3

  Cenário: Outro parceiro não altera show alheio
    Dado que existe o parceiro "Rock Produções" com cnpj "11222333000181"
    E que estou autenticado como dono do parceiro "Rock Produções"
    Quando crio o show "Rock in Rio" do parceiro "Rock Produções"
    E guardo o show criado
    E que estou autenticado como outro parceiro
    Quando publico tudo do show
    Então a resposta deve ter status 403

  Cenário: Catálogo é público e erros são uniformes
    Dado que estou sem credenciais
    Quando busco o show inexistente
    Então a resposta deve ter status 404
    Dado que existe o parceiro "Rock Produções" com cnpj "11222333000181"
    E que estou autenticado como dono do parceiro "Rock Produções"
    Quando crio o show sem nome
    Então a resposta deve ter status 422
    Dado que estou sem credenciais
    Quando crio o show "Rock in Rio" do parceiro "Rock Produções"
    Então a resposta deve ter status 401

  Cenário: Criar seção avulsa de show inexistente retorna 404
    Dado que sou o administrador
    Quando crio a seção avulsa do show inexistente
    Então a resposta deve ter status 404
