package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class ItemDto extends Dto {

	public String item_id;
	
	public String _revision_id;
	
	public ItemDto() {}
	
	public ItemDto(String item_id) {
		this.item_id = item_id;
	}
	
	public ItemDto(String item_id, String _revision_id) {
		this.item_id = item_id;
		this._revision_id = _revision_id;
	}
}
