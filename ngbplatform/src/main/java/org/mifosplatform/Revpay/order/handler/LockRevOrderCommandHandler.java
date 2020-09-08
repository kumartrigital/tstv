package org.mifosplatform.Revpay.order.handler;

import org.mifosplatform.Revpay.order.service.RevPayOrderWritePlatformService;
import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommandType(entity = "REVPAY", action = "UPDATE")
public class LockRevOrderCommandHandler implements NewCommandSourceHandler {

	private final RevPayOrderWritePlatformService writePlatformService;

	@Autowired
	public LockRevOrderCommandHandler(final RevPayOrderWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		// TODO Auto-generated method stub
		return this.writePlatformService.lockOrder( command.entityId(),command.getSupportedEntityType());
	}
}
