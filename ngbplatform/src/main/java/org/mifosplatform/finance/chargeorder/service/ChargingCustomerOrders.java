package org.mifosplatform.finance.chargeorder.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.json.JSONObject;
import org.mifosplatform.finance.chargeorder.data.BillingOrderData;
import org.mifosplatform.finance.chargeorder.data.ChargeData;
import org.mifosplatform.finance.chargeorder.data.ProcessDate;
import org.mifosplatform.finance.chargeorder.domain.BillItem;
import org.mifosplatform.finance.chargeorder.domain.BillItemRepository;
import org.mifosplatform.finance.chargeorder.domain.Charge;
import org.mifosplatform.finance.chargeorder.exception.ProcessDateGreaterThanPlanEndDateException;
import org.mifosplatform.finance.chargeorder.serialization.ChargingOrderCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.clientservice.domain.ClientService;
import org.mifosplatform.portfolio.clientservice.domain.ClientServiceRepository;
import org.mifosplatform.portfolio.order.data.OrderData;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.portfolio.plan.exceptions.PlanNotFundException;
import org.mifosplatform.portfolio.slabRate.service.SlabRateWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class ChargingCustomerOrders {

	private final static Logger logger = LoggerFactory.getLogger(ChargingCustomerOrders.class);

	private final ChargingOrderReadPlatformService chargingOrderReadPlatformService;
	private final GenerateChargesForOrderService generateChargesForOrderService;
	private final ChargingOrderWritePlatformService chargingOrderWritePlatformService;
	private final ChargingOrderCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final ConfigurationRepository globalConfigurationRepository;
	private final OrderReadPlatformService orderReadPlatformService;
	private final FromJsonHelper fromJsonHelper;
	private final PlanRepository planRepository;
	private final SlabRateWritePlatformService slabRateWritePlatformService;
	private final OrderWritePlatformService orderWritePlatformService;
	private final FromJsonHelper fromApiJsonHelper;
	private final OrderRepository orderRepository;
	private final ClientServiceRepository clientServiceRepository;
	private final ConfigurationRepository configurationRepository;

	@Autowired
	public ChargingCustomerOrders(final ChargingOrderReadPlatformService chargingOrderReadPlatformService,
			final GenerateChargesForOrderService generateChargesForOrderService,
			final ChargingOrderWritePlatformService chargingOrderWritePlatformService,
			final ChargingOrderCommandFromApiJsonDeserializer apiJsonDeserializer,
			final ConfigurationRepository globalConfigurationRepository, final BillItemRepository billItemRepository,
			final OrderReadPlatformService orderReadPlatformService, final FromJsonHelper fromJsonHelper,
			final PlanRepository planRepository, final SlabRateWritePlatformService slabRateWritePlatformService,
			final @Lazy OrderWritePlatformService orderWritePlatformService, final FromJsonHelper fromApiJsonHelper,
			final OrderRepository orderRepository, final ClientServiceRepository clientServiceRepository,
			final ConfigurationRepository configurationRepository) {

		this.chargingOrderReadPlatformService = chargingOrderReadPlatformService;
		this.generateChargesForOrderService = generateChargesForOrderService;
		this.chargingOrderWritePlatformService = chargingOrderWritePlatformService;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.globalConfigurationRepository = globalConfigurationRepository;
		this.orderReadPlatformService = orderReadPlatformService;
		this.fromJsonHelper = fromJsonHelper;
		this.planRepository = planRepository;
		this.slabRateWritePlatformService = slabRateWritePlatformService;
		this.orderWritePlatformService = orderWritePlatformService;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.orderRepository = orderRepository;
		this.clientServiceRepository = clientServiceRepository;
		this.configurationRepository = configurationRepository;

	}

	public CommandProcessingResult createNewCharges(JsonCommand command) {

		try {
			logger.info("start ChargingCustomerOrders.createNewCharges()");
			// System.out.println("start ChargingCustomerOrders.createNewCharges()");
			this.apiJsonDeserializer.validateForCreate(command.json());
			LocalDateTime processDate = ProcessDate.fromJsonDateTime(command);
			List<BillItem> invoice = this.invoicingSingleClient(command.entityId(), processDate);
			logger.info("end ChargingCustomerOrders.createNewCharges()");
			// System.out.println("end ChargingCustomerOrders.createNewCharges()");

			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(invoice.get(0).getId()).build();
		} catch (Exception dve) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	public List<BillItem> invoicingSingleClient(Long clientId, LocalDateTime processDate) {

		logger.info("ClientId : " + clientId);
		logger.info("start ChargingCustomerOrders.invoicingSingleClient() processDate :" + processDate);
		// System.out.println("start ChargingCustomerOrders.invoicingSingleClient()
		// processDate :" + processDate);
		LocalDateTime initialProcessDate = processDate;
		DateTime nextBillableDate = null;
		Map<String, List<Charge>> groupOfCharges = null;
		// Get list of qualified orders of customer

		List<BillingOrderData> billingOrderDatas = chargingOrderReadPlatformService.retrieveOrderIds(clientId,
				processDate);
		logger.info("ChargingCustomerOrders.billingOrderDatas:" + billingOrderDatas.size());

		if (billingOrderDatas.size() != 0) {

			boolean prorataWithNextBillFlag = this
					.checkInvoiceConfigurations(ConfigurationConstants.CONFIG_PRORATA_WITH_NEXT_BILLING_CYCLE);

			groupOfCharges = new HashMap<String, List<Charge>>();

			Map<String, List<Charge>> groupOfAdvanceCharges = new HashMap<String, List<Charge>>(); // add global config
																									// to control
			Configuration isAdvance = this.globalConfigurationRepository
					.findOneByName(ConfigurationConstants.IS_ADVANCE); // isadvance plan

			Configuration billingPackageConfig = configurationRepository
					.findOneByName(ConfigurationConstants.BILLINGPLANID);
			Long bpkgId = Long.parseLong(billingPackageConfig.getValue());

			// flag

			if (true) {
				// if (null != isAdvance && isAdvance.isEnabled()) {
				logger.info("ChargingCustomerOrders.invoicingSingleClient() is advance");

				for (BillingOrderData billingOrderData : billingOrderDatas) {
					logger.info(
							"ChargingCustomerOrders.invoicingSingleClient().orderID" + billingOrderData.getOrderId());

					if (billingOrderData.getOrderId() == 68186) {
						logger.info("ChargingCustomerOrders.bug()");
					}

					if (billingOrderData.getBillEndDate() != null
							&& processDate.isAfter(billingOrderData.getBillEndDate().toLocalDateTime())) {

						logger.info("ChargingCustomerOrders.invoicingSingleClient().if condition");

						JSONObject disconnectCommand = new JSONObject();
						try {
							disconnectCommand.put("dateFormat", "dd MMMM yyyy");
							SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
							String disconnectedDate = formatter.format(new Date());
							disconnectCommand.put("disconnectionDate", disconnectedDate);
							disconnectCommand.put("disconnectReason", "plan end date reached");
							disconnectCommand.put("locale", "en");

						} catch (Exception e1) {
							e1.printStackTrace();
						}
						final JsonElement renwalCommandElement = fromApiJsonHelper.parse(disconnectCommand.toString());

						JsonCommand disconnectCommandJson = new JsonCommand(null, renwalCommandElement.toString(),
								renwalCommandElement, fromApiJsonHelper, null, null, null, null, null, null, null, null,
								null, null, null, null);

						orderWritePlatformService.disconnectOrder(disconnectCommandJson, billingOrderData.getOrderId());
						throw new ProcessDateGreaterThanPlanEndDateException("Process Date:: " + processDate
								+ "is greater than plan end date:: " + billingOrderData.getEndDate());

					} else {
						logger.info("ChargingCustomerOrders.invoicingSingleClient().else condition");

						nextBillableDate = billingOrderData.getNextBillableDate();
						logger.info(
								"ChargingCustomerOrders.invoicingSingleClient() nextBillableDate :" + nextBillableDate);

						if (prorataWithNextBillFlag && ("Y".equalsIgnoreCase(billingOrderData.getBillingAlign()))
								&& billingOrderData.getInvoiceTillDate() == null) {

							logger.info("ChargingCustomerOrders.invoicingSingleClient() if :" + nextBillableDate);

							LocalDateTime alignEndDate = new LocalDateTime(nextBillableDate).dayOfMonth()
									.withMaximumValue();

							if (!processDate.toDate().after(alignEndDate.toDate()))
								processDate = alignEndDate.plusDays(2);
						} else {
							logger.info("ChargingCustomerOrders.invoicingSingleClient() else :" + nextBillableDate);

							processDate = initialProcessDate;
						}

						while (processDate.toDateTime().isAfter(nextBillableDate)
								|| processDate.toDateTime().compareTo(nextBillableDate) == 0) {
							logger.info("ChargingCustomerOrders.invoicingSingleClient() processDate :" + processDate
									+ " and  nextBillableDate: " + nextBillableDate);

							// System.out.println("ChargingCustomerOrders.invoicingSingleClient()
							// processDate :" +processDate +" and nextBillableDate: "+nextBillableDate);

							Plan plan = planRepository.findOne(billingOrderData.getPlanId());

							if (plan.getIsAdvance() == 'y' || plan.getIsAdvance() == 'Y') {

								groupOfAdvanceCharges = getChargeLinesForServices(billingOrderData, clientId,
										processDate, groupOfAdvanceCharges);
								logger.info("ChargingCustomerOrders.invoicingSingleClient() groupOfAdvanceCharges :"
										+ groupOfAdvanceCharges);

							} else {
								logger.info("Plan is not enabled with is_Advance Flag");
							}

							if (!groupOfAdvanceCharges.isEmpty()
									&& groupOfAdvanceCharges.containsKey(billingOrderData.getOrderId().toString())) {
								List<Charge> charges = groupOfAdvanceCharges
										.get(billingOrderData.getOrderId().toString());
								nextBillableDate = new DateTime(nextBillableDate.plusDays(1).toDateTime());
							} else if (!groupOfAdvanceCharges.isEmpty()
									&& groupOfAdvanceCharges.containsKey(billingOrderData.getChargeCode())) {
								List<Charge> charges = groupOfAdvanceCharges.get(billingOrderData.getChargeCode());
								nextBillableDate = new DateTime(nextBillableDate.plusDays(1).toDateTime());
							}
						}

					}
				}
				// commented for testing

				if (!groupOfAdvanceCharges.isEmpty()) {

					BigDecimal totalAdvancecharge = this.generateChargesForOrderService
							.calculateChargeBillItemRecords(groupOfAdvanceCharges, clientId);

					List<ClientService> service = this.clientServiceRepository.findWithClientId(clientId);

					try {

						for (ClientService client : service) {
							if (!client.getStatus().equals("NEW")) { // expecting payment
								this.slabRateWritePlatformService.prepaidService(clientId, totalAdvancecharge);
							}
						}
					} catch (PlatformDataIntegrityException e) {

						List<Order> orders = orderRepository.findActiveOrdersOnlyByClientId(clientId);

						// for (BillingOrderData billingOrderDataNow : billingOrderDatas) {

						for (Order order : orders) {
							if (!order.getPlanId().equals(bpkgId)) {
								JSONObject disconnectCommand = new JSONObject();
								try {

									disconnectCommand.put("dateFormat", "dd MMMM yyyy");
									SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
									String disconnectedDate = formatter.format(new Date());

									disconnectCommand.put("disconnectionDate", disconnectedDate);
									disconnectCommand.put("disconnectReason", "Insufficient client balance");
									disconnectCommand.put("locale", "en");

								} catch (Exception e1) {
									e1.printStackTrace();
								}
								final JsonElement renwalCommandElement = fromApiJsonHelper
										.parse(disconnectCommand.toString());

								JsonCommand disconnectCommandJson = new JsonCommand(null,
										renwalCommandElement.toString(), renwalCommandElement, fromApiJsonHelper, null,
										null, null, null, null, null, null, null, null, null, null, null);

								orderWritePlatformService.disconnectOrder(disconnectCommandJson, order.getId());
							}

						} // returning a message json message
						throw new PlatformDataIntegrityException("Insufficient client balance",
								"Insufficient client balance", "Insufficient client balance");
					}
				}

			}

			for (BillingOrderData billingOrderData : billingOrderDatas) {

				nextBillableDate = billingOrderData.getNextBillableDate();
				if (prorataWithNextBillFlag && ("Y".equalsIgnoreCase(billingOrderData.getBillingAlign()))
						&& billingOrderData.getInvoiceTillDate() == null) {

					LocalDateTime alignEndDate = new LocalDateTime(nextBillableDate).dayOfMonth().withMaximumValue();

					if (!processDate.toDate().after(alignEndDate.toDate()))
						processDate = alignEndDate.plusDays(2);
				} else {
					processDate = initialProcessDate;
				}

				while (processDate.toDateTime().isAfter(nextBillableDate)
						|| processDate.toDateTime().compareTo(nextBillableDate) == 0) {

					groupOfCharges = chargeLinesForServices(billingOrderData, clientId, processDate, groupOfCharges);

					if (!groupOfCharges.isEmpty()
							&& groupOfCharges.containsKey(billingOrderData.getOrderId().toString())) {
						List<Charge> charges = groupOfCharges.get(billingOrderData.getOrderId().toString());
						nextBillableDate = new LocalDateTime(charges.get(charges.size() - 1).getEntDate()).plusDays(1)
								.toDateTime();
					} else if (!groupOfCharges.isEmpty()
							&& groupOfCharges.containsKey(billingOrderData.getChargeCode())) {
						List<Charge> charges = groupOfCharges.get(billingOrderData.getChargeCode());
						nextBillableDate = new LocalDateTime(charges.get(charges.size() - 1).getEntDate()).plusDays(1)
								.toDateTime();
					}
				}

			}
			logger.info("Group of charges" + groupOfCharges.toString());
			// System.out.println("Group of charges" + groupOfCharges.toString());

			return this.generateChargesForOrderService.createBillItemRecords(groupOfCharges, clientId);

		}

		else {
			List<BillItem> billItem = new ArrayList<BillItem>();
			System.out.println("ChargingCustomerOrders.invoicingSingleClient() end " + new Date());
			return billItem;
		}

	}

	public Map<String, List<Charge>> chargeLinesForServices(BillingOrderData billingOrderData, Long clientId,
			LocalDateTime processDate, Map<String, List<Charge>> groupOfCharges) {

		List<BillingOrderData> chargeServices = this.chargingOrderReadPlatformService.retrieveBillingOrderData(clientId,
				processDate, billingOrderData.getOrderId());

		List<ChargeData> chargeDatas = this.generateChargesForOrderService.generatebillingOrder(chargeServices);

		this.chargingOrderWritePlatformService.updateBillingOrder(chargeDatas);

		return this.generateChargesForOrderService.createNewChargesForServices(chargeDatas, groupOfCharges);
	}

	public Map<String, List<Charge>> getChargeLinesForServices(BillingOrderData billingOrderData, Long clientId,
			LocalDateTime processDate, Map<String, List<Charge>> groupOfCharges) {

		System.out.println("start ChargingCustomerOrders.getChargeLinesForServices() :" + groupOfCharges.size());
		List<BillingOrderData> chargeServices = this.chargingOrderReadPlatformService.retrieveBillingOrderData(clientId,
				processDate, billingOrderData.getOrderId());
		System.out.println("start ChargingCustomerOrders.getChargeLinesForServices() :" + chargeServices.size());

		List<ChargeData> chargeDatas = this.generateChargesForOrderService.generatebillingOrder(chargeServices);
		System.out.println("start ChargingCustomerOrders.getChargeLinesForServices() :" + chargeDatas.size());

		return this.generateChargesForOrderService.calculateNewChargesForServices(chargeDatas, groupOfCharges);
	}

	public List<BillItem> singleOrderInvoice(Long orderId, Long clientId, LocalDateTime processDate) {

		// Get qualified order complete details
		List<BillingOrderData> chargeServices = this.chargingOrderReadPlatformService.retrieveBillingOrderData(clientId,
				processDate, orderId);

		List<ChargeData> chargeDatas = this.generateChargesForOrderService.generatebillingOrder(chargeServices);

		// BillItem
		List<BillItem> billItems = this.generateChargesForOrderService.generateCharge(chargeDatas);

		// Update order-price
		this.chargingOrderWritePlatformService.updateBillingOrder(chargeDatas);
		logger.info("Top-Up:---------------------" + chargeDatas.get(0).getNextBillableDate());

		List<OrderData> orderData = this.orderReadPlatformService.orderDetailsForClientBalance(orderId);
		for (BillItem billItem : billItems) {

			JsonObject clientBalanceObject = new JsonObject();
			clientBalanceObject.addProperty("clientId", clientId);
			clientBalanceObject.addProperty("amount", billItem.getInvoiceAmount());
			clientBalanceObject.addProperty("isWalletEnable", false);
			clientBalanceObject.addProperty("clientServiceId", orderData.get(0).getClientServiceId());
			clientBalanceObject.addProperty("currencyId", billItem.getCurrencyId());
			clientBalanceObject.addProperty("locale", "en");

			final JsonElement clientServiceElementNew = fromJsonHelper.parse(clientBalanceObject.toString());
			JsonCommand clientBalanceCommand = new JsonCommand(null, clientServiceElementNew.toString(),
					clientServiceElementNew, fromJsonHelper, null, null, null, null, null, null, null, null, null, null,
					null, null);

			// Update Client Balance
			this.chargingOrderWritePlatformService.updateClientBalance(clientBalanceCommand);
		}
		return billItems;

	}

	public boolean checkInvoiceConfigurations(final String configName) {

		Configuration configuration = this.globalConfigurationRepository.findOneByName(configName);
		if (configuration != null && configuration.isEnabled()) {
			return true;
		} else {
			return false;
		}

	}

	/*
	 * public GenerateChargeData chargesForServices(BillingOrderData
	 * billingOrderData, Long clientId,LocalDatße processDate,BillItem
	 * invoice,boolean singleInvoiceFlag) {
	 * 
	 * // Get qualified order complete details List<BillingOrderData> products =
	 * this.chargingOrderReadPlatformService.retrieveBillingOrderData(clientId,
	 * processDate,billingOrderData.getOrderId());
	 * 
	 * List<ChargeData> chargeDatas =
	 * this.generateChargesForOrderService.generatebillingOrder(products);
	 * 
	 * if(singleInvoiceFlag){
	 * 
	 * invoice =
	 * this.generateChargesForOrderService.generateMultiOrderCharges(chargeDatas,
	 * invoice);
	 * 
	 * // Update order-price
	 * this.chargingOrderWritePlatformService.updateBillingOrder(chargeDatas);
	 * System.out.println("---------------------"+
	 * chargeDatas.get(0).getNextBillableDate());
	 * 
	 * return new GenerateChargeData(clientId,
	 * chargeDatas.get(0).getNextBillableDate(),
	 * invoice.getInvoiceAmount(),invoice);
	 * 
	 * }else{
	 * 
	 * // BillItem BillItem singleInvoice =
	 * this.generateChargesForOrderService.generateCharge(chargeDatas);
	 * 
	 * // Update order-price
	 * this.chargingOrderWritePlatformService.updateBillingOrder(chargeDatas);
	 * System.out.println("---------------------"+
	 * chargeDatas.get(0).getNextBillableDate());
	 * 
	 * // Update Client Balance
	 * this.chargingOrderWritePlatformService.updateClientBalance(singleInvoice.
	 * getInvoiceAmount(), clientId, false);
	 * 
	 * return new GenerateChargeData(clientId,
	 * chargeDatas.get(0).getNextBillableDate(),singleInvoice.getInvoiceAmount(),
	 * singleInvoice); } return new GenerateChargeData(clientId,
	 * chargeDatas.get(0).getNextBillableDate(),
	 * invoice.getInvoiceAmount(),invoice); }
	 */

	/*
	 * if (singleInvoiceFlag) {
	 * 
	 * this.billItemRepository.save(invoiceData.getInvoice());
	 * 
	 * // Update Client Balance
	 * this.chargingOrderWritePlatformService.updateClientBalance(invoiceData.
	 * getInvoice().getInvoiceAmount(), clientId,false); }
	 */
}
