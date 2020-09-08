package org.mifosplatform.inview.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;

public interface InviewWritePlatformService {

	public void createClient(JsonCommand command);

	public void topUpforPaywizard(JsonCommand command, Long clientId);

	public void addMovieForPaywizard(JsonCommand command);

	String retrackForPaywizardRestCall (String username);

}
