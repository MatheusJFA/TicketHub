package com.tickethub.domain.core.customer;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.shared.CPF;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.validation.ValidationHandler;

public class Customer extends AggregateRoot<CustomerID> {
    private final CPF cpf;
    private Name name;

    private Customer(CustomerID id, CPF cpf, Name name) {
        super(id);
        this.cpf = cpf;
        this.name = name;
    }

    public static Customer create(String cpf, String name) {
        final CustomerID id = CustomerID.generate();
        return new Customer(
                id,
                CPF.create(cpf),
                Name.create(name));
    }

    public Customer changeName(final String name) {
        this.name = Name.create(name);
        markAsUpdated();
        return this;
    }

    public CPF getCpf() {
        return cpf;
    }

    public Name getName() {
        return name;
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new CustomerValidator(this, handler);
        validator.validate();
    }
}
