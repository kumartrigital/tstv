package org.mifosplatform.provisioning.provisioning.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class ProvisioningRequestNotFoundException extends AbstractPlatformDomainRuleException {

	public ProvisioningRequestNotFoundException(Long provisionId) {
		super("error.msg.provisioning.request.not.found.with.this.identifier","provisioning request not found with this identifier",provisionId);
		
	}
	
	public ProvisioningRequestNotFoundException(String message) {
		super("error.msg.provisioning.request.not.process.with.this.identifier","provisioning request not able to process",message);
		
	}

}
