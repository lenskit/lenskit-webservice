package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class PreferenceDto extends Dto {
	
	public String type;
	
	public String item;
	
	public Double value;
	
	public PreferenceDto(String item_id, String type, double value) {
		this.item = item_id;
		this.type = type;
		this.value = value;
	}
	
	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private PreferenceDto() {}

}
