package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class UserPreferencesDto extends Dto {
	
	@XmlAttribute
	public String user_id;
	
	@XmlAttribute
	public Integer count;
	
	@XmlAttribute
	public Integer start;
	
	public PreferenceDto[] preferences;
	
	private int next;
	
	public UserPreferencesDto(String user_id, int count, int start) {
		this.user_id = user_id;
		this.count = count;
		this.start = start;
		preferences = new PreferenceDto[count - start];
		next = 0;
	}
	
	// Deserialization requires a no-arg constructor
	public UserPreferencesDto() {}
	
	public void addPreference(String iid, String type, double value) {
		if (next == preferences.length) {
			throw new IllegalStateException("UserPreferencesDto filled to capacity");
		}
		preferences[next++] = new PreferenceDto(iid, type, value);
	}
}