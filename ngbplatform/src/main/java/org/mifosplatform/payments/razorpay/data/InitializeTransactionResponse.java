package org.mifosplatform.payments.razorpay.data;

public class InitializeTransactionResponse {
	private String id;
	private String entity;
	private int amount;
	private int amount_paid;
	private int amount_due;
	private String currency;
	private String receipt;
	private String offer_id;
	private String status;
	private int attempts;
	private Notes notes;
	private long created_at;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getAmount_paid() {
		return amount_paid;
	}
	public void setAmount_paid(int amount_paid) {
		this.amount_paid = amount_paid;
	}
	public int getAmount_due() {
		return amount_due;
	}
	public void setAmount_due(int amount_due) {
		this.amount_due = amount_due;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getReceipt() {
		return receipt;
	}
	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}
	public String getOffer_id() {
		return offer_id;
	}
	public void setOffer_id(String offer_id) {
		this.offer_id = offer_id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getAttempts() {
		return attempts;
	}
	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}
	public Notes getNotes() {
		return notes;
	}
	public void setNotes(Notes notes) {
		this.notes = notes;
	}	
	public long getCreated_at() {
		return created_at;
	}
	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}
	@Override
	public String toString() {
		return "InitializeTransactionResponse [id=" + id + ", entity=" + entity + ", amount=" + amount
				+ ", amount_paid=" + amount_paid + ", amount_due=" + amount_due + ", currency=" + currency
				+ ", receipt=" + receipt + ", offer_id=" + offer_id + ", status=" + status + ", attempts=" + attempts
				+ ", notes=" + notes + ", created_at=" + created_at + "]";
	}

}
