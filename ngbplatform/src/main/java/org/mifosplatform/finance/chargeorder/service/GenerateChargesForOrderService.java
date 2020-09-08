package org.mifosplatform.finance.chargeorder.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.mifosplatform.finance.chargeorder.data.BillingOrderData;
import org.mifosplatform.finance.chargeorder.data.ChargeData;
import org.mifosplatform.finance.chargeorder.domain.BillItem;
import org.mifosplatform.finance.chargeorder.domain.Charge;

public interface GenerateChargesForOrderService {

	List<ChargeData> generatebillingOrder(List<BillingOrderData> products);

	List<BillItem> generateCharge(List<ChargeData> billingOrderCommands);

	Map<String, List<Charge>> createNewChargesForServices(List<ChargeData> chargeDatas, Map<String, List<Charge>> groupOfCharges);	

	List<BillItem> createBillItemRecords(Map<String, List<Charge>> listOfCharges, Long clientId);

	Map<String, List<Charge>> calculateNewChargesForServices(List<ChargeData> billingOrderCommands,
			Map<String, List<Charge>> groupOfCharges);

	BigDecimal calculateChargeBillItemRecords(Map<String, List<Charge>> groupOfAdvanceCharges, Long clientId);


}
