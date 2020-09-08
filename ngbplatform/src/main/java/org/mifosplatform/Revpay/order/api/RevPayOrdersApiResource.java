package org.mifosplatform.Revpay.order.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;
import org.kohsuke.rngom.digested.DListPattern;
import org.mifosplatform.Revpay.order.domain.RevPayOrderRepository;
import org.mifosplatform.Revpay.order.domain.RevpayOrder;
import org.mifosplatform.Revpay.order.service.RevPayOrderWritePlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.payments.service.PaymentWritePlatformService;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.sun.jersey.spi.resource.Singleton;

import antlr.StringUtils;

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

	@Autowired
	public RevPayOrdersApiResource(final DefaultToApiJsonSerializer<RevpayOrder> apiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final RevPayOrderWritePlatformService revPayOrderWritePlatformService,
			final RevPayOrderRepository revPayOrderRepository, final FromJsonHelper fromApiJsonHelper,
			final OrderWritePlatformService orderWritePlatformService,
			final PaymentGatewayRepository paymentGatewayRepository,
			final PaymentWritePlatformService PaymentWritePlatformService) {

		this.toApiJsonSerializer = apiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.revPayOrderWritePlatformService = revPayOrderWritePlatformService;
		this.revPayOrderRepository = revPayOrderRepository;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.orderWritePlatformService = orderWritePlatformService;
		this.paymentGatewayRepository = paymentGatewayRepository;
		this.PaymentWritePlatformService = PaymentWritePlatformService;

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

	@POST
	@Path("/orderlock")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@SuppressWarnings("unchecked")
	public Response CallBackRavePayOrder(@PathParam("txref") Long txref, @PathParam("flwref") String flwref) {
		String status = revPayOrderWritePlatformService.revTransactionStatus(txref);

		PaymentGateway revpayOrder = paymentGatewayRepository.findPaymentDetailsByPaymentId(txref.toString());
		if (status.equals("success")) {

			revpayOrder.setStatus("Success");
			revpayOrder.setPartyId(flwref);

			JSONObject paymentJson = new JSONObject();
			paymentJson.put("clientId", revpayOrder.getReffernceId());
			paymentJson.put("isSubscriptionPayment", "false");
			paymentJson.put("isChequeSelected", "No");
			paymentJson.put("paymentCode", 27);
			paymentJson.put("receiptNo", revpayOrder.getReceiptNo());// need to
			paymentJson.put("remarks", "nothing");
			paymentJson.put("amountPaid", revpayOrder.getAmountPaid());// need to change
			paymentJson.put("paymentType", "Online Payment");
			paymentJson.put("locale", "en");
			paymentJson.put("dateFormat", "dd MMMM yyyy");
			paymentJson.put("paymentSource", null);
			paymentJson.put("paymentDate", "false");

			final JsonElement paymentElement = fromApiJsonHelper.parse(paymentJson.toString());

			JsonCommand paymentCommandJson = new JsonCommand(null, paymentElement.toString(), paymentElement,
					fromApiJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);

			PaymentWritePlatformService.createPayment(paymentCommandJson);

		} else {
			revpayOrder.setStatus("Failed");
		}
		paymentGatewayRepository.save(revpayOrder);
		URI indexPath = null;
		try {
			indexPath = new URI("https://www.facebook.com/");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return Response.temporaryRedirect(indexPath).build();
	}
}
