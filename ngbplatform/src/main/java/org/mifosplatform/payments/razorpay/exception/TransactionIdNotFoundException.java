package org.mifosplatform.payments.razorpay.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class TransactionIdNotFoundException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public TransactionIdNotFoundException(Long txid) {
		super("error.msg.transaction.id.not.found", "TransactionId not Exist with this " + txid, txid);
	}

	public TransactionIdNotFoundException(String txid) {
		super("error.msg.transaction.id.not.found","TransactionId not Exist with this " + txid, txid);
	}

	public TransactionIdNotFoundException(String message, String txid) {
		super("error.for.this.transaction.", message, txid);
	}


}