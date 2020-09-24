package org.mifosplatform.finance.chargeorder.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ProcessDateGreaterThanPlanEndDateException  extends AbstractPlatformResourceNotFoundException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessDateGreaterThanPlanEndDateException(final String msg) {
        super("error.end date reached " , msg);
    }
}
