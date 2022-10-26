package org.mifosplatform.payments.razorpay.service;

/*
 * ::  SivaKishore  -  RazorpayIntegration
 */
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.mifosplatform.finance.officebalance.domain.OfficeBalance;
import org.mifosplatform.finance.officebalance.domain.OfficeBalanceRepository;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.payments.razorpay.data.InitializeTransactionRequest;
import org.mifosplatform.payments.razorpay.data.InitializeTransactionResponse;
import org.mifosplatform.payments.razorpay.data.InitializeTransactionResponseDTO;
import org.mifosplatform.payments.razorpay.data.OrderEnum;
import org.mifosplatform.payments.razorpay.data.OrderLockRequest;
import org.mifosplatform.payments.razorpay.exception.RazorpayCreateOrderFailedException;
import org.mifosplatform.payments.razorpay.exception.SignatureAuthenticationException;
import org.mifosplatform.payments.razorpay.exception.TransactionIdNotFoundException;
import org.mifosplatform.payments.razorpay.utils.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RazorpayInitializeTransactionServiceImpl implements RazorpayInitializeTransactionService {

	private final static Logger logger = LoggerFactory.getLogger(RazorpayInitializeTransactionServiceImpl.class);
	private final PaymentGatewayRepository paymentGatewayRepository;
	private final OfficeBalanceRepository officeBalanceRepository;
	//public static final String testKey = "rzp_test_86hAcbHA9LGiKv";
	//public static final String testSecretKey = "fXutITkVOKUt7x4kDV8Sz7LO";
	public static final String liveKey = "rzp_live_nFiyS0x0PreUjo";
	public static final String liveSecretKey = "ZJlAqAEDgh3NBKNO8FDvbtWC";
	@Autowired
	public RazorpayInitializeTransactionServiceImpl(PaymentGatewayRepository paymentGatewayRepository,
			OfficeBalanceRepository officeBalanceRepository) {
		this.paymentGatewayRepository = paymentGatewayRepository;
		this.officeBalanceRepository = officeBalanceRepository;
	}

	public InitializeTransactionResponseDTO createOrder(Long officeId,
			InitializeTransactionRequest initializeTransactionRequest) {

		try {
			logger.info("createOrder() - start");
			
			/** Amount conversion : SubUnits to Units */
			//BigDecimal divisor = new BigDecimal(100);
			//BigDecimal payment = (new BigDecimal(initializeTransactionRequest.getAmount())).divide(divisor);
			
			PaymentGateway paymentGateway = new PaymentGateway();
			paymentGateway.setDeviceId(officeId + "");
			paymentGateway.setAmountPaid(new BigDecimal(initializeTransactionRequest.getAmount()));
			paymentGateway.setType("OnlinePayment");
			/** setting tempid to PartyId **/
			paymentGateway.setPartyId(Long.toString(System.currentTimeMillis()));
			/** setting temp date **/
			paymentGateway.setPaymentDate(new Date());
			
			PaymentGateway pg	=	paymentGatewayRepository.save(paymentGateway);

			initializeTransactionRequest.setReceipt(pg.getId().toString());
			InitializeTransactionResponse response = this.initializeTransaction(initializeTransactionRequest);
			
			Instant instant = Instant.ofEpochSecond(response.getCreated_at());
			ZoneId zoneId = ZoneId.of("Asia/Kolkata");
			Date date = Date.from(ZonedDateTime.ofInstant(instant, zoneId).toInstant());


			pg.setPaymentDate(date);
			/** Mapping OrderId to PartyId **/
			pg.setPartyId((response.getId()));
			pg.setReceiptNo("RAZORPAY_" + response.getReceipt());
			pg.setSource("RAZORPAY");
			pg.setStatus(OrderEnum.CREATED.getValue());
			pg.setRemarks(response.getNotes().getNotes_key_1());
			paymentGatewayRepository.save(pg);

			InitializeTransactionResponseDTO responseDTO = new InitializeTransactionResponseDTO();
			responseDTO.setAmount(response.getAmount());
			responseDTO.setAmountDue(response.getAmount_due());
			responseDTO.setAmountPaid(response.getAmount_paid());
			responseDTO.setAttempts(response.getAttempts());
			responseDTO.setCreatedAt(response.getCreated_at());
			responseDTO.setCurrency(response.getCurrency());
			responseDTO.setEntity(response.getEntity());
			responseDTO.setOrderId(response.getId());
			responseDTO.setKey(liveKey);
			responseDTO.setNotes(response.getNotes());
			responseDTO.setOfferId(response.getOffer_id());
			responseDTO.setReceipt(response.getReceipt());
			responseDTO.setRedirectURL("https://13.235.37.142:8877/ngbplatform/api/v1/razorpay/orderlock");
			//responseDTO.setRedirectURL("https://localhost:8877/ngbplatform/api/v1/razorpay/orderlock");
			responseDTO.setStatus(response.getStatus());
			logger.info("response : "+responseDTO.toString());
			logger.info("createOrder() - end");

			return responseDTO;
		} catch (Exception e) {
			logger.error("createOrder()"+e.getMessage());
			e.printStackTrace();
			throw new RazorpayCreateOrderFailedException(e.getLocalizedMessage());
		}

	}

	public InitializeTransactionResponse initializeTransaction(
			InitializeTransactionRequest initializeTransactionRequest) {

		logger.info("initializeTransaction() - start");
		RestTemplate restTemplate = new RestTemplate();

		/** Amount conversion : Units to SubUnits */
		int amount = (initializeTransactionRequest.getAmount()) * 100;
		initializeTransactionRequest.setAmount(amount);

		try {
			String url = "https://api.razorpay.com/v1/orders";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("User-agent", "Application");
			headers.set("Authorization", getBasicAuthenticationHeader(liveKey, liveSecretKey));

			HttpEntity<InitializeTransactionRequest> entity = new HttpEntity<InitializeTransactionRequest>(
					initializeTransactionRequest, headers);
			logger.info("Request  : "+initializeTransactionRequest.toString());
			System.out.println("req : " + entity);
			ResponseEntity<InitializeTransactionResponse> response = restTemplate.postForEntity(url, entity,
					InitializeTransactionResponse.class);

			if (!response.getStatusCode().toString().equals("200")) {
				logger.error("Error status code: "+response.getStatusCode());
				throw new RazorpayCreateOrderFailedException("error code :" + response.getStatusCode());
			}

			System.out.println("res : " + response.getBody());
			logger.info("Response : "+response.getBody());
			logger.info("initializeTransaction() - end");
			return response.getBody();

		} catch (Exception e) {
			logger.error("Exception: "+e.getMessage());
			e.printStackTrace();
			throw new RazorpayCreateOrderFailedException(e.getMessage());
		}
	}

	public final String getBasicAuthenticationHeader(String username, String password) {
		String valueToEncode = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
	}

	@Override
	public Response processAndUpdate(OrderLockRequest orderLockRequest) {
		logger.info("OrderLock - ProcessAndUpdate - start");
		logger.info("Orderlock request : "+orderLockRequest.toString());
		URI indexPath = null;

		PaymentGateway razorpayOrder = paymentGatewayRepository
				.findPaymentDetailsByPartyId(orderLockRequest.getRazorpayOrderId());

		if (razorpayOrder == null) {
			throw new TransactionIdNotFoundException(orderLockRequest.getRazorpayOrderId());
		}

		if (razorpayOrder.getStatus().equals("Success")) {
			throw new TransactionIdNotFoundException("Transaction is already Success",
					orderLockRequest.getRazorpayOrderId());
		}

		razorpayOrder.setStatus(OrderEnum.ATTEMPTED.getValue());
		paymentGatewayRepository.save(razorpayOrder);

		boolean transactionStatus = this.verifyTransaction(razorpayOrder.getPartyId(), orderLockRequest);
		if (transactionStatus) {
			razorpayOrder.setStatus(OrderEnum.PAID.getValue());
			razorpayOrder.setPaymentId(orderLockRequest.getRazorpayPaymentId());
			paymentGatewayRepository.save(razorpayOrder);

			Long officeId = Long.parseLong(razorpayOrder.getDeviceId());
			logger.info("OfficeId : "+officeId);
			logger.info("amount : "+razorpayOrder.getAmountPaid());

			/** Updating Office Balance **/
			OfficeBalance officeBalance = this.officeBalanceRepository.findOneByOfficeId(officeId);
			if (officeBalance != null) {
				officeBalance.updateBalance("CREDIT", razorpayOrder.getAmountPaid());
			} else if (officeBalance == null) {
				BigDecimal balance = BigDecimal.ZERO.subtract(razorpayOrder.getAmountPaid());
				officeBalance = OfficeBalance.create(officeId, balance);
			}
			this.officeBalanceRepository.saveAndFlush(officeBalance);
		}
		try {
			indexPath = new URI("https://13.235.37.142:8877/#/profile");//("https://localhost:8877/#/profile")("https://freetv.ng/online-recharge.html");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		logger.info("OrderLock - ProcessAndUpdate - end");
		return Response.temporaryRedirect(indexPath).build();
	}

	private boolean verifyTransaction(String orderId, OrderLockRequest orderLockRequest) {

		logger.info("verifyTransaction - start");
		String generated_signature;
		try {
			generated_signature = Signature
					.calculateRFC2104HMAC(orderId + "|" + orderLockRequest.getRazorpayPaymentId(), liveSecretKey);
			if (generated_signature.equals(orderLockRequest.getRazorpaySignature())) {
				// payment is successful
				logger.info("verifyTransaction - end");
				return true;
			}
		} catch (SignatureException e) {
			logger.info("SignatureException : "+e.getMessage());
			e.printStackTrace();
			throw new SignatureAuthenticationException(e.getLocalizedMessage());
		}
		logger.info("verifyTransaction - end");
		return false;
	}

}
