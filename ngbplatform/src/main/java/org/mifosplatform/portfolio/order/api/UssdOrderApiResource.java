package org.mifosplatform.portfolio.order.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.cms.eventprice.service.EventPriceReadPlatformService;
import org.mifosplatform.cms.media.domain.MediaAsset;
import org.mifosplatform.cms.mediadetails.domain.MediaAssetRepository;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.service.CrmServices;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.itemdetails.exception.SerialNumberNotFoundException;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.clientservice.service.ClientServiceReadPlatformService;
import org.mifosplatform.portfolio.order.data.OrderData;
import org.mifosplatform.portfolio.order.data.OrderUssdData;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderAddOnsReadPlaformService;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.mifosplatform.portfolio.plan.service.PlanReadPlatformService;
import org.mifosplatform.provisioning.networkelement.service.NetworkElementReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/order")
@Component
@Scope("singleton")
public class UssdOrderApiResource {
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "cancelledStatus", "status", "contractPeriod", "nextBillDate", "flag", "currentDate",
					"plan_code", "units", "service_code", "allowedtypes", "data", "servicedata", "billing_frequency",
					"start_date", "contract_period", "billingCycle", "startDate", "invoiceTillDate", "orderHistory",
					"userAction", "ispaymentEnable", "paymodes", "orderServices", "orderDiscountDatas",
					"discountstartDate", "discountEndDate", "userName", "isAutoProvision"));

	private final String resourceNameForPermissions = "ORDER";
	private final PlatformSecurityContext context;
	private final PlanReadPlatformService planReadPlatformService;
	private final OrderReadPlatformService orderReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final OrderAddOnsReadPlaformService orderAddOnsReadPlaformService;
	private final DefaultToApiJsonSerializer<OrderData> toApiJsonSerializer;
	private final DefaultToApiJsonSerializer<MediaAsset> toApiJsonSerializerMovie;
	private final DefaultToApiJsonSerializer<OrderUssdData> toApiJsonSerializerussd;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final OrderWritePlatformService orderWritePlatformService;
	private final HardwareAssociationReadplatformService associationReadplatformService;
	private final ClientServiceReadPlatformService clientServiceReadPlatformService;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final CrmServices crmServices;
	private final OrderRepository orderRepository;
	private final NetworkElementReadPlatformService networkElementReadPlatformService;
	private final ClientReadPlatformService clientReadPlatformService;
	private final MediaAssetRepository mediaAssetRepository;
	private final EventPriceReadPlatformService eventPriceReadPlatformService;

	@Autowired
	public UssdOrderApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<OrderData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final OrderReadPlatformService orderReadPlatformService,
			final PlanReadPlatformService planReadPlatformService,
			final MCodeReadPlatformService mCodeReadPlatformService,
			final OrderAddOnsReadPlaformService orderAddOnsReadPlaformService,
			final OrderWritePlatformService orderWritePlatformService,
			final HardwareAssociationReadplatformService associationReadplatformService,
			final ClientServiceReadPlatformService clientServiceReadPlatformService,
			final OfficeReadPlatformService officeReadPlatformService, final CrmServices crmServices,
			final OrderRepository orderRepository,
			final NetworkElementReadPlatformService networkElementReadPlatformService,
			final ClientReadPlatformService clientReadPlatformService,
			final DefaultToApiJsonSerializer<OrderUssdData> toApiJsonSerializerussd,
			final DefaultToApiJsonSerializer<MediaAsset> toApiJsonSerializerMovie,
			final MediaAssetRepository mediaAssetRepository,
			final EventPriceReadPlatformService eventPriceReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.planReadPlatformService = planReadPlatformService;
		this.mCodeReadPlatformService = mCodeReadPlatformService;
		this.orderReadPlatformService = orderReadPlatformService;
		this.orderAddOnsReadPlaformService = orderAddOnsReadPlaformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.orderWritePlatformService = orderWritePlatformService;
		this.associationReadplatformService = associationReadplatformService;
		this.clientServiceReadPlatformService = clientServiceReadPlatformService;
		this.officeReadPlatformService = officeReadPlatformService;
		this.crmServices = crmServices;
		this.orderRepository = orderRepository;
		this.networkElementReadPlatformService = networkElementReadPlatformService;
		this.clientReadPlatformService = clientReadPlatformService;
		this.toApiJsonSerializerussd = toApiJsonSerializerussd;
		this.toApiJsonSerializerMovie = toApiJsonSerializerMovie;
		this.mediaAssetRepository = mediaAssetRepository;
		this.eventPriceReadPlatformService = eventPriceReadPlatformService;

	}

	@GET
	@Path("{orderId}/status")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getUssdDetailsByOrderID(@PathParam("orderId") final String orderId, @Context final UriInfo uriInfo) {

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		OrderUssdData ussdData = null;

		if (orderId.length() < 12 || orderId.length() > 15) {
			ussdData = new OrderUssdData("INVALID_ORDER_ID", "Order ID is invalid format");
			return this.toApiJsonSerializerussd.serialize(settings, ussdData, RESPONSE_DATA_PARAMETERS);
		}

		if (orderId.length() == 15) {
			String serialNo = orderId.substring(0, 12);
			String movieCode = orderId.substring(13, 15);
			System.out.println("movie code " + movieCode);
			ClientData clientData = null;

			try {
				clientData = clientReadPlatformService.retrieveSearchClientId("serial_no", serialNo);
			} catch (Exception e) {
				ussdData = new OrderUssdData("ORDER_ID_NOT_FOUND", "Specified orderId was not found");
				return this.toApiJsonSerializerussd.serialize(settings, ussdData, RESPONSE_DATA_PARAMETERS);
			}
			MediaAsset movieData = mediaAssetRepository.findOneByOverView(movieCode);
			if (movieData == null) {
				ussdData = new OrderUssdData("ORDER_ID_NOT_FOUND", "Specified orderId was not found");
				return this.toApiJsonSerializerussd.serialize(settings, ussdData, RESPONSE_DATA_PARAMETERS);

			}
			Double Price = eventPriceReadPlatformService.findMoviePricingByMovieCode(movieData.getOverview());

			ussdData = new OrderUssdData("PENDING", "Payment for specified ID can be done", Price);

			return this.toApiJsonSerializerussd.serialize(settings, ussdData, RESPONSE_DATA_PARAMETERS);
		} else {
			ClientData clienthwdata = null;
			try {
				clienthwdata = clientReadPlatformService.retrieveSearchClientId("serial_no", orderId);
			} catch (Exception e) {
				ussdData = new OrderUssdData("ORDER_ID_NOT_FOUND", "Specified orderId was not found");
				return this.toApiJsonSerializerussd.serialize(settings, ussdData, RESPONSE_DATA_PARAMETERS);
			}

			ussdData = orderReadPlatformService.getOrderDetailsBySerialNo(orderId);
			return this.toApiJsonSerializerussd.serialize(settings, ussdData, RESPONSE_DATA_PARAMETERS);
		}
	}

	@POST
	@Path("/{orderId}/acquire/{referenceId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String postOrderUssd(@PathParam("orderId") final String orderId,
			@PathParam("referenceId") String referenceId, final String apiRequestBodyAsJson,
			@Context final UriInfo uriInfo) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.orderUssd(Long.parseLong(orderId), Long.parseLong(referenceId)).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);

	}

}
