package org.mifosplatform.Revpay.order.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONObject;
import org.mifosplatform.Revpay.order.domain.RevPayOrderRepository;
import org.mifosplatform.Revpay.order.domain.RevpayOrder;
import org.mifosplatform.Revpay.order.service.RevPayOrderWritePlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.payments.api.PaymentsApiResource;
import org.mifosplatform.finance.payments.service.PaymentWritePlatformService;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Component
@Path("/revpay")
public class RevPayOrdersApiResource {

	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "provisionType", "action", "provisionigSystem", "isEnable"));
	private final DefaultToApiJsonSerializer<RevpayOrder> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final RevPayOrderWritePlatformService revPayOrderWritePlatformService;
	private final RevPayOrderRepository revPayOrderRepository;
	private final FromJsonHelper fromApiJsonHelper;
	private final OrderWritePlatformService orderWritePlatformService;
	private final PaymentGatewayRepository paymentGatewayRepository;
	private final PaymentWritePlatformService PaymentWritePlatformService;
	private final PaymentsApiResource paymentsApiResource;
	private final ToApiJsonSerializer<PaymentGateway> apiJsonSerializerPaymentGateway;

	@Autowired
	public RevPayOrdersApiResource(final DefaultToApiJsonSerializer<RevpayOrder> apiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final RevPayOrderWritePlatformService revPayOrderWritePlatformService,
			final RevPayOrderRepository revPayOrderRepository, final FromJsonHelper fromApiJsonHelper,
			final OrderWritePlatformService orderWritePlatformService,
			final PaymentGatewayRepository paymentGatewayRepository,
			final PaymentWritePlatformService PaymentWritePlatformService,
			final PaymentsApiResource paymentsApiResource,
			final ToApiJsonSerializer<PaymentGateway> apiJsonSerializerPaymentGateway) {

		this.toApiJsonSerializer = apiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.revPayOrderWritePlatformService = revPayOrderWritePlatformService;
		this.revPayOrderRepository = revPayOrderRepository;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.orderWritePlatformService = orderWritePlatformService;
		this.paymentGatewayRepository = paymentGatewayRepository;
		this.PaymentWritePlatformService = PaymentWritePlatformService;
		this.paymentsApiResource = paymentsApiResource;
		this.apiJsonSerializerPaymentGateway = apiJsonSerializerPaymentGateway;

	}

	@POST
	@Path("/createorder")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createOrder(String apiRequestBodyAsJson) {

		final CommandWrapper commandWrapper = new CommandWrapperBuilder().createRevOrder()
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandWrapper);
		return this.toApiJsonSerializer.serialize(result);

	}

	@GET
	@Path("/status/{txid}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getRavePayStatus(@PathParam("txid") String txid, @Context final UriInfo uriInfo) {

		PaymentGateway orderDetails = paymentGatewayRepository.findPaymentDetailsByPaymentId(txid);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializerPaymentGateway.serialize(settings, orderDetails, RESPONSE_DATA_PARAMETERS);

	}

	@GET
	@Path("/orderlock/{txref}/{flwref}")
	@SuppressWarnings("unchecked")
	public Response GetcllBackRavePayOrder(@PathParam("txref") Long txref, @PathParam("flwref") String flwref,
			@QueryParam("resp") String resp) {

		URI indexPath = null;

		String status = revPayOrderWritePlatformService.revTransactionStatus(txref);
		String locale = "en";
		String dateFormat = "dd MMMM yyyy";
		PaymentGateway revpayOrder = paymentGatewayRepository.findPaymentDetailsByPaymentId(txref.toString());
		if (status.equalsIgnoreCase("success")) {

			revpayOrder.setStatus("successful");
			revpayOrder.setPartyId(flwref);
			paymentGatewayRepository.save(revpayOrder);

			JSONObject paymentJson = new JSONObject();
			paymentJson.put("clientId", revpayOrder.getReffernceId());
			paymentJson.put("isSubscriptionPayment", "false");
			paymentJson.put("isChequeSelected", "No");
			paymentJson.put("paymentCode", 27);
			paymentJson.put("receiptNo", revpayOrder.getReceiptNo());// need to
			paymentJson.put("remarks", "nothing");
			paymentJson.put("amountPaid", revpayOrder.getAmountPaid());// need to
			paymentJson.put("paymentType", "Online Payment");
			paymentJson.put("locale", locale);
			paymentJson.put("dateFormat", dateFormat);
			paymentJson.put("paymentSource", null);
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

			paymentJson.put("paymentDate", formatter.format(revpayOrder.getPaymentDate()));

			paymentsApiResource.createPayment(Long.parseLong(revpayOrder.getReffernceId()), paymentJson.toString());
		} else if (status.equals("failed")) {
			revpayOrder.setStatus("Failed");
			revpayOrder.setPartyId(flwref);
		} else if (status.equals("success-pending-validation")) {
			revpayOrder.setStatus("success-pending-validation");
			revpayOrder.setPartyId(flwref);
		} else {
			revpayOrder.setStatus("Not found");
			revpayOrder.setPartyId(flwref);
		}

		try {
			indexPath = new URI("https://tstv.nextgenerationbilling.com/#/renewal-customer/" + txref);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return Response.temporaryRedirect(indexPath).build();
	}

}
