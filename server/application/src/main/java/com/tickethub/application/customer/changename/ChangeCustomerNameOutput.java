package com.tickethub.application.customer.changename;

import com.tickethub.domain.core.customer.Customer;

public record ChangeCustomerNameOutput(String id) {
	public static ChangeCustomerNameOutput from(final String id) {
		return new ChangeCustomerNameOutput(id);
	}

	public static ChangeCustomerNameOutput from(final Customer entity) {
		return from(entity.getId().getValue());
	}
}
