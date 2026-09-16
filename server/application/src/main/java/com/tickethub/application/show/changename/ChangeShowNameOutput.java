package com.tickethub.application.show.changename;

import com.tickethub.domain.core.show.Show;

public record ChangeShowNameOutput(String id) {
	public static ChangeShowNameOutput from(final String id) {
		return new ChangeShowNameOutput(id);
	}

	public static ChangeShowNameOutput from(final Show entity) {
		return from(entity.getId().getValue());
	}
}
