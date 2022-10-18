package org.mifosplatform.payments.razorpay.service;

import javax.ws.rs.core.Response;

import org.mifosplatform.payments.razorpay.data.InitializeTransactionRequest;
import org.mifosplatform.payments.razorpay.data.InitializeTransactionResponseDTO;
import org.mifosplatform.payments.razorpay.data.OrderLockRequest;

public interface RazorpayInitializeTransactionService {

	InitializeTransactionResponseDTO createOrder(Long officeId,
			InitializeTransactionRequest initializeTransactionRequest);

	Response processAndUpdate(OrderLockRequest orderLockRequest);

}
