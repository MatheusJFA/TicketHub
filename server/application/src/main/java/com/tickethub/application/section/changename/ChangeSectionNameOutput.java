package com.tickethub.application.section.changename;

import com.tickethub.domain.core.section.Section;

public record ChangeSectionNameOutput(String id) {
	public static ChangeSectionNameOutput from(final String id) {
		return new ChangeSectionNameOutput(id);
	}

	public static ChangeSectionNameOutput from(final Section entity) {
		return from(entity.getId().getValue());
	}
}
