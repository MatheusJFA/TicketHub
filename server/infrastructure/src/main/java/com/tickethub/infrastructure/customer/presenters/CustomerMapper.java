package com.tickethub.infrastructure.customer.presenters;

import com.tickethub.application.customer.changename.ChangeCustomerNameCommand;
import com.tickethub.application.customer.create.CreateCustomerCommand;
import com.tickethub.application.customer.retrieve.get.GetCustomerOutput;
import com.tickethub.application.customer.retrieve.list.ListCustomersOutput;
import com.tickethub.application.customer.update.UpdateCustomerCommand;
import com.tickethub.infrastructure.customer.models.ChangeCustomerNameRequest;
import com.tickethub.infrastructure.customer.models.CreateCustomerRequest;
import com.tickethub.infrastructure.customer.models.CustomerListResponse;
import com.tickethub.infrastructure.customer.models.CustomerResponse;
import com.tickethub.infrastructure.customer.models.UpdateCustomerRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(GetCustomerOutput output);

    CustomerListResponse toListResponse(ListCustomersOutput output);

    CreateCustomerCommand toCommand(CreateCustomerRequest request);

    ChangeCustomerNameCommand toCommand(String id, ChangeCustomerNameRequest request);

    UpdateCustomerCommand toCommand(String id, UpdateCustomerRequest request);
}
