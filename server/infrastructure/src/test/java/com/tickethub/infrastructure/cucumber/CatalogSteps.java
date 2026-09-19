package com.tickethub.infrastructure.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public class CatalogSteps extends BaseSteps {

    @Dado("que sou o administrador")
    public void souAdministrador() {
        asAdmin();
    }

    @Dado("que estou sem credenciais")
    public void semCredenciais() {
        asAnonymous();
    }

    @Dado("que existe o parceiro {string} com cnpj {string}")
    public void existeParceiro(final String name, final String cnpj) throws Exception {
        asAdmin();
        final String json = """
                {"name":"%s","cnpj":"%s","address":%s}\
                """.formatted(name, cnpj, addressJson());
        post("/partners", json);
        assertStatus(201);
        world.partners().put(name, body().get("id").asText());
    }

    @Dado("que estou autenticado como dono do parceiro {string}")
    public void autenticadoComoDono(final String name) {
        final String partnerId = world.partners().get(name);
        assertTrue(partnerId != null, "unknown partner: " + name);
        world.currentPartner(name);
        asOwner(partnerId);
    }

    @Dado("que estou autenticado como outro parceiro")
    public void autenticadoComoOutro() {
        world.currentPartner(null);
        asOwner("parceiro-estrangeiro");
    }

    @Quando("crio o show {string} do parceiro {string}")
    public void crioShow(final String show, final String partner) throws Exception {
        final String partnerId = world.partners().get(partner);
        final String json = """
                {"partnerId":"%s","name":"%s","description":"Festival",\
                "date":"2027-01-15T20:00:00-03:00","address":%s,"totalSpots":0}\
                """.formatted(partnerId, show, addressJson());
        post("/shows", json);
    }

    @Quando("adiciono a seção {string} com {long} lugares")
    public void adicionoSecao(final String section, final long spots) throws Exception {
        final String json = """
                {"name":"%s","description":"Setor",\
                "totalSpots":%d,"price":{"value":50.00,"currency":"BRL"}}\
                """.formatted(section, spots);
        post("/shows/" + world.showId() + "/sections", json);
    }

    @Quando("crio a seção avulsa do show inexistente")
    public void crioSecaoShowInexistente() throws Exception {
        asAdmin();
        post("/sections", """
                {"showId":"show-inexistente","name":"Avulsa","description":"Setor",\
                "totalSpots":0,"price":{"value":50.00,"currency":"BRL"}}\
                """);
    }

    @Quando("guardo o show criado")
    public void guardoShow() throws Exception {
        world.showId(body().get("id").asText());
    }

    @Quando("publico tudo do show")
    public void publicoTudo() throws Exception {
        post("/shows/" + world.showId() + "/publish-all", "{}");
    }

    @Entao("a resposta deve ter status {int}")
    public void respostaStatus(final int status) {
        assertStatus(status);
    }

    @Entao("o show deve estar publicado")
    public void showPublicado() throws Exception {
        get("/shows/" + world.showId());
        assertStatus(200);
        assertEquals(true, body().get("published").asBoolean());
    }

    @Entao("o catálogo deve listar {long} seções")
    public void catalogoSecoes(final long total) throws Exception {
        get("/sections?page=0&perPage=100");
        assertStatus(200);
        assertEquals(total, body().get("totalItems").asLong());
    }

    @Entao("a busca por spots {string} deve retornar {long}")
    public void buscaSpots(final String term, final long total) throws Exception {
        get("/spots?search=" + term + "&page=0&perPage=100");
        assertStatus(200);
        assertEquals(total, body().get("totalItems").asLong());
    }

    @Quando("busco o show inexistente")
    public void buscoShowInexistente() throws Exception {
        get("/shows/show-inexistente");
    }

    @Quando("crio o show sem nome")
    public void crioShowSemNome() throws Exception {
        final String partnerId = world.partners().values().iterator().next();
        final String json = """
                {"partnerId":"%s","name":"A","description":"Festival",\
                "date":"2027-01-15T20:00:00-03:00","address":%s,"totalSpots":0}\
                """.formatted(partnerId, addressJson());
        post("/shows", json);
    }
}
