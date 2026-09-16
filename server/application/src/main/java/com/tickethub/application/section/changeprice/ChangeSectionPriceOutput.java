package com.tickethub.application.section.changeprice;

import com.tickethub.domain.core.section.Section;

public record ChangeSectionPriceOutput(String id) {
	public static ChangeSectionPriceOutput from(final String id) {
		return new ChangeSectionPriceOutput(id);
	}

	public static ChangeSectionPriceOutput from(final Section entity) {
		return from(entity.getId().getValue());
	}
}
