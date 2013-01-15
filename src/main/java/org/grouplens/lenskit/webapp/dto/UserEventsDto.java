package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class UserEventsDto extends Dto {

	@XmlAttribute
	public String user_id;
	
	@XmlAttribute
	public Integer count;
	
	@XmlAttribute
	public Integer start;
	
	public EventDto[] events;
	
	private int next;
	
	// Deserialization requires a no-arg constructor
	public UserEventsDto() {}
	
	public UserEventsDto(String user_id, int count, int start) {
		this.user_id = user_id;
		this.count = count;
		this.start = start;
		events = new EventDto[count - start];
		next = 0;
	}
	
	public void addEvent(String type, String event_id, String item_id, long timestamp, Double value, String _revision_id) {
		if (next == events.length) {
			throw new IllegalStateException("UserEventsDto filled to capacity");
		}
		events[next++] = new EventDto(type, event_id, user_id, item_id, timestamp, value, _revision_id);
	}
	
	public void addEvent(String type, String event_id, String item_id, long timestamp, String _revision_id) {
		if (next == events.length) {
			throw new IllegalStateException("UserEventsDto filled to capacity");
		}
		events[next++] = new EventDto(type, event_id, user_id, item_id, timestamp, null,  _revision_id);
	}
}
