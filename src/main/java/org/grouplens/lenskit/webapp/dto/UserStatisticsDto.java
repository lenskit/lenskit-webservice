package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class UserStatisticsDto extends Dto {

	@XmlAttribute
	public String user_id;
	
	public Integer event_count;
	
	public Integer item_rating_count;
	
	public Double average_rating;
	
	public UserStatisticsDto(String user_id, int event_count, int item_rating_count, double average_rating) {
		this.user_id = user_id;
		this.event_count = event_count;
		this.item_rating_count = item_rating_count;
		this.average_rating = average_rating;
	}
	
	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private UserStatisticsDto() {}
}
