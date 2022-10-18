package org.mifosplatform.payments.razorpay.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class RazorpayCreateOrderFailedException extends AbstractPlatformResourceNotFoundException {
	
	private static final long serialVersionUID = 1L;

	public RazorpayCreateOrderFailedException(String message) {
		super("error.msg.order.failed" + "failed to create razorpay order" , "failed to create razorpay order "+message);
    }
}