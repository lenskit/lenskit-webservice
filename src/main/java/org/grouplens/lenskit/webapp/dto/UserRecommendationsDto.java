package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class UserRecommendationsDto extends Dto {
	
	@XmlAttribute
	public String user_id;
	
	@XmlAttribute
	public Integer count;
	
	@XmlAttribute
	public Integer start;
	
	public RecommendationDto[] recommendations;
	
	private int next;
	
	// Deserialization requires a no-arg constructor
	public UserRecommendationsDto() {}
	
	public UserRecommendationsDto(String user_id, int count, int start) {
		this.user_id = user_id;
		this.count = count;
		this.start = start;
		recommendations = new RecommendationDto[count - start];
		next = 0;
	}
	
	public void addRecommendation(String iid) {
		if (next == recommendations.length) {
			throw new IllegalStateException("UserRecommendationsDto filled to capacity");
		}
		recommendations[next++] = new RecommendationDto(iid);
	}
}
