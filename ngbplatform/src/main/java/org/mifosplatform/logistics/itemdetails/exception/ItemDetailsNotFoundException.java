package org.mifosplatform.logistics.itemdetails.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ItemDetailsNotFoundException extends AbstractPlatformDomainRuleException {

    

	public ItemDetailsNotFoundException(Long itemId) {
		 super("error.msg.item.id.exception", "Item id not found "+itemId,itemId);
		 
	}
	
	
}
