package org.mifosplatform.payments.razorpay.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SignatureAuthenticationException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public SignatureAuthenticationException(String message) {
		super("error.msg.signatue.authentication.exception","error.msg.signatue.authentication.exception", message);
	}

}