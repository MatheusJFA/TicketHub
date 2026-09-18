package com.tickethub.infrastructure.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.tickethub.infrastructure.security.Permission;
import com.tickethub.infrastructure.security.Role;
import com.tickethub.infrastructure.security.TestTokens;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public abstract class BaseSteps {

    private static final String ADDRESS = """
            {"street":"Rua Augusta","number":"100","complement":null,\
            "neighborhood":"Centro","city":"São Paulo","state":"SP",\
            "country":"Brasil","zipCode":"01305-000"}\
            """;

    protected void asAdmin() {
        world.token("Bearer " + TestTokens.bearer(jwtSecret, null, authorities(Role.ADMIN)));
    }

    protected void asOwner(final String partnerId) {
        world.token("Bearer " + TestTokens.bearer(jwtSecret, partnerId, authorities(Role.PARTNER)));
    }

    private static String[] authorities(final Role role) {
        return Stream.concat(Stream.of("ROLE_" + role.name()),
                role.permissions().stream().map(Permission::authority)).toArray(String[]::new);
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected World world;

    @Value("${tickethub.security.jwt.secret}")
    protected String jwtSecret;

    protected void asAnonymous() {
        world.token(null);
    }

    protected MockHttpServletRequestBuilder authorized(final MockHttpServletRequestBuilder request) {
        if (world.token() != null) {
            request.header("Authorization", world.token());
        }
        return request;
    }

    protected JsonNode body() throws Exception {
        assertNotNull(world.lastResult(), "no response captured");
        final String content = world.lastResult().getResponse().getContentAsString();
        assertNotNull(content);
        return mapper.readTree(content);
    }

    protected int status() {
        assertNotNull(world.lastResult(), "no response captured");
        return world.lastResult().getResponse().getStatus();
    }

    protected void assertStatus(final int expected) {
        assertEquals(expected, status(),
                () -> "unexpected body: " + uncheckedBody());
    }

    private String uncheckedBody() {
        try {
            return world.lastResult().getResponse().getContentAsString();
        } catch (final Exception e) {
            return "<unreadable>";
        }
    }

    protected MvcResult post(final String url, final String json) throws Exception {
        final var result = mvc.perform(authorized(MockMvcRequestBuilders
                        .post(url).contentType(MediaType.APPLICATION_JSON).content(json)))
                .andReturn();
        world.lastResult(result);
        return result;
    }

    protected MvcResult get(final String url) throws Exception {
        final var result = mvc.perform(authorized(MockMvcRequestBuilders.get(url)))
                .andReturn();
        world.lastResult(result);
        return result;
    }

    protected MvcResult patch(final String url, final String json) throws Exception {
        final var result = mvc.perform(authorized(MockMvcRequestBuilders
                        .patch(url).contentType(MediaType.APPLICATION_JSON).content(json)))
                .andReturn();
        world.lastResult(result);
        return result;
    }

    protected static String addressJson() {
        return ADDRESS;
    }
}
