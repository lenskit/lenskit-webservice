package org.grouplens.lenskit.webapp.dto;

import org.grouplens.common.dto.Dto;

public class UserPurchasesDto extends Dto {

	@XmlAttribute
	public String user_id;

	@XmlAttribute
	public Integer count;

	@XmlAttribute
	public Integer start;

	public PurchaseDto[] purchases;
	
	private int next;

	// Deserialization requires a no-arg constructor
	public UserPurchasesDto() {}
	
	public UserPurchasesDto(String user_id, int count, int start) {
		this.user_id = user_id;
		this.count = count;
		this.start = start;
		purchases = new PurchaseDto[count - start];
	}
	
	public void addPurchase(String eid, String iid, long timestamp) {
		if (next == purchases.length) {
			throw new IllegalStateException("UserPurchasesDto filled to capacity");
		}
		purchases[next++] = new PurchaseDto(eid, user_id, iid, timestamp);
	}
}
