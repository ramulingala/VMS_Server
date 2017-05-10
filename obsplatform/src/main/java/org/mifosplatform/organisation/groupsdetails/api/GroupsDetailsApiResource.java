package org.mifosplatform.organisation.groupsdetails.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.groupsdetails.data.GroupsDetailsData;
import org.mifosplatform.organisation.groupsdetails.service.GroupsDetailsReadPlatformService;
import org.mifosplatform.organisation.office.service.SearchSqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/groupsdetails")
@Component
@Scope("singleton")
public class GroupsDetailsApiResource {
	
	private final String resourceNameForPermission = "GROUPSDETAILS";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<GroupsDetailsData> toApiJsonSerializer;
	private final GroupsDetailsReadPlatformService groupsDetailsReadPlatformService; 
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	
	@Autowired
	public GroupsDetailsApiResource(final PlatformSecurityContext context,final GroupsDetailsReadPlatformService groupsDetailsReadPlatformService,
		final ApiRequestParameterHelper apiRequestParameterHelper,final DefaultToApiJsonSerializer<GroupsDetailsData> toApiJsonSerializer,
		final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService){
		
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.groupsDetailsReadPlatformService = groupsDetailsReadPlatformService;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
	}
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveGroupDetails(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,@QueryParam("limit") final Integer limit,
			@QueryParam("offset") final Integer offset){
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final SearchSqlQuery searchGroupsDetails =SearchSqlQuery.forSearch(sqlSearch, offset, limit);
		final Page<GroupsDetailsData> groupsData = this.groupsDetailsReadPlatformService.retrieveAllGroupsData(searchGroupsDetails);
		return this.toApiJsonSerializer.serialize(groupsData);
		
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String createGroupsDetails(final String jsonRequestBody){
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createGroupsDetails().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@POST
	@Path("provision/{prepareRequestId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String addGroupsDetailsProvision(@PathParam("prepareRequestId") final Long prepareRequestId,final String jsonRequestBody){
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createGroupsDetailsProvision(prepareRequestId).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@POST
	@Path("statment/{clientId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String generateStatment(@PathParam("{clientid}")final Long clientId,final String jsonRequestBody){
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createGroupsStatment(clientId).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
}
