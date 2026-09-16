package com.tickethub.application.partner.delete;

public record DeletePartnerOutput(String id) {
	public static DeletePartnerOutput from(final String id) {
		return new DeletePartnerOutput(id);
	}

}
