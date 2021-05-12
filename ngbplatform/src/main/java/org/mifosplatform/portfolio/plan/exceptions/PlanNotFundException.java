package org.mifosplatform.portfolio.plan.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class PlanNotFundException extends AbstractPlatformResourceNotFoundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PlanNotFundException() {
		super("error.msg.depositproduct.id.invalid","Charge Code already exists with same plan");
	}

public PlanNotFundException(Long planId) {
	super("error.msg.plan.with.id.not.exists","Plan was alreay deleted");

}

public PlanNotFundException(String string) {
	// TODO Auto-generated constructor stub
	super("error.msg.plan.is_advanced.flag.false.error","Plan is not configured with is_advanced flag");

}

}
