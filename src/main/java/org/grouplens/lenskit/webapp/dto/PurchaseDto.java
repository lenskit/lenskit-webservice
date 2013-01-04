package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class PurchaseDto extends Dto {

	public String event_id;
	
	public String user_id;
	
	public String item_id;
	
	public Long timestamp;
	
	public PurchaseDto(String event_id, String user_id, String item_id, long timestamp) {
		this.event_id = event_id;
		this.user_id = user_id;
		this.item_id = item_id;
		this.timestamp = timestamp;
	}
	
	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private PurchaseDto() {}
	
}
