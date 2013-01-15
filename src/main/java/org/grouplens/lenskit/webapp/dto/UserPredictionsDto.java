package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class UserPredictionsDto extends Dto {
	
	@XmlAttribute
	public String user_id;
	
	@XmlAttribute
	public Integer count;
	
	@XmlAttribute
	public Integer start;
	
	public PredictionDto[] predictions;
	
	private int next;
	
	public UserPredictionsDto(String user_id, int count, int start) {
		this.user_id = user_id;
		this.count = count;
		this.start = start;
		predictions = new PredictionDto[count - start];
		next = 0;
	}
	
	// Deserialization requires a no-arg constructor
	public UserPredictionsDto() {}
	
	public void addPrediction(String iid, Double value) {
		if (next == predictions.length) {
			throw new IllegalStateException("UserPredictionsDto filled to capacity");
		}
		predictions[next++] = new PredictionDto(iid, value);
	}
}
