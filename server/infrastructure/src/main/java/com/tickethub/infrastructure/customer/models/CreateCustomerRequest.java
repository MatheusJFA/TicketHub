package com.tickethub.infrastructure.customer.models;

import com.tickethub.infrastructure.api.models.*;

public record CreateCustomerRequest(String cpf, String name, String email, String password) {}
