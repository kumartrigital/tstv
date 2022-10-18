package org.mifosplatform.payments.razorpay.data;

public class InitializeTransactionRequest {
	private int amount;
	private String currency;
	private String receipt;
	private Notes notes;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
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

	public Notes getNotes() {
		return notes;
	}

	public void setNotes(Notes notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return "InitializeTransactionRequest [amount=" + amount + ", currency=" + currency + ", receipt=" + receipt
				+ ", notes=" + notes + "]";
	}

}
