package com.tickethub.application.customer.delete;

public record DeleteCustomerOutput(String id) {
	public static DeleteCustomerOutput from(final String id) {
		return new DeleteCustomerOutput(id);
	}
}
