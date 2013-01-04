package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class PredictionDto extends Dto {

	public String item;
	
	public Double value;
	
	public PredictionDto(String item, double value) {
		this.item = item;
		this.value = value;
	}
	
	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private PredictionDto() {}
}
