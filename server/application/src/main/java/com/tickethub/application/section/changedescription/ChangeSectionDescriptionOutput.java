package com.tickethub.application.section.changedescription;

import com.tickethub.domain.core.section.Section;

public record ChangeSectionDescriptionOutput(String id) {
	public static ChangeSectionDescriptionOutput from(final String id) {
		return new ChangeSectionDescriptionOutput(id);
	}

	public static ChangeSectionDescriptionOutput from(final Section entity) {
		return from(entity.getId().getValue());
	}
}
