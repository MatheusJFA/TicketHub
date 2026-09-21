package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tickethub.application.Either;
import com.tickethub.application.ticket.validate.ValidateTicketOutput;
import com.tickethub.application.ticket.validate.ValidateTicketUseCase;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.security.ShowAccess;
import com.tickethub.infrastructure.security.TestTokens;
import com.tickethub.domain.validation.Error;

@ControllerTest(controllers = TicketController.class)
@Import({})
@DisplayName("Ticket controller")
class TicketControllerTest {
    @Autowired MockMvc mvc;
    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, authorities);
    }

    private String bearerAsOwner(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }

    @MockitoBean ValidateTicketUseCase validateTicket;
    @MockitoBean(name = "showAccess")
    ShowAccess showAccess;

    @Test
    @DisplayName("Given valid QR data, when validates ticket, then returns checked-in ticket")
    void givenValidQrData_whenValidatesTicket_thenReturnsCheckedInTicket() throws Exception {
        final var output = new ValidateTicketOutput("show-1", "ticket-1", "ABCDEFGH", "order-1",
                "spot-1", "A00001", OffsetDateTime.parse("2027-01-15T20:00:00-03:00"),
                Instant.parse("2027-01-15T22:00:00Z"));
        when(showAccess.canWrite("show-1")).thenReturn(true);
        when(validateTicket.execute(any())).thenReturn(Either.right(output));

        mvc.perform(post("/shows/show-1/tickets/validate")
                        .header("Authorization", bearerAsOwner("partner-1", "ticket:validate"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":\"ticket-1\",\"code\":\"ABCDEFGH\",\"signature\":\"sig\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value("ticket-1"))
                .andExpect(jsonPath("$.showId").value("show-1"))
                .andExpect(jsonPath("$.spotId").value("spot-1"));
    }

    @Test
    @DisplayName("Given non owner, when validates ticket, then returns forbidden")
    void givenNonOwner_whenValidatesTicket_thenReturnsForbidden() throws Exception {
        when(showAccess.canWrite("show-1")).thenReturn(false);

        mvc.perform(post("/shows/show-1/tickets/validate")
                        .header("Authorization", bearerAsOwner("partner-9", "ticket:validate"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":\"ticket-1\",\"code\":\"ABCDEFGH\",\"signature\":\"sig\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Given missing code, when validates ticket, then returns bad request")
    void givenMissingCode_whenValidatesTicket_thenReturnsBadRequest() throws Exception {
        when(showAccess.canWrite("show-1")).thenReturn(true);

        mvc.perform(post("/shows/show-1/tickets/validate")
                        .header("Authorization", bearerAsOwner("partner-1", "ticket:validate"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":\"ticket-1\",\"signature\":\"sig\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Given unknown ticket, when validates ticket, then returns not found")
    void givenUnknownTicket_whenValidatesTicket_thenReturnsNotFound() throws Exception {
        when(showAccess.canWrite("show-1")).thenReturn(true);
        when(validateTicket.execute(any())).thenReturn(Either.left(
                Notification.create(new Error("Ticket not found: ticket-1"))));

        mvc.perform(post("/shows/show-1/tickets/validate")
                        .header("Authorization", bearerAsOwner("partner-1", "ticket:validate"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":\"ticket-1\",\"code\":\"ABCDEFGH\",\"signature\":\"sig\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Given used ticket, when validates ticket, then returns unprocessable")
    void givenUsedTicket_whenValidatesTicket_thenReturnsUnprocessable() throws Exception {
        when(showAccess.canWrite("show-1")).thenReturn(true);
        when(validateTicket.execute(any())).thenReturn(Either.left(
                Notification.create(new Error("Ticket is already used"))));

        mvc.perform(post("/shows/show-1/tickets/validate")
                        .header("Authorization", bearerAsOwner("partner-1", "ticket:validate"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":\"ticket-1\",\"code\":\"ABCDEFGH\",\"signature\":\"sig\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Given no token, when validates ticket, then returns unauthorized")
    void givenNoToken_whenValidatesTicket_thenReturnsUnauthorized() throws Exception {
        mvc.perform(post("/shows/show-1/tickets/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketId\":\"ticket-1\",\"code\":\"ABCDEFGH\",\"signature\":\"sig\"}"))
                .andExpect(status().isUnauthorized());
    }
}
