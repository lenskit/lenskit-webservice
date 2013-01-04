package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class ItemRatingsDto extends Dto {

	@XmlAttribute
	public String item_id;
	
	@XmlAttribute
	public Integer count;
	
	@XmlAttribute
	public Integer start;
	
	public RatingDto[] ratings;
	
	private int next;
	
	public ItemRatingsDto(String item_id, int count, int start) {
		this.item_id = item_id;
		this.count = count;
		this.start = start;
		ratings = new RatingDto[count - start];
	}
	
	//This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private ItemRatingsDto() {}
	
	public void addRating(String eid, String uid, long timestamp, Double value, String _revision_id) {
		if (next == ratings.length)
			throw new IllegalStateException("ItemRatingsDto filled to capacity");
		ratings[next++] = new RatingDto(eid, uid, item_id, timestamp, value, _revision_id);
	}
}
