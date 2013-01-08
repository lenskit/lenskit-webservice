package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class RecommendationDto extends Dto {
	
	public String item;
	
	// Deserialization requires a no-arg constructor
	public RecommendationDto() {}
	
	public RecommendationDto(String item) {
		this.item = item;
	}	
}
