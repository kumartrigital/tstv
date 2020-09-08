package org.mifosplatform.Revpay.order.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.Revpay.order.domain.RevPayOrderRepository;
import org.mifosplatform.Revpay.order.domain.RevpayOrder;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonElement;

@Service
public class RevPayOrderWritePlatformServiceImpl implements RevPayOrderWritePlatformService {

	private final RevPayOrderRepository revPayOrderRepo;

	private final PaymentGatewayRepository paymentGatewayRepository;

	private final OrderWritePlatformService orderWritePlatformService;

	private final FromJsonHelper fromApiJsonHelper;

	private HttpServletResponse httpServletResponse;

	@Autowired
	public RevPayOrderWritePlatformServiceImpl(final RevPayOrderRepository revPayOrderRepo,
			OrderWritePlatformService orderWritePlatformService, FromJsonHelper fromApiJsonHelper,
			PaymentGatewayRepository paymentGatewayRepository) {
		this.revPayOrderRepo = revPayOrderRepo;
		this.orderWritePlatformService = orderWritePlatformService;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.paymentGatewayRepository = paymentGatewayRepository;
	}

	@Override
	public CommandProcessingResult createOrder(JsonCommand command) {
		JSONObject revorder = null;
		try {
			PaymentGateway PaymentGateway = new PaymentGateway();
			PaymentGateway.setDeviceId(command.stringValueOfParameterName("stbNo"));
			PaymentGateway.setAmountPaid(new BigDecimal(command.stringValueOfParameterName("amount")));
			PaymentGateway.setPaymentId(getTxid());
			PaymentGateway.setPartyId(getTxid());
			PaymentGateway.setReceiptNo("TSTV_" + getTxid());
			PaymentGateway.setStatus("intiated");
			PaymentGateway.setPaymentDate(new Date());
			PaymentGateway.setSource("REVPAY");
			PaymentGateway.setReffernceId(command.stringValueOfParameterName("refId"));
			paymentGatewayRepository.save(PaymentGateway);

			revorder = new JSONObject();
			revorder.put("revorder", PaymentGateway);
			revorder.put("callbackUrl", "http://45.63.98.216:9091/ngbplatform/api/v1/revpay/orderlock");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new CommandProcessingResult(revorder);

	}

	public String getTxid() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		return String.valueOf(timestamp.getTime());
	}

	@Override
	public CommandProcessingResult lockOrder(Long txid, String flwref) {
		String result = null;
		Map<String, Object> myMap = null;
		try {
			String status = this.revTransactionStatus(txid);
			RevpayOrder revpayOrder = revPayOrderRepo.findOneByTxid(Long.toString(txid));
			if (status.equals("success")) {
				if (revpayOrder.getPurchaseType().equals("topup")) {
					JSONObject topupJson = new JSONObject();
					JSONObject paymentDetails = new JSONObject();
					paymentDetails.put("paymentType", "OnlinePayment");
					paymentDetails.put("transactionNo", txid);
					topupJson.put("stbNo", revpayOrder.getStbNo());
					topupJson.put("paymentDetails", paymentDetails);

					String orderId = revpayOrder.getRefId();

					final JsonElement renwalCommandElement = fromApiJsonHelper.parse(topupJson.toString());

					JsonCommand renwalCommandJson = new JsonCommand(null, renwalCommandElement.toString(),
							renwalCommandElement, fromApiJsonHelper, null, null, null, null, null, null, null, null,
							null, null, null, null);

					orderWritePlatformService.topUp(renwalCommandJson, Long.parseLong(orderId));
				} else if (revpayOrder.getPurchaseType().equals("tvod")) {
					JSONObject tvodJson = new JSONObject();
					JSONObject paymentDetails = new JSONObject();
					paymentDetails.put("paymentType", "OnlinePayment");
					paymentDetails.put("transactionNo", txid);
					tvodJson.put("stbNo", revpayOrder.getStbNo());
					tvodJson.put("itemId", revpayOrder.getRefId());
					tvodJson.put("paymentDetails", paymentDetails);

					final JsonElement renwalCommandElement = fromApiJsonHelper.parse(tvodJson.toString());

					JsonCommand renwalCommandJson = new JsonCommand(null, renwalCommandElement.toString(),
							renwalCommandElement, fromApiJsonHelper, null, null, null, null, null, null, null, null,
							null, null, null, null);

					orderWritePlatformService.tovdtopUp(renwalCommandJson);
				}
				revpayOrder.setFlwref(flwref);
				revpayOrder.setAction("completed");
				revpayOrder.setStatus("success");
				revpayOrder.setTransactionStatus(1);
				result = "Payment success";
			} else {
				result = "Payment failed";
				revpayOrder.setFlwref(flwref);
				revpayOrder.setAction("completed");
				revpayOrder.setStatus("failed");
				revpayOrder.setTransactionStatus(0);

			}
			myMap = new HashMap<String, Object>();
			myMap.put("message", "rev response");
			myMap.put("result", result);
			httpServletResponse.setHeader("Location", "https://45.63.98.216:8877/customer-details");
			httpServletResponse.setStatus(302);
			myMap.put("redirect", httpServletResponse);

			return new CommandProcessingResult(myMap);

		} catch (JSONException e) {
			e.printStackTrace();
			return new CommandProcessingResult(e);
		}

	}

	public String revTransactionStatus(Long txid) {
		String status = null;
		try {
			RestTemplate rest = new RestTemplate();

			String VERIFY_ENDPOINT = "https://api.ravepay.co/flwv3-pug/getpaidx/api/v2/verify";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			JSONObject revRequest = new JSONObject();
			revRequest.put("txref", txid);
			revRequest.put("SECKEY", "FLWSECK-c971cfc40fc35841a2445a0b782bb623-X");
			HttpEntity<String> request = new HttpEntity<>(revRequest.toString(), headers);
			String revResponse = rest.postForObject(VERIFY_ENDPOINT, request, String.class);
			JSONObject json = new JSONObject(revResponse.toString());
			status = json.getString("status");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return status;

	}

	private void handleCodeDataIntegrityIssues(JsonCommand command, Exception dve) {
		throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource.");
	}

}
