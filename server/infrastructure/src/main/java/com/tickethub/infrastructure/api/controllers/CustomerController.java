package com.tickethub.infrastructure.api.controllers;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.customer.changename.*;
import com.tickethub.application.customer.create.*;
import com.tickethub.application.customer.delete.*;
import com.tickethub.application.customer.retrieve.get.*;
import com.tickethub.application.customer.retrieve.list.*;
import com.tickethub.infrastructure.customer.models.*;
import org.springframework.http.ResponseEntity;
import com.tickethub.infrastructure.api.ApiSupport;
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
        final var output = ApiSupport.execute(changeCustomerName,
                new ChangeCustomerNameCommand(id, input.name()));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<?> createCustomer(CreateCustomerRequest input) {
        final var output = ApiSupport.execute(createCustomer,
                new CreateCustomerCommand(input.cpf(), input.name()));
        return ResponseEntity.created(URI.create("/customers/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<?> deleteById(String id) {
        ApiSupport.execute(deleteCustomer, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> getById(String id) {
        return ResponseEntity.ok(CustomerResponse.from(ApiSupport.execute(getCustomer, id)));
    }

    @Override
    public ResponseEntity<?> list(String search, int page, int perPage, String sort, String direction) {
        final var result = ApiSupport.execute(listCustomers, ApiSupport.query(search, page, perPage, sort, direction))
                .map(CustomerListResponse::from);
        return ResponseEntity.ok(result);
    }
}
