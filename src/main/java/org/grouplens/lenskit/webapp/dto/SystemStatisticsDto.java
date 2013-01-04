package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class SystemStatisticsDto extends Dto {
	
	public Integer user_count;
	
	public Integer item_count;
	
	public Integer event_count;
	
	public SystemStatisticsDto(int user_count, int item_count, int event_count) {
		this.user_count = user_count;
		this.item_count = item_count;
		this.event_count = event_count;;
	}

	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private SystemStatisticsDto() {}
}
