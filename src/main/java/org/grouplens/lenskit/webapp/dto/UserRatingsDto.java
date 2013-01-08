package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;
import org.grouplens.lenskit.webapp.dto.RatingDto;

public class UserRatingsDto extends Dto {

	@XmlAttribute
	public String user_id;
	
	@XmlAttribute
	public Integer count;
	
	@XmlAttribute
	public Integer start;
	
	public RatingDto[] ratings;
	
	private int next = 0;
	
	// Deserialization requires a no-arg constructor
	public UserRatingsDto() {}
	
	public UserRatingsDto(String user_id, int count, int start) {
		this.user_id = user_id;
		this.count = count;
		this.start = start;
		ratings = new RatingDto[count - start];
	}
	
	public void addRating(String eid, String iid, long timestamp, Double value, String _revision_id) {
		if (next == ratings.length)
			throw new IllegalStateException("UserRatingsDto filled to capacity");
		ratings[next++] = new RatingDto(eid, user_id, iid, timestamp, value, _revision_id);
	}
}