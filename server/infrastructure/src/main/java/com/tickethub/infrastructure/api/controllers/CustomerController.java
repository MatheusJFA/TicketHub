package com.tickethub.infrastructure.api.controllers;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.application.customer.changename.*;
import com.tickethub.application.customer.create.*;
import com.tickethub.application.customer.delete.*;
import com.tickethub.application.customer.retrieve.get.*;
import com.tickethub.application.customer.retrieve.list.*;
import com.tickethub.infrastructure.customer.models.*;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.CustomerAPI;

import java.net.URI;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController implements CustomerAPI {
    private final ChangeCustomerNameUseCase changeCustomerName;
    private final CreateCustomerUseCase createCustomer;
    private final DeleteCustomerUseCase deleteCustomer;
    private final GetCustomerUseCase getCustomer;
    private final ListCustomersUseCase listCustomers;

    public CustomerController(ChangeCustomerNameUseCase changeCustomerName,
            CreateCustomerUseCase createCustomer,
            DeleteCustomerUseCase deleteCustomer,
            GetCustomerUseCase getCustomer,
            ListCustomersUseCase listCustomers) {
        this.changeCustomerName = changeCustomerName;
        this.createCustomer = createCustomer;
        this.deleteCustomer = deleteCustomer;
        this.getCustomer = getCustomer;
        this.listCustomers = listCustomers;
    }

    @Override
    public ResponseEntity<?> changeCustomerName(String id, ChangeCustomerNameRequest input) {
        final var command = new ChangeCustomerNameCommand(
                id,
                input.name()
        );
        return changeCustomerName.execute(command)
                .<ResponseEntity<?>>fold(notification -> ResponseEntity.unprocessableEntity().body(notification),
                        output -> ResponseEntity.ok(new IdResponse(output.id())));
    }

    @Override
    public ResponseEntity<?> createCustomer(CreateCustomerRequest input) {
        final var command = new CreateCustomerCommand(
                input.cpf(),
                input.name()
        );
        return createCustomer.execute(command)
                .<ResponseEntity<?>>fold(notification -> ResponseEntity.unprocessableEntity().body(notification),
                        output -> ResponseEntity.created(URI.create("/customers/" + output.id())).body(new IdResponse(output.id())));
    }

    @Override
    public ResponseEntity<?> deleteById(String id) {
        return deleteCustomer.execute(id)
                .<ResponseEntity<?>>fold(notification -> ResponseEntity.unprocessableEntity().body(notification),
                        output -> ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<?> getById(String id) {
        return getCustomer.execute(id)
                .<ResponseEntity<?>>fold(notification -> ResponseEntity.unprocessableEntity().body(notification),
                        output -> ResponseEntity.ok(CustomerResponse.from(output)));
    }

    @Override
    public ResponseEntity<?> list(String search, int page, int perPage, String sort, String direction) {
        return listCustomers.execute(new SearchQuery(page, perPage, search, sort, direction))
                .<ResponseEntity<?>>fold(notification -> ResponseEntity.unprocessableEntity().body(notification),
                        output -> ResponseEntity.ok(output.map(CustomerListResponse::from)));
    }
}
