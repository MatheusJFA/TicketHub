package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.Either;
import com.tickethub.application.customer.changename.ChangeCustomerNameOutput;
import com.tickethub.application.customer.changename.ChangeCustomerNameUseCase;
import com.tickethub.application.customer.create.CreateCustomerCommand;
import com.tickethub.application.customer.create.CreateCustomerOutput;
import com.tickethub.application.customer.create.CreateCustomerUseCase;
import com.tickethub.application.customer.delete.DeleteCustomerUseCase;
import com.tickethub.application.customer.retrieve.get.GetCustomerUseCase;
import com.tickethub.application.customer.retrieve.list.ListCustomersUseCase;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.security.OwnerAccess;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.tickethub.infrastructure.shared.presenters.SharedMapperImpl;
import com.tickethub.infrastructure.customer.presenters.CustomerMapperImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.tickethub.infrastructure.security.TestTokens;
import org.springframework.beans.factory.annotation.Value;

@ControllerTest(controllers = CustomerController.class)
@Import({SharedMapperImpl.class, CustomerMapperImpl.class})
class CustomerControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ChangeCustomerNameUseCase changeCustomerName;
    @MockitoBean CreateCustomerUseCase createCustomer;
    @MockitoBean DeleteCustomerUseCase deleteCustomer;
    @MockitoBean GetCustomerUseCase getCustomer;
    @MockitoBean ListCustomersUseCase listCustomers;
    @MockitoBean(name = "ownerAccess") OwnerAccess ownerAccess;

    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }

    @Test
    void givenAValidCommand_whenCallsCreateCustomer_shouldReturnCustomerId() throws Exception {
        when(createCustomer.execute(any())).thenReturn(Either.right(new CreateCustomerOutput("customer-1")));

        final var response = mvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cpf\":\"52998224725\",\"name\":\"Maria\"}"));

        response.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/customers/customer-1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("customer-1"));
        verify(createCustomer).execute(new CreateCustomerCommand("52998224725", "Maria"));
    }

    @Test
    void givenAnInvalidCommand_whenCallsCreateCustomer_shouldReturnNotification() throws Exception {
        when(createCustomer.execute(any())).thenReturn(Either.left(Notification.create(new Error("Invalid CPF"))));

        mvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"x\",\"name\":\"Maria\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].message").value("Invalid CPF"));
    }

    @Test
    void givenAValidCommand_whenCallsChangeName_shouldReturnCustomerId() throws Exception {
        when(changeCustomerName.execute(any())).thenReturn(Either.right(new ChangeCustomerNameOutput("customer-1")));
        when(ownerAccess.isSelfOrAdmin("customer-1")).thenReturn(true);

        mvc.perform(patch("/customers/customer-1/name").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer("customer-1", "customer:write"))
                        .content("{\"name\":\"Maria Silva\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("customer-1"));
    }

    @Test
    void givenAValidId_whenCallsDeleteCustomer_shouldReturnNoContent() throws Exception {
        when(deleteCustomer.execute("customer-1")).thenReturn(Optional.empty());
        when(ownerAccess.isSelfOrAdmin("customer-1")).thenReturn(true);

        mvc.perform(delete("/customers/customer-1").header("Authorization", bearer("customer-1", "customer:delete"))).andExpect(status().isNoContent());
        verify(deleteCustomer).execute("customer-1");
    }

    @Test
    void givenValidParams_whenCallsListCustomers_shouldReturnCustomers() throws Exception {
        when(listCustomers.execute(any())).thenReturn(Either.right(new Pagination<>(0, 10, 0, List.of())));

        mvc.perform(get("/customers").header("Authorization", bearer(null, "ROLE_ADMIN")).param("search", "maria").param("sort", "name").param("dir", "asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void givenAnotherAccount_whenCallsDeleteCustomer_thenReturnsForbidden() throws Exception {
        mvc.perform(delete("/customers/customer-1")
                        .header("Authorization", bearer("customer-9", "customer:delete")))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAdmin_whenCallsDeleteAnotherCustomer_thenReturnsNoContent() throws Exception {
        when(deleteCustomer.execute("customer-1")).thenReturn(Optional.empty());

        when(ownerAccess.isSelfOrAdmin("customer-1")).thenReturn(true);

        mvc.perform(delete("/customers/customer-1").header("Authorization", bearer(null, "ROLE_ADMIN", "customer:delete")))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenCustomer_whenCallsListCustomers_thenReturnsForbidden() throws Exception {
        mvc.perform(get("/customers").header("Authorization", bearer("customer-1", "customer:write")))
                .andExpect(status().isForbidden());
    }
}
