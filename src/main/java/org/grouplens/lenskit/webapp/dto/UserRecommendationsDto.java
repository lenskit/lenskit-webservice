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
	
	public UserRecommendationsDto(String user_id, int count, int start) {
		this.user_id = user_id;
		this.count = count;
		this.start = start;
		recommendations = new RecommendationDto[count - start];
		next = 0;
	}
	
	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private UserRecommendationsDto() {}
	
	public void addRecommendation(String iid) {
		if (next == recommendations.length)
			throw new IllegalStateException("UserRecommendationsDto filled to capacity");
		recommendations[next++] = new RecommendationDto(iid);
	}
}
