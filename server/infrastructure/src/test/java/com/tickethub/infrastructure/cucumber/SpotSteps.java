package com.tickethub.infrastructure.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public class SpotSteps extends BaseSteps {

    private void ensureParentSection() throws Exception {
        if (world.sectionId() != null) {
            return;
        }
        asAdmin();
        post("/partners", """
                {"name":"Spot Fixtures","cnpj":"99999999000191","address":%s}\
                """.formatted(addressJson()));
        assertStatus(201);
        final String partnerId = body().get("id").asText();
        post("/shows", """
                {"partnerId":"%s","name":"Spot Fest","description":"Festival",\
                "date":"2027-01-15T20:00:00-03:00","address":%s,"totalSpots":0}\
                """.formatted(partnerId, addressJson()));
        assertStatus(201);
        final String showId = body().get("id").asText();
        post("/sections", """
                {"showId":"%s","name":"Avulsa","description":"Setor",\
                "totalSpots":0,"price":{"value":50.00,"currency":"BRL"}}\
                """.formatted(showId));
        assertStatus(201);
        world.sectionId(body().get("id").asText());
    }

    @Quando("crio o spot sem localização")
    public void crioSpotSemLocalizacao() throws Exception {
        ensureParentSection();
        post("/spots", """
                {"sectionId":"%s"}\
                """.formatted(world.sectionId()));
    }

    @Quando("crio o spot na localização {string}")
    public void crioSpotComLocalizacao(final String location) throws Exception {
        ensureParentSection();
        post("/spots", """
                {"sectionId":"%s","location":"%s"}\
                """.formatted(world.sectionId(), location));
    }

    @Quando("crio o spot na seção inexistente")
    public void crioSpotSecaoInexistente() throws Exception {
        asAdmin();
        post("/spots", """
                {"sectionId":"secao-inexistente","location":"Z9"}\
                """);
    }

    @Quando("guardo o spot criado")
    public void guardoSpot() throws Exception {
        world.spotId(body().get("id").asText());
    }

    @Quando("altero a localização do spot para {string}")
    public void alteroLocalizacao(final String location) throws Exception {
        patch("/spots/" + world.spotId() + "/location", """
                {"location":"%s"}\
                """.formatted(location));
    }

    @Entao("o spot deve ter localização {string}")
    public void spotTemLocalizacao(final String expected) throws Exception {
        get("/spots/" + world.spotId());
        assertStatus(200);
        assertEquals(expected, body().get("location").asText());
    }

    @Entao("o spot deve ter localização gerada")
    public void spotTemLocalizacaoGerada() throws Exception {
        get("/spots/" + world.spotId());
        assertStatus(200);
        final String location = body().get("location").asText();
        assertEquals(true, location.matches("[A-Z]\\d{5}"), "unexpected location: " + location);
        world.spotLocation(location);
    }

    @Dado("que uso o primeiro spot da busca {string}")
    public void primeiroSpotDaBusca(final String term) throws Exception {
        get("/spots?search=" + term + "&page=0&perPage=100");
        assertStatus(200);
        world.spotId(body().get("items").get(0).get("id").asText());
    }

    @Quando("aguardo os spots da busca {string} totalizarem {long}")
    public void aguardoSpots(final String term, final long total) throws Exception {
        final long deadline = System.currentTimeMillis() + 90_000;
        long found = -1;
        while (System.currentTimeMillis() < deadline) {
            get("/spots?search=" + term + "&page=0&perPage=100");
            found = body().get("totalItems").asLong();
            if (found == total) {
                return;
            }
            Thread.sleep(500);
        }
        assertEquals(total, found, "spots did not materialize in time");
    }
}
