/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.data.AuditData;
import org.mifosplatform.commands.data.AuditSearchData;
import org.mifosplatform.commands.data.CommandConstants;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.AuditReadPlatformService;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 *this api class use to perform  different maker actions are approve or delete or reject
 */
@Path("/executecmd")
@Component
@Scope("singleton")
public class ExecuteCmdApiResource {

    private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit;
    private final PortfolioCommandSourceWritePlatformService writePlatformService;

    @Autowired
    public ExecuteCmdApiResource(final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit,
            final PortfolioCommandSourceWritePlatformService writePlatformService) {
        this.toApiJsonSerializerAudit = toApiJsonSerializerAudit;
        this.writePlatformService = writePlatformService;
    }

  

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String executePostCommand(@QueryParam("cmdType") final String cmdType,final String payLoad) {
    	
    	final CommandWrapper commandRequest = getCommandRequest(cmdType,payLoad);//
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		return toApiJsonSerializerAudit.serialize(result);
    }
    
    @PUT
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String executePutCommand(@PathParam("id") final Long id,@QueryParam("cmdType") final String cmdType,final String payLoad) {
    	
    	final CommandWrapper commandRequest = getCommandRequest(id,cmdType,payLoad);//
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		return toApiJsonSerializerAudit.serialize(result);
    }
    
    @DELETE
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String executeDeleteCommand(@PathParam("id") Long id,@QueryParam("cmdType") String cmdType, String payLoad) {
    	payLoad="{statusRemark:\""+payLoad+"\"}";
    	final CommandWrapper commandRequest = getCommandRequest(id,cmdType,payLoad);//
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		return toApiJsonSerializerAudit.serialize(result);
    }
    
	private CommandWrapper getCommandRequest(String cmdType, String payLoad) {
		CommandWrapper commandRequest = null;
		switch(cmdType) {
		//ClientProspect
		  case CommandConstants.CMD_CREATE_PROSPECT:
			  commandRequest = new CommandWrapperBuilder().createProspect().withJson(payLoad).build();
		    break;
		  case CommandConstants.CMD_CREATE_QUOTE:
			  commandRequest = new CommandWrapperBuilder().createQuote().withJson(payLoad).build();
		    break;
		  //Quotes   
		  default:
		    // default code block
		}
		return commandRequest;
	}
	
	private CommandWrapper getCommandRequest(Long id, String cmdType, String payLoad) {
		CommandWrapper commandRequest = null;
		switch(cmdType) {
		//ClientProspect
		  case CommandConstants.CMD_UPDATE_PROSPECT:
			  commandRequest = new CommandWrapperBuilder().updateProspect(id).withJson(payLoad).build();
		    break;
		  case CommandConstants.CMD_DELETE_PROSPECT:
			  commandRequest = new CommandWrapperBuilder().deleteProspect(id).withJson(payLoad).build();
		    break;  
		  case CommandConstants.CMD_UPDATE_FOLLOWUP_PROSPECT:
			  commandRequest = new CommandWrapperBuilder().followUpProspect(id).withJson(payLoad).build();
		    break;
		  case CommandConstants.CMD_CONVERT_PROSPECT_TO_CLIENT:
			  commandRequest = new CommandWrapperBuilder().convertProspectToClient(id).withJson(payLoad).build();
		    break;
		  case CommandConstants.CMD_UPDATE_ELEVATE_PROSPECT:
			  commandRequest = new CommandWrapperBuilder().elevateProspect(id).withJson(payLoad).build();
		    break;
		  //Quotes
		  case CommandConstants.CMD_DELETE_QUOTE:
			  commandRequest = new CommandWrapperBuilder().deleteQuotation(id).withJson(payLoad).build();
		    break;
		  case CommandConstants.CMD_UPDATE_QUOTE:
			  commandRequest = new CommandWrapperBuilder().updateQuotation(id).withJson(payLoad).build();
		    break;
		  case CommandConstants.CMD_UPDATE_QUOTE_STATUS:
			  commandRequest = new CommandWrapperBuilder().updateQuotationStatus(id).withJson(payLoad).build();
		    break;
		    
		  default:
		    // default code block
		}
		return commandRequest;
	}
}