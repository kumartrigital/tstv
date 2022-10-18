package org.mifosplatform.payments.razorpay.data;

import org.codehaus.jackson.annotate.JsonProperty;

public class InitializeTransactionResponseDTO {
	@JsonProperty("id")
	private String orderId;
	private String entity;
	private int amount;
	@JsonProperty("amount_paid")
	private int amountPaid;
	@JsonProperty("amount_due")
	private int amountDue;
	private String currency;
	private String receipt;
	@JsonProperty("offer_id")
	private String offerId;
	private String status;
	private int attempts;
	private Notes notes;
	@JsonProperty("created_at")
	private long createdAt;
	private String key;
	private String redirectURL;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public String getOrderId() {
		return orderId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(int amountPaid) {
		this.amountPaid = amountPaid;
	}

	public int getAmountDue() {
		return amountDue;
	}

	public void setAmountDue(int amountDue) {
		this.amountDue = amountDue;
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

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

}
