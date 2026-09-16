package com.tickethub.application.section.delete;

public record DeleteSectionOutput(String id) {
	public static DeleteSectionOutput from(final String id) {
		return new DeleteSectionOutput(id);
	}
}
