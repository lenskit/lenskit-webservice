package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class ItemStatisticsDto extends Dto {
	
	@XmlAttribute
	public String item_id;
	
	public Integer event_count;
	
	public Integer user_rating_count;
	
	public Double average_rating;
	
	public ItemStatisticsDto(String item_id, int event_count, int user_rating_count, double average_rating) {
		this.item_id = item_id;
		this.average_rating = average_rating;
		this.user_rating_count = user_rating_count;
		this.event_count = event_count;
	}	
	
	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private ItemStatisticsDto() {}
}
