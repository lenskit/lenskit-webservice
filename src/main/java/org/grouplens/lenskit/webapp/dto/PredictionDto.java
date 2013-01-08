package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class PredictionDto extends Dto {

	public String item;
	
	public Double value;
	
	// Deserialization requires a no-arg constructor
	public PredictionDto() {}
	
	public PredictionDto(String item, double value) {
		this.item = item;
		this.value = value;
	}
}
