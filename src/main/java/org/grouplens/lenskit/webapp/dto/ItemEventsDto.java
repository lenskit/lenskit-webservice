package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class ItemEventsDto extends Dto {

	@XmlAttribute
	public String item_id;
	
	@XmlAttribute
	public Integer count;

	@XmlAttribute
	public Integer start;
	
	public EventDto[] events;
	
	private int next;
	
	// Deserialization requires a no-arg constructor
	public ItemEventsDto() {}
	
	public ItemEventsDto(String item_id, int count, int start) {
		this.item_id = item_id;
		this.count = count;
		this.start = start;
		events = new EventDto[count - start];
		next = 0;
	}
	
	public void addEvent(String type, String eid, String uid, long timestamp, Double value, String _revision_id) {
		if (next == events.length) throw new IllegalStateException("ItemEventsDto filled to capacity");
		events[next++] = new EventDto(type, eid, uid, item_id, timestamp, value, _revision_id);
	}
	
	public void addEvent(String type, String eid, String uid, long timestamp, String _revision_id) {
		if (next == events.length) throw new IllegalStateException("ItemEventsDto filled to capacity");
		events[next++] = new EventDto(type, eid, uid, item_id, timestamp, _revision_id);
	}
}
