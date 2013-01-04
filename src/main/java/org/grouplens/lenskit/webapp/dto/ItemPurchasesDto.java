package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class ItemPurchasesDto extends Dto {

	@XmlAttribute
	public String item_id;
	
	@XmlAttribute
	public Integer count;
	
	@XmlAttribute
	public Integer start;
	
	public PurchaseDto[] purchases;
	
	private int next;
	
	public ItemPurchasesDto(String item_id, int count, int start) {
		this.item_id = item_id;
		this.count = count;
		this.start = start;
		purchases = new PurchaseDto[count - start];
		next = 0;
	}
	
	public void addPurchase(String eid, String uid, long timestamp) {
		if (next == purchases.length) throw new IllegalStateException("ItemPurchasesDto filled to capacity");
		purchases[next++] = new PurchaseDto(eid, uid, item_id, timestamp);
	}
	
	// This is a stub constructor required for GSON deserialization and should not be used
	@SuppressWarnings("unused")
	private ItemPurchasesDto() {}
}
