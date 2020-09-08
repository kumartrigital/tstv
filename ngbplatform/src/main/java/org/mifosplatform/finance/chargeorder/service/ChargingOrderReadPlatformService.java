package org.mifosplatform.finance.chargeorder.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.taxmaster.data.TaxMappingRateData;
import org.mifosplatform.finance.chargeorder.data.BillingOrderData;
import org.mifosplatform.organisation.partneragreement.data.AgreementData;

public interface ChargingOrderReadPlatformService {

	List<BillingOrderData> retrieveOrderIds(Long clientId, LocalDateTime processDate);
	
	List<BillingOrderData> retrieveBillingOrderData(Long clientId,LocalDateTime localDate, Long planId);

	List<DiscountMasterData> retrieveDiscountOrders(Long orderId,Long orderPriceId);
	
	List<TaxMappingRateData> retrieveTaxMappingData(Long clientId, String chargeCode);

	List<TaxMappingRateData> retrieveDefaultTaxMappingData(Long clientId,String chargeCode);

	List<BillingOrderData> getReverseBillingOrderData(Long clientId,LocalDateTime disconnectionDate, Long orderId);

	AgreementData retriveClientOfficeDetails(Long clientId);

	AgreementData retrieveOfficeChargesCommission(Long id);

	List<Long> listOfInvoices(Long clientId, Long orderId);



}
