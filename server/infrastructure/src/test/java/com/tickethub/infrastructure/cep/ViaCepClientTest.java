package com.tickethub.infrastructure.cep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tickethub.infrastructure.shared.http.BaseHttpClient;

class ViaCepClientTest {

    private record Fixture(ViaCepClient client, MockRestServiceServer server) {
    }

    private Fixture fixture() {
        final var builder = BaseHttpClient.preparedBuilder(RestClient.builder(), "https://viacep.com.br");
        final var server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(new ViaCepClient(builder.build()), server);
    }

    @Test
    void givenKnownCep_whenFindByCep_thenReturnsAddressData() {
        final var fixture = fixture();
        fixture.server().expect(requestTo("https://viacep.com.br/ws/01305000/json/"))
                .andRespond(withSuccess("""
                        {"cep":"01305-000","logradouro":"Rua Augusta","complemento":"",\
                        "bairro":"Centro","localidade":"São Paulo","uf":"SP"}\
                        """, MediaType.APPLICATION_JSON));

        final var response = fixture.client().findByCep("01305000").orElseThrow();

        assertEquals("Rua Augusta", response.logradouro());
        assertEquals("Centro", response.bairro());
        assertEquals("São Paulo", response.localidade());
        assertEquals("SP", response.uf());
        fixture.server().verify();
    }

    @Test
    void givenUnknownCep_whenFindByCep_thenReturnsErrorFlag() {
        final var fixture = fixture();
        fixture.server().expect(requestTo("https://viacep.com.br/ws/00000000/json/"))
                .andRespond(withSuccess("{\"erro\":true}", MediaType.APPLICATION_JSON));

        final var response = fixture.client().findByCep("00000000").orElseThrow();

        assertTrue(response.hasError());
        fixture.server().verify();
    }

    @Test
    void givenMalformedCep_whenFindByCep_thenReturnsEmpty() {
        final var fixture = fixture();
        fixture.server().expect(requestTo("https://viacep.com.br/ws/123/json/"))
                .andRespond(withBadRequest());

        assertTrue(fixture.client().findByCep("123").isEmpty());
        fixture.server().verify();
    }

    @Test
    void givenProviderOutage_whenFindByCep_thenReturnsEmpty() {
        final var fixture = fixture();
        fixture.server().expect(requestTo("https://viacep.com.br/ws/01305000/json/"))
                .andRespond(withServerError());

        assertTrue(fixture.client().findByCep("01305000").isEmpty());
        fixture.server().verify();
    }
}
