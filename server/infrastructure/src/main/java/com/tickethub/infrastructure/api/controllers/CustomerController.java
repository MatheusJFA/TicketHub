package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.customer.changename.*;
import com.tickethub.application.customer.create.*;
import com.tickethub.application.customer.delete.*;
import com.tickethub.application.customer.retrieve.get.*;
import com.tickethub.application.customer.retrieve.list.*;
import com.tickethub.application.customer.update.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.infrastructure.api.CustomerAPI;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.infrastructure.customer.models.*;
import com.tickethub.infrastructure.customer.presenters.CustomerMapper;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController implements CustomerAPI {
    private final ChangeCustomerNameUseCase changeCustomerName;
    private final CreateCustomerUseCase createCustomer;
    private final DeleteCustomerUseCase deleteCustomer;
    private final GetCustomerUseCase getCustomer;
    private final ListCustomersUseCase listCustomers;
    private final UpdateCustomerUseCase updateCustomer;
    private final CustomerMapper mapper;

    public CustomerController(
            ChangeCustomerNameUseCase changeCustomerName,
            CreateCustomerUseCase createCustomer,
            DeleteCustomerUseCase deleteCustomer,
            GetCustomerUseCase getCustomer,
            ListCustomersUseCase listCustomers,
            UpdateCustomerUseCase updateCustomer,
            CustomerMapper mapper) {
        this.changeCustomerName = changeCustomerName;
        this.createCustomer = createCustomer;
        this.deleteCustomer = deleteCustomer;
        this.getCustomer = getCustomer;
        this.listCustomers = listCustomers;
        this.updateCustomer = updateCustomer;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<IdResponse> changeCustomerName(String id, ChangeCustomerNameRequest input) {
        final var output = HttpResults.require(changeCustomerName.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<IdResponse> createCustomer(CreateCustomerRequest input) {
        final var output = HttpResults.require(createCustomer.execute(mapper.toCommand(input)));
        return ResponseEntity.created(URI.create("/customers/" + output.id())).body(new IdResponse(output.id()));
    }

    @Override
    public ResponseEntity<Void> deleteById(String id) {
        HttpResults.requireEmpty(deleteCustomer.execute(id));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CustomerResponse> getById(String id) {
        return ResponseEntity.ok(mapper.toResponse(HttpResults.require(getCustomer.execute(id))));
    }

    @Override
    public ResponseEntity<Pagination<CustomerListResponse>> list(
            String search, int page, int perPage, String sort, String direction) {
        final var result = HttpResults.require(
                        listCustomers.execute(HttpResults.search(search, page, perPage, sort, direction)))
                .map(mapper::toListResponse);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<IdResponse> updateCustomer(String id, UpdateCustomerRequest input) {
        final var output = HttpResults.require(updateCustomer.execute(mapper.toCommand(id, input)));
        return ResponseEntity.ok(new IdResponse(output.id()));
    }
}
