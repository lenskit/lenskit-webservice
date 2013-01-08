package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.event.Rating;

public class EventDto extends Dto {
	
	public String type;
	
	public String event_id;
	
	public String user_id;
	
	public String item_id;
	
	public Long timestamp;
	
	public Double value;
	
	public String _revision_id;
	
	public EventDto() {}
	
	public EventDto(String type, String event_id, String user_id, String item_id, long timestamp, Double value) {
		this.type = type;
		this.event_id = event_id;
		this.user_id = user_id;
		this.item_id = item_id;
		this.timestamp = timestamp;
		this.value = value;
	}
	
	public EventDto(String type, String event_id, String user_id, String item_id, long timestamp) {
		this.type = type;
		this.event_id = event_id;
		this.user_id = user_id;
		this.item_id = item_id;
		this.timestamp = timestamp;
	}
	
	public EventDto(String type, String event_id, String user_id, String item_id, long timestamp, Double value, String _revision_id) {
		this.type = type;
		this.event_id = event_id;
		this.user_id = user_id;
		this.item_id = item_id;
		this.timestamp = timestamp;
		this.value = value;
		this._revision_id = _revision_id;
	}
	
	public EventDto(String type, String event_id, String user_id, String item_id, long timestamp, String _revision_id) {
		this.type = type;
		this.event_id = event_id;
		this.user_id = user_id;
		this.item_id = item_id;
		this.timestamp = timestamp;
		this._revision_id = _revision_id;
	}
	
	public EventDto(Event evt) {
		this.type = evt.getClass().getSimpleName();
		this.event_id = Long.toString(evt.getId());
		this.user_id = Long.toString(evt.getUserId());
		this.item_id = Long.toString(evt.getItemId());
		this.timestamp = evt.getTimestamp();
		if (evt instanceof Rating) {
			Rating r = (Rating)evt;
			if (r.getPreference() != null)
				this.value = r.getPreference().getValue();
		}
	}
}
