package com.tickethub.application.spot.delete;

public record DeleteSpotOutput(String id) {
	public static DeleteSpotOutput from(final String id) {
		return new DeleteSpotOutput(id);
	}
}
