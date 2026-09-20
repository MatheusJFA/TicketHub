package com.tickethub.infrastructure.zipcode;

import java.util.Map;
import java.util.Optional;

import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tickethub.infrastructure.shared.http.BaseHttpClient;

public class ViaCepClient extends BaseHttpClient {

    public ViaCepClient(final RestClient restClient) {
        super(restClient, "viacep");
    }

    public Optional<ViaCepResponse> findByCep(final String digits) {
        return getOptional("/ws/{cep}/json/", Map.of("cep", digits), ViaCepResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ViaCepResponse(String cep, String logradouro, String complemento, String bairro,
            String localidade, String uf, Boolean erro) {

        public boolean hasError() {
            return Boolean.TRUE.equals(erro);
        }
    }
}
