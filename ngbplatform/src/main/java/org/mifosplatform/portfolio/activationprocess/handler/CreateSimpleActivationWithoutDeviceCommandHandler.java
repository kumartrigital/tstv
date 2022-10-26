package org.mifosplatform.portfolio.activationprocess.handler;
/**
 * Siva Kishore
 */
import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.activationprocess.service.ActivationProcessWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommandType(entity = "CUSTOMERSERVICEACTIVATIONWOD", action = "CREATE")
public class CreateSimpleActivationWithoutDeviceCommandHandler implements NewCommandSourceHandler{
	
	private final ActivationProcessWritePlatformService activationProcessWritePlatformService;

	@Autowired
	public CreateSimpleActivationWithoutDeviceCommandHandler(ActivationProcessWritePlatformService activationProcessWritePlatformService) {
		this.activationProcessWritePlatformService = activationProcessWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		// TODO Auto-generated method stub
		
			return this.activationProcessWritePlatformService.createCustomerServiceActivationWithoutDevice(command);
		
	}
	
	

}
