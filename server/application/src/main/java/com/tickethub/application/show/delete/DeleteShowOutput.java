package com.tickethub.application.show.delete;

public record DeleteShowOutput(String id) {
	public static DeleteShowOutput from(final String id) {
		return new DeleteShowOutput(id);
	}
}
