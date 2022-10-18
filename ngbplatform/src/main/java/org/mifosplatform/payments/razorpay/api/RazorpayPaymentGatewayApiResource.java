/**
 * 
 * @author SivaKishore
 *
 */
package org.mifosplatform.payments.razorpay.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.payments.razorpay.data.InitializeTransactionRequest;
import org.mifosplatform.payments.razorpay.data.InitializeTransactionResponseDTO;
import org.mifosplatform.payments.razorpay.data.OrderLockRequest;
import org.mifosplatform.payments.razorpay.service.RazorpayInitializeTransactionServiceImpl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

@Path("/razorpay")
@Component
@Scope("singleton")
public class RazorpayPaymentGatewayApiResource {
	private final RazorpayInitializeTransactionServiceImpl initializeTransactionService;

	@Autowired
	public RazorpayPaymentGatewayApiResource(final PlatformSecurityContext context,
			final FromJsonHelper fromApiJsonHelper,
			final RazorpayInitializeTransactionServiceImpl initializeTransactionService) {
		this.initializeTransactionService = initializeTransactionService;
	}

	@POST
	@Path("/createorder/{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public InitializeTransactionResponseDTO initializeTransaction(@PathParam("officeId") Long officeId,
			@RequestBody InitializeTransactionRequest initializeTransactionRequest) {
		InitializeTransactionResponseDTO initializeTransactionResponse = initializeTransactionService
				.createOrder(officeId, initializeTransactionRequest);
		return initializeTransactionResponse;
	}

	@POST
	@Path("/orderlock")
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
	public Response orderLock(@FormParam("razorpay_payment_id") String paymentId,
			@FormParam("razorpay_order_id") String orderId, @FormParam("razorpay_signature") String signature) {

		OrderLockRequest reqObj = new OrderLockRequest(paymentId, orderId, signature);
		return initializeTransactionService.processAndUpdate(reqObj);


	}

}
