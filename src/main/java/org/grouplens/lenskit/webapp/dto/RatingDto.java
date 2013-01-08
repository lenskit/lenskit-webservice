package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;
import org.grouplens.lenskit.data.event.Rating;

public class RatingDto extends Dto {
	
	public String event_id;
	
	public String user_id;
	
	public String item_id;
	
	public Long timestamp;
	
	public Double value;
	
	public String _revision_id;
	
	// Deserialization requires a no-arg constructor
	public RatingDto() {}
	
	public RatingDto(String event_id, String user_id, String item_id, Long timestamp, Double value) {
		this.event_id = event_id;
		this.user_id = user_id;
		this.item_id = item_id;
		this.timestamp = timestamp;
		this.value = value;
	}
	
	public RatingDto(String event_id, String user_id, String item_id, Long timestamp, Double value, String _revision_id) {
		this.event_id = event_id;
		this.user_id = user_id;
		this.item_id = item_id;
		this.timestamp = timestamp;
		this.value = value;
		this._revision_id = _revision_id;
	}
	
	public RatingDto(String item_id, Double value) {
		this.item_id = item_id;
		this.value = value;
	}
	
	public RatingDto(Rating r) {
		this.event_id = Long.toString(r.getId());
		this.user_id = Long.toString(r.getUserId());
		this.item_id = Long.toString(r.getItemId());
		this.timestamp = r.getTimestamp();
		if (r.getPreference() == null) {
			this.value = null;
		} else {
			this.value = r.getPreference().getValue();
		}
	}
}
