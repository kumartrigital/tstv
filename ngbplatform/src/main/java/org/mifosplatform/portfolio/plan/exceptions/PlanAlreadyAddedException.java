package org.mifosplatform.portfolio.plan.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class PlanAlreadyAddedException extends AbstractPlatformResourceNotFoundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PlanAlreadyAddedException(String planCode) {
		super("error.msg.planalready.added.invalid","Plan is already added-->" +planCode);
	}

public PlanAlreadyAddedException(Long planId) {
	super("error.msg.plan.with.id.not.exists","Plan was alreay deleted");

}

}
