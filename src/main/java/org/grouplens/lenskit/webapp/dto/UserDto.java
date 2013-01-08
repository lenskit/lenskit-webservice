package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class UserDto extends Dto {

	public String user_id;
	
	@XmlAttribute
	public String _revision_id;
	
	// Deserialization requires a no-arg constructor
	public UserDto() {}
	
	public UserDto(String user_id) {
		this.user_id = user_id;
	}
	
	public UserDto(String user_id, String _revision_id) {
		this.user_id = user_id;
		this._revision_id = _revision_id;
	}
}
