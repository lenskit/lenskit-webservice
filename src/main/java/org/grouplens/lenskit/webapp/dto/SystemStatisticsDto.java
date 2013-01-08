package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class SystemStatisticsDto extends Dto {
	
	public Integer user_count;
	
	public Integer item_count;
	
	public Integer event_count;
	
	// Deserialization requires a no-arg constructor
	public SystemStatisticsDto() {}
	
	public SystemStatisticsDto(int user_count, int item_count, int event_count) {
		this.user_count = user_count;
		this.item_count = item_count;
		this.event_count = event_count;;
	}
}
