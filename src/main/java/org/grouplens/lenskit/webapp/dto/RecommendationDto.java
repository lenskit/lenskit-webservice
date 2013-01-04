package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class RecommendationDto extends Dto {
	
	public String item;
	
	public RecommendationDto(String item) {
		this.item = item;
	}
	
	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private RecommendationDto() {}
	
}
