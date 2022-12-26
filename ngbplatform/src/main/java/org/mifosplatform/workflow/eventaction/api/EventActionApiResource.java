/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.workflow.eventaction.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionWritePlatformServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Path("/eventaction")
@Component
@Scope("singleton")
public class EventActionApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("eventaction", "entityName", "actionName", "json",
            "resourceId", "clientId"));
    private final String resourceNameForPermissions = "EVENTACTIONS";

    private final PlatformSecurityContext context;
    private final EventActionReadPlatformService eventActionReadPlatformService;
    private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
    private final DefaultToApiJsonSerializer<EventActionData> toApiJsonSerializer;
    private final EventActionWritePlatformServiceImpl eventActionWritePlatformServiceImpl;

    @Autowired
    public EventActionApiResource(final PlatformSecurityContext context, final EventActionReadPlatformService eventActionReadPlatformService,
            final DefaultToApiJsonSerializer<EventActionData> toApiJsonSerializer,  final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
            final EventActionWritePlatformServiceImpl eventActionWritePlatformServiceImpl) {
        this.context = context;
        this.eventActionReadPlatformService = eventActionReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
        this.eventActionWritePlatformServiceImpl = eventActionWritePlatformServiceImpl;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllEventActions(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset,
			@QueryParam("statusType") final String statusType) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        final SearchSqlQuery searchTicketMaster = SearchSqlQuery.forSearch(sqlSearch, offset,limit );
        final Page<EventActionData> data = this.eventActionReadPlatformService.retriveAllEventActions(searchTicketMaster,statusType);
        
        return this.toApiJsonSerializer.serialize(data);
    }
    
    @GET
    @Path("details")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<ActionDetaislData> retrieveActionDetails(@QueryParam("eventType") final String eventType) {

        //Siva context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        final List<ActionDetaislData> data = this.actionDetailsReadPlatformService.retrieveActionDetails(eventType);
        //return this.toApiJsonSerializer.serialize(data);
        return data;
    }
    
    @POST
    @Path("addnewaction")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public String AddNewActions(final @RequestBody  List<ActionDetaislData> data,@QueryParam("clientId") final String clientId,
			@QueryParam("resourceId") final String resourceId, @QueryParam("resourceString") final String resourceString ) {
    	
    	return eventActionWritePlatformServiceImpl.AddNewActions(data, Long.parseLong(clientId), resourceId, resourceString);
    	
    }

}