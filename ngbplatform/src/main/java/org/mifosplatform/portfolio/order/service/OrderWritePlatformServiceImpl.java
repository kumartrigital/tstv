package org.mifosplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityExistsException;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.mifosplatform.billing.planprice.domain.Price;
import org.mifosplatform.billing.planprice.domain.PriceRepository;
import org.mifosplatform.billing.planprice.exceptions.ChargeCodeAndContractPeriodException;
import org.mifosplatform.billing.planprice.exceptions.ContractNotNullException;
import org.mifosplatform.billing.planprice.exceptions.PriceNotFoundException;
import org.mifosplatform.billing.promotioncodes.domain.PromotionCodeMaster;
import org.mifosplatform.billing.promotioncodes.domain.PromotionCodeRepository;
import org.mifosplatform.billing.promotioncodes.exception.PromotionCodeNotFoundException;
import org.mifosplatform.cms.eventmaster.domain.EventMasterRepository;
import org.mifosplatform.cms.eventorder.api.EventOrderApiResource;
import org.mifosplatform.cms.eventorder.service.PrepareRequestWriteplatformService;
import org.mifosplatform.cms.eventprice.data.EventPriceData;
import org.mifosplatform.cms.eventprice.service.EventPriceReadPlatformService;
import org.mifosplatform.cms.media.domain.MediaAsset;
import org.mifosplatform.cms.media.exceptions.NoMoviesFoundException;
import org.mifosplatform.cms.mediadetails.domain.MediaAssetRepository;
import org.mifosplatform.cms.mediadetails.domain.MediaassetLocation;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.service.CrmServices;
import org.mifosplatform.finance.chargeorder.api.ChargingOrderApiResourse;
import org.mifosplatform.finance.chargeorder.domain.BillItem;
import org.mifosplatform.finance.chargeorder.service.ChargingCustomerOrders;
import org.mifosplatform.finance.chargeorder.service.ReverseCharges;
import org.mifosplatform.finance.paymentsgateway.domain.PaypalRecurringBilling;
import org.mifosplatform.finance.paymentsgateway.domain.PaypalRecurringBillingRepository;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.configuration.domain.EnumDomainService;
import org.mifosplatform.infrastructure.configuration.domain.EnumDomainServiceRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.DateTimeUtils;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.inview.service.InviewWritePlatformService;
import org.mifosplatform.logistics.item.service.ItemReadPlatformService;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetails;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.mifosplatform.logistics.itemdetails.exception.SerialNumberNotFoundException;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.redemption.api.RedemptionApiResource;
import org.mifosplatform.organisation.redemption.exception.PinNumberNotFoundException;
import org.mifosplatform.organisation.voucher.data.VoucherData;
import org.mifosplatform.organisation.voucher.domain.VoucherDetailsRepository;
import org.mifosplatform.organisation.voucher.exception.VoucherIsNotProductTypeException;
import org.mifosplatform.organisation.voucher.service.VoucherReadPlatformService;
/*import org.mifosplatform.portfolio.allocation.domain.HardwareAssociationRepository;*/
import org.mifosplatform.portfolio.allocation.service.AllocationReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
/*import org.mifosplatform.portfolio.association.service.HardwareAssociationReadplatformService;
 import org.mifosplatform.portfolio.association.service.HardwareAssociationWriteplatformService;*/
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.clientservice.api.ClientServiceApiResource;
import org.mifosplatform.portfolio.clientservice.data.ClientServiceData;
import org.mifosplatform.portfolio.clientservice.domain.ClientService;
import org.mifosplatform.portfolio.clientservice.domain.ClientServiceRepository;
import org.mifosplatform.portfolio.clientservice.service.ClientServiceReadPlatformService;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.contract.domain.Contract;
import org.mifosplatform.portfolio.contract.domain.ContractRepository;
import org.mifosplatform.portfolio.contract.exception.ContractPeriodNotFoundException;
import org.mifosplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.mifosplatform.portfolio.order.api.OrdersApiResource;
import org.mifosplatform.portfolio.order.data.OrderData;
import org.mifosplatform.portfolio.order.data.OrderStatusEnumaration;
import org.mifosplatform.portfolio.order.data.UserActionStatusEnumaration;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderAddons;
import org.mifosplatform.portfolio.order.domain.OrderAddonsRepository;
import org.mifosplatform.portfolio.order.domain.OrderDiscount;
import org.mifosplatform.portfolio.order.domain.OrderHistory;
import org.mifosplatform.portfolio.order.domain.OrderHistoryRepository;
import org.mifosplatform.portfolio.order.domain.OrderLine;
import org.mifosplatform.portfolio.order.domain.OrderPrice;
import org.mifosplatform.portfolio.order.domain.OrderPriceRepository;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.domain.PaymentFollowup;
import org.mifosplatform.portfolio.order.domain.PaymentFollowupRepository;
import org.mifosplatform.portfolio.order.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.order.exceptions.NoOrdersFoundException;
import org.mifosplatform.portfolio.order.exceptions.OrderNotFoundException;
import org.mifosplatform.portfolio.order.serialization.OrderCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.portfolio.plan.exceptions.PlanAlreadyAddedException;
import org.mifosplatform.portfolio.plan.exceptions.PlanNotFundException;
import org.mifosplatform.portfolio.plan.service.PlanReadPlatformService;
import org.mifosplatform.portfolio.product.domain.Product;
import org.mifosplatform.portfolio.product.domain.ProductRepository;
import org.mifosplatform.portfolio.service.domain.ServiceMasterRepository;
import org.mifosplatform.portfolio.slabRate.service.SlabRateWritePlatformService;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequest;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequsetRepository;
import org.mifosplatform.provisioning.preparerequest.exception.PrepareRequestActivationException;
import org.mifosplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.mifosplatform.provisioning.provisioning.data.ProvisioningData;
import org.mifosplatform.provisioning.provisioning.domain.ProvisioningRequest;
import org.mifosplatform.provisioning.provisioning.domain.ProvisioningRequestRepository;
import org.mifosplatform.provisioning.provisioning.exceptions.ProvisioningRequestNotFoundException;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningReadPlatformService;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.domain.EventAction;
import org.mifosplatform.workflow.eventaction.domain.EventActionRepository;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.mifosplatform.workflow.eventvalidation.service.EventValidationReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class OrderWritePlatformServiceImpl implements OrderWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(OrderWritePlatformServiceImpl.class);

	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	private final ProvisioningWritePlatformService provisioningWritePlatformService;
	private final PlanRepository planRepository;
	private final ReverseCharges reverseInvoice;
	private final PlatformSecurityContext context;
	private final OrderRepository orderRepository;
	private final OrderAddonsRepository orderAddonsRepository;
	private final PriceRepository priceRepository;
	private final OrderAssembler orderAssembler;
	private final ClientRepository clientRepository;
	private final EventValidationReadPlatformService eventValidationReadPlatformService;
	private final PrepareRequestReadplatformService prepareRequestReadplatformService;
	private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
	private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
	private final PrepareRequestWriteplatformService prepareRequestWriteplatformService;
	private final ClientServiceApiResource clientServiceApiResource;
	private final MediaAssetRepository mediaAssetRepository;
	private final ChargingOrderApiResourse chargingOrderApiResourse;
	/*
	 * private final HardwareAssociationWriteplatformService
	 * associationWriteplatformService;
	 */
	private final OrderReadPlatformService orderReadPlatformService;
	private final ServiceMasterRepository serviceMasterRepository;
	private final PrepareRequsetRepository prepareRequsetRepository;
	private final PaymentFollowupRepository paymentFollowupRepository;
	private final CodeValueRepository codeValueRepository;
	/* private final HardwareAssociationRepository Repository; */
	private final EnumDomainServiceRepository enumDomainServiceRepository;
	private final AllocationReadPlatformService allocationReadPlatformService;
	private final OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ContractRepository subscriptionRepository;
	private final ConfigurationRepository configurationRepository;
	private final PromotionCodeRepository promotionCodeRepository;
	private final InviewWritePlatformService inviewWritePlatformService;
	/*
	 * private final HardwareAssociationReadplatformService
	 * hardwareAssociationReadplatformService;
	 */
	private final ChargeCodeRepository chargeCodeRepository;
	private final OrderPriceRepository orderPriceRepository;
	private final EventActionRepository eventActionRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
	private final PaypalRecurringBillingRepository paypalRecurringBillingRepository;
	private final ContractRepository contractRepository;
	private final ChargingCustomerOrders invoiceClient;
	private final FromJsonHelper fromJsonHelper;
	private final PlanReadPlatformService planReadPlatformService;
	private final ClientServiceRepository clientServiceRepository;
	private final FromJsonHelper fromApiJsonHelper;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final ProductRepository productRepository;
	private final CrmServices crmServices;
	private final OrdersApiResource orderApiResource;
	private final ProvisioningReadPlatformService provisioningReadPlatformService;
	private final ClientServiceReadPlatformService clientServiceReadPlatformService;
	private final ClientReadPlatformService clientReadPlatformService;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService;
	private final VoucherDetailsRepository voucherDetailsRepository;
	private final RedemptionApiResource redemptionApiResource;
	private final ItemReadPlatformService itemReadPlatformService;
	private final EventOrderApiResource eventOrderApiResource;
	private final EventMasterRepository eventMasterRepository;
	private final VoucherReadPlatformService voucherReadPlatformService;
	private final ItemDetailsRepository itemDetailsRepository;
	private final EventPriceReadPlatformService eventPriceReadPlatformService;
	private final SlabRateWritePlatformService slabRateReadPlatformService;
	private final ProvisioningRequestRepository provisioningRequestRepository;
	private final static String VALUE_PINTYPE = "VALUE";
	private final static String PRODUCT_PINTYPE = "PRODUCT";

	@Autowired
	public OrderWritePlatformServiceImpl(final PlatformSecurityContext context, final OrderRepository orderRepository,
			final PlanRepository planRepository, final OrderPriceRepository OrderPriceRepository,
			final CodeValueRepository codeRepository, final ServiceMasterRepository serviceMasterRepository,
			final EnumDomainServiceRepository enumDomainServiceRepository,
			final ContractRepository subscriptionRepository,
			final OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer, final ReverseCharges reverseInvoice,
			final PrepareRequestWriteplatformService prepareRequestWriteplatformService,
			final OrderHistoryRepository orderHistoryRepository, final ConfigurationRepository configurationRepository,
			final AllocationReadPlatformService allocationReadPlatformService,
			/*
			 * final HardwareAssociationWriteplatformService
			 * associationWriteplatformService,
			 */
			final PrepareRequestReadplatformService prepareRequestReadplatformService,
			final OrderReadPlatformService orderReadPlatformService, final OrderAddonsRepository addonsRepository,
			final OrderAssembler orderAssembler, final ProcessRequestRepository processRequestRepository,
			/*
			 * final HardwareAssociationReadplatformService
			 * hardwareAssociationReadplatformService,
			 */
			final PrepareRequsetRepository prepareRequsetRepository,
			final PromotionCodeRepository promotionCodeRepository, final ContractRepository contractRepository,
			final ClientRepository clientRepository,
			final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
			final ActiondetailsWritePlatformService actiondetailsWritePlatformService,
			final EventValidationReadPlatformService eventValidationReadPlatformService,
			final EventActionRepository eventActionRepository,
			final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
			final ChargingCustomerOrders invoiceClient,
			/* final HardwareAssociationRepository associationRepository, */
			final ProvisioningWritePlatformService provisioningWritePlatformService,
			final PaymentFollowupRepository paymentFollowupRepository, final PriceRepository priceRepository,
			final ChargeCodeRepository chargeCodeRepository,
			final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,
			final PaypalRecurringBillingRepository paypalRecurringBillingRepository,
			final FromJsonHelper fromJsonHelper, final MCodeReadPlatformService mCodeReadPlatformService,
			final PlanReadPlatformService planReadPlatformService,
			final ClientServiceRepository clientServiceRepository, final FromJsonHelper fromApiJsonHelper,
			final ProductRepository productRepository, final CrmServices crmServices,
			@Lazy final OrdersApiResource ordersApiResource, final ClientServiceApiResource clientServiceApiResource,
			final ProvisioningReadPlatformService provisioningReadPlatformService,
			final ClientServiceReadPlatformService clientServiceReadPlatformService,
			final ClientReadPlatformService clientReadPlatformService,
			final OfficeReadPlatformService officeReadPlatformService,
			final InviewWritePlatformService inviewWritePlatformService,
			final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService,
			final VoucherDetailsRepository voucherDetailsRepository, final MediaAssetRepository mediaAssetRepository,
			final RedemptionApiResource redemptionApiResource, final ItemReadPlatformService itemReadPlatformService,
			final EventOrderApiResource eventOrderApiResource, final EventMasterRepository eventMasterRepository,
			final VoucherReadPlatformService voucherReadPlatformService,
			final ItemDetailsRepository itemDetailsRepository,
			final EventPriceReadPlatformService eventPriceReadPlatformService,
			final ChargingOrderApiResourse chargingOrderApiResourse,
			final SlabRateWritePlatformService slabRateReadPlatformService,
			final ProvisioningRequestRepository provisioningRequestRepository) {

		this.context = context;
		this.reverseInvoice = reverseInvoice;
		this.subscriptionRepository = subscriptionRepository;
		this.serviceMasterRepository = serviceMasterRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.configurationRepository = configurationRepository;
		this.prepareRequsetRepository = prepareRequsetRepository;
		this.orderReadPlatformService = orderReadPlatformService;
		this.paymentFollowupRepository = paymentFollowupRepository;
		this.enumDomainServiceRepository = enumDomainServiceRepository;
		this.allocationReadPlatformService = allocationReadPlatformService;
		this.orderAssembler = orderAssembler;
		this.eventValidationReadPlatformService = eventValidationReadPlatformService;
		this.priceRepository = priceRepository;
		this.planRepository = planRepository;
		this.orderRepository = orderRepository;
		this.orderAddonsRepository = addonsRepository;
		this.clientRepository = clientRepository;
		this.codeValueRepository = codeRepository;
		this.promotionCodeRepository = promotionCodeRepository;
		this.provisioningWritePlatformService = provisioningWritePlatformService;
		this.prepareRequestReadplatformService = prepareRequestReadplatformService;
		this.actiondetailsWritePlatformService = actiondetailsWritePlatformService;
		this.contractPeriodReadPlatformService = contractPeriodReadPlatformService;
		this.prepareRequestWriteplatformService = prepareRequestWriteplatformService;
		this.chargingOrderApiResourse = chargingOrderApiResourse;
		/*
		 * this.hardwareAssociationReadplatformService =
		 * hardwareAssociationReadplatformService;
		 */
		this.chargeCodeRepository = chargeCodeRepository;
		this.orderPriceRepository = OrderPriceRepository;
		this.eventActionRepository = eventActionRepository;
		/* this.associationRepository = associationRepository; */
		this.orderHistoryRepository = orderHistoryRepository;
		/*
		 * this.associationWriteplatformService = associationWriteplatformService;
		 */
		this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
		this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
		this.paypalRecurringBillingRepository = paypalRecurringBillingRepository;
		this.contractRepository = contractRepository;
		this.invoiceClient = invoiceClient;
		this.fromJsonHelper = fromJsonHelper;
		this.planReadPlatformService = planReadPlatformService;
		this.clientServiceRepository = clientServiceRepository;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.mCodeReadPlatformService = mCodeReadPlatformService;
		this.productRepository = productRepository;
		this.crmServices = crmServices;
		this.orderApiResource = ordersApiResource;
		this.clientServiceApiResource = clientServiceApiResource;
		this.provisioningReadPlatformService = provisioningReadPlatformService;
		this.clientServiceReadPlatformService = clientServiceReadPlatformService;
		this.clientReadPlatformService = clientReadPlatformService;
		this.officeReadPlatformService = officeReadPlatformService;
		this.inviewWritePlatformService = inviewWritePlatformService;
		this.portfolioCommandSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
		this.voucherDetailsRepository = voucherDetailsRepository;
		this.mediaAssetRepository = mediaAssetRepository;
		this.redemptionApiResource = redemptionApiResource;
		this.itemReadPlatformService = itemReadPlatformService;
		this.eventOrderApiResource = eventOrderApiResource;
		this.eventMasterRepository = eventMasterRepository;
		this.voucherReadPlatformService = voucherReadPlatformService;
		this.itemDetailsRepository = itemDetailsRepository;
		this.eventPriceReadPlatformService = eventPriceReadPlatformService;
		this.slabRateReadPlatformService = slabRateReadPlatformService;
		this.provisioningRequestRepository = provisioningRequestRepository;
	}

	@Override
	@Transactional
	public CommandProcessingResult createOrder(Long clientId, JsonCommand command, Order oldOrder) {

		try {

			List<Long> planIds = new ArrayList<Long>();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			String serialnum = command.stringValueOfParameterNamed("serialnumber");
			String allocationType = command.stringValueOfParameterNamed("allocation_type");
			final Long userId = getUserId();

			checkingContractPeriodAndBillfrequncyValidation(command.longValueOfParameterNamed("contractPeriod"),
					command.stringValueOfParameterNamed("paytermCode"));

			// Check for Custome_Validation
			this.eventValidationReadPlatformService.checkForCustomValidations(clientId,
					EventActionConstants.EVENT_CREATE_ORDER, command.json(), userId);

			Plan plan = this.planRepository.findPlanCheckDeletedStatus(command.longValueOfParameterNamed("planCode"));
			planIds = this.orderReadPlatformService.retrieveClientActiveOrders(clientId);
			if (planIds.contains(plan.getId()))
				throw new PlanAlreadyAddedException(plan.getDescription());

			Order order = this.orderAssembler.assembleOrderDetails(command, clientId, plan);
			System.out.println("OrderWritePlatformServiceImpl.createOrder( :)" + order.getEndDate());
			order.setActiveDate(new LocalDateTime());
			order.setStartDate(new LocalDateTime());
			// this condition is for updating order_No for multiple plans
			if (command.stringValueOfParameterName("orderNo") != null) {
				order.setOrderNo(command.stringValueOfParameterName("orderNo"));
			}

			System.out.println("OrderWritePlatformServiceImpl.createOrder( :)" + order.getEndDate());

			this.orderRepository.saveAndFlush(order);

			boolean isNewPlan = command.booleanPrimitiveValueOfParameterNamed("isNewplan");
			String requstStatus = UserActionStatusTypeEnum.ACTIVATION.toString();

			if (isNewPlan) {
				if (command.stringValueOfParameterName("orderNo") == null) {
					final AccountNumberGenerator orderNoGenerator = this.accountIdentifierGeneratorFactory
							.determineClientAccountNoGenerator(order.getId());
					order.updateOrderNum(orderNoGenerator.generate(),
							this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.NUMBER_GENERATOR));
				} else {
					order.setOrderNo(command.stringValueOfParameterNamed("orderNo"));
				}

				// ServiceMaster service =
				// this.serviceMasterRepository.findOneByServiceCode(planDetails.iterator().next().getServiceCode());
				Long commandId = Long.valueOf(0);

				/*
				 * if (service != null && service.isAuto() == 'Y' &&
				 * !plan.getProvisionSystem().equalsIgnoreCase("None")) {
				 * CommandProcessingResult processingResult =
				 * this.prepareRequestWriteplatformService .prepareNewRequest(order, plan,
				 * requstStatus); commandId = processingResult.commandId(); }
				 */

				// For Order History
				OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
						DateTimeUtils.getLocalDateTimeOfTenant(), commandId, requstStatus, userId, null);

				logger.info(orderHistory.toString());
				this.orderHistoryRepository.save(orderHistory);

			}

			if (plan.getIsAdvance() == 'Y' || plan.getIsAdvance() == 'y') {
				// charging
				JSONObject jsonObject = new JSONObject();
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
				try {
					jsonObject.put("dateFormat", "dd MMMM yyyy HH:mm:ss");
					jsonObject.put("locale", "en");
					jsonObject.put("systemDate", dateFormat.format(order.getStartDate()));
					this.chargingOrderApiResourse.createChargesToOrders(order.getClientId(), jsonObject.toString());

				} catch (Exception e) {
					throw new PlatformDataIntegrityException("error.msg.charge.exception",
							"error.message.charging.exception");
				}
			}

			if (plan.getProvisionSystem().equalsIgnoreCase("None")) {

				Client client = this.clientRepository.findOne(clientId);
				client.setStatus(ClientStatus.ACTIVE.getValue());
				this.clientRepository.save(client);

			}

			/*
			 * if (isNewPlan) {
			 * processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM,
			 * clientId, order.getId().toString(), "ACTIVATION"); }
			 */
			Order order1 = this.orderRepository.findOne(order.getId());

			ClientService clientService = null;
			clientService = this.clientServiceRepository.findOne(command.longValueOfParameterNamed("clientServiceId"));
			if (clientService.getStatus().equalsIgnoreCase("NEW")) {
				order1.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.NEW).getId());
			} else {
				order1.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
			}

			/*
			 * if ("Y".equalsIgnoreCase(String.valueOf(plan.isPrepaid()))) {
			 * order1.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE
			 * ).getId()); }
			 */
			System.out.println("OrderWritePlatformServiceImpl.createOrder( :)" + order.getEndDate());
			Configuration billingPackageConfig = configurationRepository
					.findOneByName(ConfigurationConstants.BILLINGPLANID);
			Long bpkgId = Long.parseLong(billingPackageConfig.getValue());
			if (order1.getPlanId().equals(bpkgId))
				if (plan.getProvisionSystem().equalsIgnoreCase("None")) {
					order1.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
				}
			order = this.orderRepository.saveAndFlush(order1);

			if (!plan.getProvisionSystem().equalsIgnoreCase("None")) {
				this.provisioningRequesting(order, oldOrder, plan.isPrepaid());
			} else {
				clientService = this.clientServiceRepository.findOne(order.getClientServiceId());
				clientService.setStatus("ACTIVE");
				clientService = this.clientServiceRepository.saveAndFlush(clientService);
			}

			/** Logic for daily billing.-Start */

			Configuration is_aggregater = configurationRepository.findOneByName(ConfigurationConstants.AGGREGATER);
			if (null != is_aggregater && is_aggregater.isEnabled()) {
				updateNextBillableDate(order, bpkgId);
				checkAndIncludeBaseBillingPackage(bpkgId, clientId, (order.getPrice().get(0).getPrice().doubleValue()));
			}

			/** Logic for daily billing.-End */
			return new CommandProcessingResult(order1.getId(), order1.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void provisioningRequesting(Order order, Order oldOrder, char isPrepaid) {
		if ("N".equalsIgnoreCase(String.valueOf(isPrepaid))) {
			if (order.getStatus().toString()
					.equalsIgnoreCase(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId().toString())
					|| order.getStatus().toString().equalsIgnoreCase(
							OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.NEW).getId().toString())) {
				ClientService clientService = this.clientServiceRepository.findOne(order.getClientServiceId());
				if ("PROCESSING".equalsIgnoreCase(clientService.getStatus())
						|| "ACTIVE".equalsIgnoreCase(clientService.getStatus())) {
					JsonObject provisioningObject = new JsonObject();
					if (oldOrder == null) {
						provisioningObject.addProperty("requestType", ProvisioningApiConstants.REQUEST_ADD_PLAN);
						provisioningObject.addProperty("clientServiceId", order.getClientServiceId());
					} else {
						provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
						provisioningObject.addProperty("oldOrderId", oldOrder.getId());
						provisioningObject.addProperty("clientServiceId", order.getClientServiceId());
					}
					JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
							fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
					List<Order> orders = new ArrayList<Order>();
					orders.add(order);
					this.provisioningWritePlatformService.createProvisioningRequest(orders, com, true);
				}

			}
		} else {
			JsonObject provisioningObject = new JsonObject();
			if (oldOrder == null) {
				provisioningObject.addProperty("requestType", ProvisioningApiConstants.REQUEST_ADD_PLAN);
				provisioningObject.addProperty("clientServiceId", order.getClientServiceId());
			} else {
				provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
				provisioningObject.addProperty("oldOrderId", oldOrder.getId());
				provisioningObject.addProperty("clientServiceId", order.getClientServiceId());
			}

			ClientService clientService = this.clientServiceRepository.findOne(order.getClientServiceId());
			if ("NEW".equalsIgnoreCase(clientService.getStatus())) {
				provisioningObject.addProperty("requestType", ProvisioningApiConstants.REQUEST_ACTIVATION);
				clientService.setStatus("PROCESSING");
				this.clientServiceRepository.saveAndFlush(clientService);
			}

			JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject, fromJsonHelper,
					null, null, null, null, null, null, null, null, null, null, null, null);
			List<Order> orders = new ArrayList<Order>();
			orders.add(order);
			this.provisioningWritePlatformService.createProvisioningRequest(orders, com, true);

		}

	}

	@Override
	public void processNotifyMessages(String eventName, Long clientId, String orderId, String actionType) {

		List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService
				.retrieveActionDetails(eventName);

		if (actionDetaislDatas.size() != 0) {
			this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, clientId, orderId, actionType);
		}
	}

	private void handleCodeDataIntegrityIssues(JsonCommand command, Exception dve) {
		throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource.");
	}

	@Transactional
	@Override
	public CommandProcessingResult updateOrderPrice(Long orderId, JsonCommand command) {
		try {
			final Long userId = context.authenticatedUser().getId();
			final Order order = retrieveOrderById(orderId);

			Long orderPriceId = command.longValueOfParameterNamed("priceId");
			BigDecimal price = command.bigDecimalValueOfParameterNamed("price");
			OrderPrice orderPrice = this.orderPriceRepository.findOne(orderPriceId);
			orderPrice.setPrice(price);
			this.orderPriceRepository.save(orderPrice);

			// For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					DateTimeUtils.getLocalDateTimeOfTenant(), null, "UPDATE PRICE", userId, null);
			this.orderHistoryRepository.save(orderHistory);

			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(order.getId())
					.with(null).withClientId(order.getClientId()).build();

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private Order retrieveOrderById(Long orderId) {
		final Order order = this.orderRepository.findOne(orderId);
		if (order == null) {
			throw new NoOrdersFoundException(orderId.toString());
		}
		return order;
	}

	@Transactional
	@Override
	public CommandProcessingResult deleteOrder(Long orderId, JsonCommand command) {

		final Long userId = this.context.authenticatedUser().getId();
		Order order = this.orderRepository.findOne(orderId);
		List<OrderLine> orderline = order.getServices();
		List<OrderPrice> orderPrices = order.getPrice();
		Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());
		if (!plan.getProvisionSystem().equalsIgnoreCase("None")) {
			List<Long> prepareIds = this.prepareRequestReadplatformService.getPrepareRequestDetails(orderId);
			if (prepareIds.isEmpty()) {
				throw new PrepareRequestActivationException();
			}
			for (Long id : prepareIds) {
				PrepareRequest prepareRequest = this.prepareRequsetRepository.findOne(id);
				prepareRequest.setCancelStatus("CANCEL");
				this.prepareRequsetRepository.save(prepareRequest);
			}
		}
		for (OrderPrice price : orderPrices) {
			price.delete();
		}
		for (OrderLine orderData : orderline) {
			orderData.delete();
		}
		order.delete();
		this.orderRepository.save(order);

		// For Order History
		OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
				DateTimeUtils.getLocalDateTimeOfTenant(), null, "CANCELLED", userId, null);
		this.orderHistoryRepository.save(orderHistory);
		return new CommandProcessingResult(order.getId(), order.getClientId());
	}

	@Override
	public CommandProcessingResult disconnectOrder(final JsonCommand command, final Long orderId) {

		try {
			Configuration billingPackageConfig = configurationRepository
					.findOneByName(ConfigurationConstants.BILLINGPLANID);
			Long BillingPkgId = Long.parseLong(billingPackageConfig.getValue());

			Order order = this.orderRepository.findOne(orderId);

			this.fromApiJsonDeserializer.validateForDisconnectOrder(command.json(), order.getPlanId(), BillingPkgId);

			final LocalDateTime disconnectionDate = command.localDateTimeValueOfParameterNamed("disconnectionDate");
			LocalDate currentDate = DateUtils.getLocalDateOfTenant();
			currentDate.toDate();
			// final Configuration configurationProperty =
			// this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);

			List<OrderPrice> orderPrices = order.getPrice();
			for (OrderPrice price : orderPrices) {
				price.updateDates(disconnectionDate);
			}
			final Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());
			Long orderStatus = null;

			if ("None".equalsIgnoreCase(plan.getProvisionSystem())) {
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.DISCONNECTED).getId();
			} else {
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
			}
			/*
			 * if (configurationProperty.isEnabled() && plan.isPrepaid() == 'N') { if
			 * (plan.getBillRule() != 400 && plan.getBillRule() != 300) {
			 * this.reverseInvoice.reverseInvoiceServices(orderId, order.getClientId(),
			 * disconnectionDate); } }
			 */
			order.update(command, orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.DISCONNECTION.toString());
			this.orderRepository.saveAndFlush(order);

			final String requstStatus = UserActionStatusTypeEnum.DISCONNECTION.toString();
			Long processingResultId = Long.valueOf(0);

			// Update Client Status
			if ("None".equalsIgnoreCase(plan.getProvisionSystem())) {
				final Long activeOrders = this.orderReadPlatformService
						.retrieveClientActiveOrderDetails(order.getClientId(), null, null);
				if (activeOrders == 0) {
					Client client = this.clientRepository.findOne(order.getClientId());
					client.setStatus(ClientStatus.DEACTIVE.getValue());
					this.clientRepository.saveAndFlush(client);
				}
				processNotifyMessages(EventActionConstants.EVENT_DISCONNECTION_ORDER, order.getClientId(),
						order.getId().toString(), null);
			} else {

				/*
				 * CommandProcessingResult processingResult =
				 * this.provisioningWritePlatformService .postOrderDetailsForProvisioning(order,
				 * plan.getPlanCode(), UserActionStatusTypeEnum.DISCONNECTION.toString(),
				 * processingResultId, null, null, order.getId(), plan.getProvisionSystem(),
				 * null); processingResultId = processingResult.commandId();
				 */

				JsonObject provisioningObject = new JsonObject();
				provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.DISCONNECTION.toString());
				provisioningObject.addProperty("clientServiceId", order.getClientServiceId());
				JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
						fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
				List<Order> orders = new ArrayList<Order>();
				orders.add(order);
				this.provisioningWritePlatformService.createProvisioningRequest(orders, com, false);

			}

			// checking for Paypal Recurring DisConnection
			processPaypalRecurringActions(orderId, EventActionConstants.EVENT_PAYPAL_RECURRING_TERMINATE_ORDER);
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, order.getClientId(),
					order.getId().toString(), "DISCONNECTION");

			// For Order History
			final OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					DateTimeUtils.getLocalDateTimeOfTenant(), processingResultId, requstStatus, getUserId(), null);
			this.orderHistoryRepository.save(orderHistory);

			/** Code for Charging start */
			Configuration is_aggregater = configurationRepository.findOneByName(ConfigurationConstants.AGGREGATER);

			if (null != is_aggregater && is_aggregater.isEnabled()) {

				String baseBillingPlanId = configurationRepository.findOneByName(ConfigurationConstants.BILLINGPLANID)
						.getValue();

				Order orderDetails = orderRepository.findOrderByClientIdAndPlanId(order.getClientId(),
						Long.parseLong(baseBillingPlanId));

				if (orderDetails != null) {
					if (order.getPlanId() != Long.parseLong(baseBillingPlanId)) {

						OrderPrice orderprice = orderDetails.getPrice().get(0);

						BigDecimal disconnectedOrderPrice = order.getPrice().get(0).getPrice();

						orderprice.setPrice(orderprice.getPrice().subtract(disconnectedOrderPrice));
						orderPriceRepository.saveAndFlush(orderprice);
					}
				}
			}
			/** Code for Charging end */

			return new CommandProcessingResult(Long.valueOf(order.getId()), order.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	@Transactional
	@Override
	public CommandProcessingResult ordersSuspention(final JsonCommand command, List<Order> orders) {
		Long clientServiceId = Long.valueOf(0);
		List<Order> provOrders = new ArrayList<Order>();
		try {
			this.fromApiJsonDeserializer.validateForOrderSuspension(command.json());
			final LocalDateTime suspensionDate = command.localDateTimeValueOfParameterNamed("suspensionDate");
			/*
			 * final Configuration configurationProperty = this.configurationRepository
			 * .findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);
			 */
			for (Order order : orders) {
				Long orderStatus = null;
				boolean isaddtoProvOrders = false;
				List<OrderPrice> orderPrices = order.getPrice();
				for (OrderPrice price : orderPrices) {
					price.updateDates(suspensionDate);
				}

				final Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());

				if ("None".equalsIgnoreCase(plan.getProvisionSystem())) {
					orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.SUSPENDED).getId();
					this.processNotifyMessages(EventActionConstants.EVENT_SUSPENSION_ORDER, order.getClientId(),
							order.getId().toString(), null);
				} else {
					orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
					clientServiceId = order.getClientServiceId();
					isaddtoProvOrders = true;
					/* provOrders.add(order); */
				}
				/*
				 * if (configurationProperty.isEnabled() && plan.isPrepaid() == 'N'){ if
				 * (plan.getBillRule() != 400 && plan.getBillRule() != 300) {
				 * this.reverseInvoice.reverseInvoiceServices(order.getId(),
				 * order.getClientId(), suspensionDate); } }
				 */
				order.updateForSuspend(command, orderStatus);
				order.setuserAction(UserActionStatusTypeEnum.SUSPENTATION.toString());
				this.orderRepository.saveAndFlush(order);
				if (isaddtoProvOrders) {
					provOrders.add(order);
				}

				// checking for Paypal Recurring DisConnection
				processPaypalRecurringActions(order.getId(),
						EventActionConstants.EVENT_PAYPAL_RECURRING_TERMINATE_ORDER);
				processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, order.getClientId(),
						order.getId().toString(), "SUSPENTATION");

				// For Order History
				final OrderHistory orderHistory = new OrderHistory(order.getId(),
						DateTimeUtils.getLocalDateTimeOfTenant(), DateTimeUtils.getLocalDateTimeOfTenant(),
						Long.valueOf(0), UserActionStatusTypeEnum.SUSPENTATION.toString(), getUserId(), null);
				this.orderHistoryRepository.save(orderHistory);
			}
			// provisioning request generation
			if (!provOrders.isEmpty()) {
				JsonObject provisioningObject = new JsonObject();
				provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.SUSPENTATION.toString());
				provisioningObject.addProperty("clientServiceId", clientServiceId);
				JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
						fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
				this.provisioningWritePlatformService.createProvisioningRequest(provOrders, com, false);
			}

			return new CommandProcessingResultBuilder().withEntityId(clientServiceId).build();

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	@Override
	public CommandProcessingResult renewalClientOrder(JsonCommand command, Long orderId) {

		try {
			LocalDateTime newStartdate = null;
			String requstStatus = null;
			String requestStatusForProv = null;
			final Long userId = getUserId();
			this.fromApiJsonDeserializer.validateForRenewalOrder(command.json());
			this.crmServices.renewalPlan(command);
			Order orderDetails = retrieveOrderById(orderId);
			Contract contract = contractRepository.findOne(command.longValueOfParameterNamed("renewalPeriod"));
			List<ChargeCodeMaster> chargeCodeMaster = chargeCodeRepository
					.findOneByBillFrequency(orderDetails.getBillingFrequency());
			Integer chargeCodeDuration = chargeCodeMaster.get(0).getChargeDuration();
			if (contract == null) {
				throw new ContractNotNullException();
			}
			if (chargeCodeDuration > contract.getUnits().intValue()) {
				throw new ChargeCodeAndContractPeriodException(chargeCodeMaster.get(0).getBillFrequencyCode(),
						"Renewal");
			}

			this.eventValidationReadPlatformService.checkForCustomValidations(orderDetails.getClientId(),
					EventActionConstants.EVENT_ORDER_RENEWAL, command.json(), userId);
			List<OrderPrice> orderPrices = orderDetails.getPrice();
			final Long contractPeriod = command.longValueOfParameterNamed("renewalPeriod");
			final String description = command.stringValueOfParameterNamed("description");
			Contract contractDetails = this.subscriptionRepository.findOne(contractPeriod);
			Plan plan = this.findOneWithNotFoundDetection(orderDetails.getPlanId());
			// chargeCodeMaster =
			// chargeCodeRepository.findOneByBillFrequency(orderDetails.getBillingFrequency());
			// Integer chargeCodeDuration =
			// chargeCodeMaster.get(0).getChargeDuration();
			if (contractDetails == null) {
				throw new ContractNotNullException();
			}
			LocalDateTime contractEndDate = this.orderAssembler.calculateEndDate(
					DateTimeUtils.getLocalDateTimeOfTenant(), contractDetails.getSubscriptionType(),
					contractDetails.getUnits());
			LocalDateTime chargeCodeEndDate = this.orderAssembler.calculateEndDate(
					DateTimeUtils.getLocalDateTimeOfTenant(), chargeCodeMaster.get(0).getDurationType(),
					chargeCodeMaster.get(0).getChargeDuration().longValue());
			if (contractEndDate != null && chargeCodeEndDate != null) {
				if (contractEndDate.toDate().before(chargeCodeEndDate.toDate())) {
					if (plan.isPrepaid() == 'N' || plan.isPrepaid() == 'n') {
						throw new ChargeCodeAndContractPeriodException(chargeCodeMaster.get(0).getBillFrequencyCode(),
								contractDetails.getSubscriptionPeriod());
					} else {
						throw new ChargeCodeAndContractPeriodException(chargeCodeMaster.get(0).getBillFrequencyCode(),
								true);
					}
				}
			}

			if (orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())) {

				newStartdate = new LocalDateTime(orderDetails.getEndDate()).plusDays(1);
				// System.out.println("new StartDate" + newStartdate);
				requstStatus = UserActionStatusEnumaration
						.OrderStatusType(UserActionStatusTypeEnum.RENEWAL_BEFORE_AUTOEXIPIRY).getValue();
				requestStatusForProv = ProvisioningApiConstants.REQUEST_RENEWAL_BE;
			} else if (orderDetails.getStatus().equals(StatusTypeEnum.DISCONNECTED.getValue().longValue())) {

				newStartdate = DateTimeUtils.getLocalDateTimeOfTenant();
				requstStatus = UserActionStatusEnumaration
						.OrderStatusType(UserActionStatusTypeEnum.RENEWAL_AFTER_AUTOEXIPIRY).getValue();
				if (!plan.getProvisionSystem().equalsIgnoreCase("None")) {
					orderDetails.setStatus(StatusTypeEnum.PENDING.getValue().longValue());
				} else {
					orderDetails.setStatus(StatusTypeEnum.ACTIVE.getValue().longValue());
					Client client = this.clientRepository.findOne(orderDetails.getClientId());
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.clientRepository.saveAndFlush(client);
				}
				requestStatusForProv = ProvisioningApiConstants.REQUEST_RENEWAL_AE;// UserActionStatusTypeEnum.ACTIVATION.toString();
				orderDetails.setNextBillableDay(null);
				orderDetails.setRenewalDate(newStartdate.toDate());
			}
			// System.out.println("calling " + newStartdate);
			LocalDateTime renewalEndDate = this.orderAssembler.calculateEndDate(newStartdate,
					contractDetails.getSubscriptionType(), contractDetails.getUnits());
			// System.out.println("renewalEndDate::" + renewalEndDate);
			Configuration configuration = this.configurationRepository
					.findOneByName(ConfigurationConstants.CONFIG_ALIGN_BIILING_CYCLE);

			if (configuration != null && plan.isPrepaid() == 'N') {

				orderDetails.setBillingAlign(configuration.isEnabled() ? 'Y' : 'N');
				if (configuration.isEnabled() && renewalEndDate != null) {
					orderDetails.setEndDate(renewalEndDate.dayOfMonth().withMaximumValue());
				} else {
					orderDetails.setEndDate(renewalEndDate);
				}
			} else {
				orderDetails.setEndDate(renewalEndDate);
			}
			// orderDetails.setEndDate(renewalEndDate);
			orderDetails.setuserAction(requstStatus);

			for (OrderPrice orderprice : orderPrices) {
				if (plan.isPrepaid() == 'Y' && orderprice.isAddon() == 'N') {

					final Long priceId = command.longValueOfParameterNamed("priceId");
					// ServiceMaster service =
					// this.serviceMasterRepository.findOne(orderprice.getserviceId());
					Product product = this.productRepository.findOne(orderprice.getProductId());
					Long productId = Long.valueOf(0);
					if (product != null) {
						productId = product.getId();
					}
					Price price1 = this.priceRepository.findOne(priceId);

					Price price = this.priceRepository.findOneByPlanAndService(plan.getId(), productId,
							contractDetails.getSubscriptionPeriod(), price1.getChargeCode(), price1.getPriceRegion(),
							price1.getChargeOwner());

					if (price != null) {
						ChargeCodeMaster chargeCode = this.chargeCodeRepository
								.findOneByChargeCode(price.getChargeCode());

						orderprice.setChargeCode(chargeCode.getChargeCode());
						orderprice.setChargeDuration(chargeCode.getChargeDuration().toString());
						orderprice.setChargeType(chargeCode.getChargeType());
						orderprice.setChargeDurationType(chargeCode.getDurationType());
						orderprice.setPrice(price.getPrice());

					} else {
						throw new PriceNotFoundException(priceId);
					}
				}
				orderprice.setDatesOnOrderStatus(newStartdate, new LocalDate(orderDetails.getEndDate()),
						orderDetails.getUserAction());
				// setBillEndDate(renewalEndDate);
				// this.OrderPriceRepository.save(orderprice);
				orderDetails.setNextBillableDay(null);
			}

			orderDetails.setContractPeriod(contractDetails.getId());
			orderDetails.setuserAction(requstStatus);
			this.orderRepository.saveAndFlush(orderDetails);

			// Set<PlanDetails> planDetails=plan.getDetails();
			// ServiceMaster
			// serviceMaster=this.serviceMasterRepository.findOneByServiceCode(planDetails.iterator().next().getServiceCode());
			Long resourceId = Long.valueOf(0);

			if (!plan.getProvisionSystem().equalsIgnoreCase("None")) {
				/*
				 * // Prepare Provisioning Req CodeValue codeValue = this.codeValueRepository
				 * .findOneByCodeValue(plan.getProvisionSystem());
				 * 
				 * if (codeValue.position() == 1&& orderDetails.getStatus().equals
				 * (StatusTypeEnum.ACTIVE.getValue().longValue())) { requestStatusForProv =
				 * "RENEWAL_BE";
				 * 
				 * } if (requestStatusForProv != null) { CommandProcessingResult
				 * commandProcessingResult = this.provisioningWritePlatformService
				 * .postOrderDetailsForProvisioning(orderDetails, plan.getPlanCode(),
				 * requestStatusForProv, Long.valueOf(0), null, null, orderDetails.getId(),
				 * plan.getProvisionSystem(), null); resourceId =
				 * commandProcessingResult.resourceId(); }
				 */
				if (orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())) {

					JsonObject provisioningObject = new JsonObject();
					orderDetails.setStatus(StatusTypeEnum.PENDING.getValue().longValue());
					this.orderRepository.saveAndFlush(orderDetails);
					provisioningObject.addProperty("requestType", requestStatusForProv);
					provisioningObject.addProperty("clientServiceId", orderDetails.getClientServiceId());
					JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
							fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
					List<Order> orders = new ArrayList<Order>();
					orders.add(orderDetails);
					this.provisioningWritePlatformService.createProvisioningRequest(orders, com, false);

				}

			} else {
				processNotifyMessages(EventActionConstants.EVENT_RECONNECTION_ORDER, orderDetails.getClientId(),
						orderId.toString(), null);
			}

			// For Order History
			OrderHistory orderHistory = new OrderHistory(orderDetails.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					newStartdate, resourceId, requstStatus, userId, description);
			this.orderHistoryRepository.saveAndFlush(orderHistory);

			// Auto renewal with invoice process for Topup orders

			if (plan.isPrepaid() == 'Y'
					&& orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())) {

				List<BillItem> invoices = this.invoiceClient.singleOrderInvoice(orderDetails.getId(),
						orderDetails.getClientId(), newStartdate.plusDays(1));
				for (BillItem invoice : invoices) {
					if (invoice != null) {
						List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService
								.retrieveActionDetails(EventActionConstants.EVENT_TOPUP_INVOICE_MAIL);
						if (actionDetaislDatas.size() != 0) {
							this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,
									orderDetails.getClientId(), invoice.getId().toString(), null);
						}
					}
				}
			}
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, orderDetails.getClientId(),
					orderId.toString(), "RENEWAL");

			/*
			 * Configuration isPaywizard = configurationRepository
			 * .findOneByName(ConfigurationConstants.PAYWIZARD_INTEGRATION); if (null !=
			 * isPaywizard && isPaywizard.isEnabled()) { try {
			 * inviewWritePlatformService.topUpforPaywizard(command,
			 * orderDetails.getClientId()); } catch (Exception e) { throw new Exception(); }
			 * }
			 */
			return new CommandProcessingResult(Long.valueOf(orderDetails.getClientId()), orderDetails.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private Long getUserId() {

		Long userId = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if (context.getAuthentication() != null) {
			AppUser appUser = this.context.authenticatedUser();
			userId = appUser.getId();
		} else {
			userId = new Long(0);
		}

		return userId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #reconnectOrder(java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult reconnectOrder(final Long orderId) {

		try {
			Order order = this.orderRepository.findOne(orderId);
			if (order == null) {
				throw new NoOrdersFoundException(orderId);
			}
			final LocalDateTime startDate = DateTimeUtils.getLocalDateTimeOfTenant();
			List<SubscriptionData> subscriptionDatas = this.contractPeriodReadPlatformService
					.retrieveSubscriptionDatabyOrder(orderId);
			Contract contractPeriod = this.subscriptionRepository.findOne(subscriptionDatas.get(0).getId());
			LocalDateTime EndDate = this.orderAssembler.calculateEndDate(startDate,
					contractPeriod.getSubscriptionType(), contractPeriod.getUnits());
			order.setStartDate(startDate);
			order.setEndDate(EndDate);
			order.setNextBillableDay(null);
			final List<OrderPrice> orderPrices = order.getPrice();

			for (OrderPrice price : orderPrices) {
				if (price.isAddon() == 'N') {

					price.setBillStartDate(startDate);
					price.setBillEndDate(EndDate);
					price.setNextBillableDay(null);
					price.setInvoiceTillDate(null);
				}
			}

			Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());
			String requstStatus = UserActionStatusTypeEnum.RECONNECTION.toString().toString();
			Long processingResultId = Long.valueOf(0);

			if (plan.getProvisionSystem().equalsIgnoreCase("None")) {
				order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
				Client client = this.clientRepository.findOne(order.getClientId());
				client.setStatus(ClientStatus.ACTIVE.getValue());
				this.clientRepository.save(client);
				processNotifyMessages(EventActionConstants.EVENT_RECONNECTION_ORDER, order.getClientId(),
						order.getId().toString(), null);

			} else {

				order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
				CommandProcessingResult processingResult = this.provisioningWritePlatformService
						.postOrderDetailsForProvisioning(order, plan.getPlanCode(), requstStatus, Long.valueOf(0), null,
								null, order.getId(), plan.getProvisionSystem(), null);
				processingResultId = processingResult.commandId();

			}
			order.setuserAction(UserActionStatusTypeEnum.RECONNECTION.toString());
			this.orderRepository.save(order);

			// For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					DateTimeUtils.getLocalDateTimeOfTenant(), processingResultId, requstStatus, getUserId(), null);
			this.orderHistoryRepository.save(orderHistory);

			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, order.getClientId(),
					order.getId().toString(), "RECONNECTION");

			return new CommandProcessingResult(order.getId(), order.getClientId());

		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	/*
	 * @SuppressWarnings("unused")
	 * 
	 * @Override public CommandProcessingResult retrackOsdMessage(final JsonCommand
	 * command) { try { this.context.authenticatedUser();
	 * this.fromApiJsonDeserializer.validateForRetrack(command.json()); String
	 * requstStatus = null; String message = null; final String commandName =
	 * command.stringValueOfParameterNamed("commandName"); final Order order =
	 * this.orderRepository.findOne(command.entityId()); if (order == null) { throw
	 * new NoOrdersFoundException(command.entityId()); } if
	 * (commandName.equalsIgnoreCase("RETRACK")) { final String restrict =
	 * orderReadPlatformService.checkRetrackInterval(command.entityId()); if
	 * (restrict != null && restrict.equalsIgnoreCase("yes")) { requstStatus =
	 * UserActionStatusTypeEnum.RETRACK.toString(); } else { throw new
	 * PlatformDataIntegrityException("retrack.already.done",
	 * "retrack.already.done", "retrack.already.done"); } } else if
	 * (commandName.equalsIgnoreCase("OSM")) { requstStatus =
	 * UserActionStatusTypeEnum.MESSAGE.toString(); message =
	 * command.stringValueOfParameterNamed("message"); } final Plan plan =
	 * this.findOneWithNotFoundDetection(order.getPlanId()); Long resourceId =
	 * Long.valueOf(0); if (requstStatus != null && plan != null) {
	 * 
	 * CommandProcessingResult commandProcessingResult =
	 * this.provisioningWritePlatformService .postOrderDetailsForProvisioning(order,
	 * plan.getPlanCode(), requstStatus, Long.valueOf(0), null, null,
	 * order.getId(),plan.getProvisionSystem(), null); resourceId =
	 * commandProcessingResult.resourceId();
	 * 
	 * 
	 * final AllocationDetailsData detailsData = this.allocationReadPlatformService
	 * .getTheHardwareItemDetails(command.entityId()); final ProcessRequest
	 * processRequest=new ProcessRequest(Long.valueOf(
	 * 0),order.getClientId(),order.getId (),plan.getProvisionSystem(),requstStatus
	 * ,'N','N'); processRequest.setNotify(); final List<OrderLine> orderLineData =
	 * order.getServices(); for (OrderLine orderLine : orderLineData) { String
	 * hardWareId = null; if (detailsData != null) { hardWareId =
	 * detailsData.getSerialNo(); } final List<ServiceMapping>
	 * provisionServiceDetails = this.provisionServiceDetailsRepository
	 * .findOneByServiceId(orderLine.getServiceId()); final ServiceMaster service =
	 * this.serviceMasterRepository.findOne(orderLine .getServiceId()); if
	 * (!provisionServiceDetails.isEmpty()) { if (message == null) { message =
	 * provisionServiceDetails.get(0).getServiceIdentification(); } final
	 * ProcessRequestDetails processRequestDetails = new
	 * ProcessRequestDetails(orderLine.getId(), orderLine.getServiceId(),message,
	 * "Recieved", hardWareId,order.getStartDate(), order.getEndDate(), null,null,
	 * 'N',requstStatus,service.getServiceType());
	 * 
	 * processRequest.add(processRequestDetails); } }
	 * this.processRequestRepository.save(processRequest);
	 * 
	 * 
	 * this.orderRepository.save(order); final OrderHistory orderHistory = new
	 * OrderHistory(order.getId(),
	 * DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),
	 * resourceId, requstStatus, getUserId(), null);
	 * this.orderHistoryRepository.save(orderHistory);
	 * 
	 * } return new CommandProcessingResult(order.getId(), order.getClientId()); }
	 * catch (EmptyResultDataAccessException dve) { throw new
	 * PlatformDataIntegrityException("retrack.already.done",
	 * "retrack.already.done", "retrack.already.done"); } catch
	 * (DataIntegrityViolationException dve) { handleCodeDataIntegrityIssues(null,
	 * dve); return new CommandProcessingResult(Long.valueOf(-1)); } }
	 */

	/* ==================COMMAND CENTER START================== */

	/*
	 * @SuppressWarnings("unused")
	 * 
	 * @Override
	 * 
	 * public CommandProcessingResult retrackOsdMessage(final JsonCommand command) {
	 * Configuration isPaywizard =
	 * configurationRepository.findOneByName(ConfigurationConstants.
	 * PAYWIZARD_INTEGRATION); try { this.context.authenticatedUser(); int count =
	 * 0;
	 * 
	 * if (command.stringValueOfParameterName("type").equalsIgnoreCase("group") &&
	 * command.booleanPrimitiveValueOfParameterNamed("isGroupSupported")) {
	 * Map<String, Object> changes = new HashMap<>(); List<ClientServiceData>
	 * clientServiceDatas = this.clientServiceReadPlatformService
	 * .retriveActiveClientsInOrg(command.longValueOfParameterNamed("officeId"));
	 * for (ClientServiceData clientServiceData : clientServiceDatas) { JsonCommand
	 * com = this.provisioningJsonPreparation(command, clientServiceData);
	 * this.provisioningWritePlatformService.
	 * createProvisioningRequestForCommandCenter(com); count++; }
	 * 
	 * }
	 * 
	 * if (null != isPaywizard && isPaywizard.isEnabled()) { ItemData itemData =
	 * itemReadPlatformService
	 * .retriveSerialNum(Long.parseLong(command.stringValueOfParameterName(
	 * "clientId")));
	 * inviewWritePlatformService.retrackForPaywizardRestCall(itemData.getSerialNo()
	 * ); }
	 * 
	 * return new
	 * CommandProcessingResultBuilder().withResourceIdAsString(String.valueOf(count)
	 * ).build();
	 * 
	 * } catch (DataIntegrityViolationException dve) { return
	 * CommandProcessingResult.empty(); }
	 * 
	 * }
	 */

	@SuppressWarnings("unused")
	@Override
	public CommandProcessingResult retrackOsdMessage(final JsonCommand command) {

		Configuration isPaywizard = configurationRepository.findOneByName(ConfigurationConstants.PAYWIZARD_INTEGRATION);
		try {
			// System.out.println("OrderWritePlatformServiceImpl.retrackOsdMessage()");
			// this.context.authenticatedUser();
			int count = 0;

			if (command.stringValueOfParameterName("type").equalsIgnoreCase("group")
					&& command.booleanPrimitiveValueOfParameterNamed("isGroupSupported")) {
				Map<String, Object> changes = new HashMap<>();
				List<ClientServiceData> clientServiceDatas = this.clientServiceReadPlatformService
						.retriveActiveClientsInOrgOfficeId(command.longValueOfParameterNamed("officeId"));
				for (ClientServiceData clientServiceData : clientServiceDatas) {
					JsonCommand com = this.provisioningJsonPreparation(command, clientServiceData);
					this.provisioningWritePlatformService.createProvisioningRequestForCommandCenter(com);
					count++;
				}

			} else {

				Long clientServiceId = command.longValueOfParameterNamed("clientServiceId");

				ProvisioningRequest requestDetails = provisioningRequestRepository
						.findLatestRetrackRequest(clientServiceId);

				DateTime requestTime = new DateTime(requestDetails.getStartDate());

				DateTime dateTime = new DateTime();
				Period p = new Period(dateTime, requestTime);
				int hours = p.getHours();

				if (hours < 2) {
					throw new ProvisioningRequestNotFoundException(
							"retrack not able to process please try after sometime");
				}

				Map<String, Object> changes = new HashMap<>();
				List<ClientServiceData> clientServiceDatas = this.clientServiceReadPlatformService
						.retriveActiveClientsInOrg(command.longValueOfParameterNamed("clientServiceId"));
				for (ClientServiceData clientServiceData : clientServiceDatas) {
					JsonCommand com = this.provisioningJsonPreparation(command, clientServiceData);
					// System.out.println("OrderWritePlatformServiceImpl.retrackOsdMessage()");
					this.provisioningWritePlatformService.createProvisioningRequestForCommandCenter(com);
					count++;
				}

			}

			return new CommandProcessingResultBuilder().withResourceIdAsString(String.valueOf(count)).build();

		} catch (DataIntegrityViolationException dve) {
			return CommandProcessingResult.empty();
		}

	}

	JsonCommand provisioningJsonPreparation(final JsonCommand command, ClientServiceData clientServiceData) {
		try {
			JsonObject provisioningObject = new JsonObject();
			provisioningObject.addProperty("requestType", command.stringValueOfParameterNamed("requestType"));
			provisioningObject.addProperty("clientId", command.longValueOfParameterNamed("clientId"));
			provisioningObject.addProperty("clientServiceId", command.longValueOfParameterNamed("clientServiceId"));
			provisioningObject.addProperty("provisioningSystem",
					command.longValueOfParameterNamed("provisioningSystem"));
			if (command.stringValueOfParameterName("requestType").equalsIgnoreCase("RETRACK")
					|| command.stringValueOfParameterName("requestType").equalsIgnoreCase("HARDRETRACK")) {
				Long clientServiceId = Long.parseLong(command.stringValueOfParameterName("clientServiceId"));
				ProvisioningData provisioningData = this.provisioningReadPlatformService
						.retrieveClientAndServiceParam(clientServiceId);
				provisioningObject.addProperty("provisioningSystem", provisioningData.getProvisioningSystem());
				ClientData clientData = provisioningData.getClientData();
				JSONObject clientJsonObject = new JSONObject();
				clientJsonObject.put("accountNo", clientData.getAccountNo());
				clientJsonObject.put("officeId", clientData.getOfficeId());
				clientJsonObject.put("displayName", clientData.getDisplayName());
				clientJsonObject.put("email", clientData.getEmail());
				clientJsonObject.put("selfcarePassword", clientData.getSelfcarePassword());
				clientJsonObject.put("firstName", clientData.getFirstname());
				clientJsonObject.put("lastName", clientData.getLastname());
				clientJsonObject.put("mobile", clientData.getPhone());
				clientJsonObject.put("password", clientData.getClientPassword());
				org.json.JSONArray clientJSONArray = new org.json.JSONArray();
				clientJSONArray.put(clientJsonObject);
				provisioningObject.addProperty("clientInfo", String.valueOf(clientJSONArray));
			}
			JsonArray array = command.arrayOfParameterNamed("requestMessage");
			provisioningObject.add("requestMessage", array);
			provisioningObject.addProperty("type", command.stringValueOfParameterNamed("type"));
			if (clientServiceData != null) {
				provisioningObject.addProperty("clientId", clientServiceData.getClientId());
				provisioningObject.addProperty("clientServiceId", clientServiceData.getId());
			}
			JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
					fromApiJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
			return com;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/* ==================COMMAND CENTER END================== */

	@Override
	public CommandProcessingResult Osdmessage(final JsonCommand command, Long clientServicePoid) throws Exception {
		try {
			this.context.authenticatedUser();
			JsonObject provisioningObject = new JsonObject();
			OrderData orderData = this.orderReadPlatformService.retrieveClientServicePoid(clientServicePoid);
			if (orderData != null) {
				provisioningObject.addProperty("clientId", orderData.getClientId());
				provisioningObject.addProperty("clientServiceId", orderData.getClientServiceId());
				provisioningObject.addProperty("provisioningSystem", orderData.getProvisioningSystem());
			}

			// this.fromApiJsonDeserializer.validateForRetrack(command.json());
			provisioningObject.addProperty("requestType", command.stringValueOfParameterNamed("requestType"));
			JsonArray array = command.arrayOfParameterNamed("requestMessage");
			provisioningObject.add("requestMessage", array);
			provisioningObject.addProperty("type", "single");
			JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
					fromApiJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);

			return this.retrackOsdMessage(com);
		} catch (DataIntegrityViolationException dve) {
			return CommandProcessingResult.empty();
		}
	}

	@Override
	public CommandProcessingResult changePlan(JsonCommand command, Long entityId) {
		String[] substancesArray = null;
		try {
			// Long userId = this.context.authenticatedUser().getId();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			checkingContractPeriodAndBillfrequncyValidation(command.longValueOfParameterNamed("contractPeriod"),
					command.stringValueOfParameterNamed("paytermCode"));
			final Long oldplanId = command.longValueOfParameterNamed("oldplanId");
			final String Newplanprepaid = command.stringValueOfParameterNamed("Newplanprepaid");
			final String oldplanprepaid = command.stringValueOfParameterNamed("oldplanprepaid");
			if (!oldplanprepaid.equalsIgnoreCase(Newplanprepaid)) {
				List<ApiParameterError> dataValidationErrors = null;
				throw new PlatformApiDataValidationException("validation.msg.Please.Select.SamePlanType",
						"Please Select SamePlanType.", dataValidationErrors);
			}
			Order order = this.orderRepository.findOne(entityId);
			String orderNo = order.getOrderNo();
			String newJson = command.json();
			JSONObject jsonObject = new JSONObject(newJson);
			jsonObject.put("orderNo", orderNo);
			CommandProcessingResult resultCrm = this.crmServices.changePlan(jsonObject.toString());
			if (resultCrm != null) {
				Set<String> subtances = resultCrm.getSubstances();
				substancesArray = subtances.toArray(new String[subtances.size()]);
			}
			order.updateDisconnectionstate();
			Date billEndDate = order.getPrice().get(0).getBillEndDate();

			LocalDateTime invoiceTillDateTime = new LocalDateTime(order.getPrice().get(0).getInvoiceTillDate());
			LocalDateTime invoicetillDate = invoiceTillDateTime;
			this.orderRepository.save(order);

			Configuration property = this.configurationRepository
					.findOneByName(ConfigurationConstants.CONFIG_CHANGE_PLAN_ALIGN_DATES);
			if (!property.isEnabled()) {
				Configuration dcConfiguration = this.configurationRepository
						.findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);
				if (dcConfiguration.isEnabled()) {
					this.reverseInvoice.reverseInvoiceServices(order.getId(), order.getClientId(),
							DateTimeUtils.getLocalDateTimeOfTenant());
				}
			}

			CommandProcessingResult result = this.createOrder(order.getClientId(), command, order);
			Order newOrder = this.orderRepository.findOne(result.resourceId());
			if (resultCrm == null) {
				newOrder.updateOrderNum(order.getOrderNo(),
						this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.NUMBER_GENERATOR));
			} else {
				newOrder.setOrderNo(this.retreiveOrderNo(substancesArray[0]));
			}
			newOrder.updateActivationDate(order.getActiveDate());
			List<OrderAddons> addons = this.orderAddonsRepository.findAddonsByOrderId(order.getId());
			if (resultCrm != null) {
				List<OrderLine> orderLines = newOrder.getServices();
				for (OrderLine orderLine : orderLines) {
					orderLine.setPurchaseProductPoId(
							Long.parseLong(this.retreivePurchaseProductPoid(substancesArray[0])));
				}
			}
			for (OrderAddons orderAddons : addons) {

				orderAddons.setOrderId(newOrder.getId());
				OrderPrice orderPrice = this.orderPriceRepository.findOne(orderAddons.getPriceId());
				orderPrice.update(newOrder);
				this.orderRepository.save(newOrder);
				this.orderPriceRepository.saveAndFlush(orderPrice);
				this.orderAddonsRepository.saveAndFlush(orderAddons);
			}

			if (property.isEnabled()) {

				List<OrderPrice> orderPrices = newOrder.getPrice();
				for (OrderPrice orderPrice : orderPrices) {
					if (billEndDate == null) {
						// orderPrice.setBillEndDate(null);

					} else {
						// orderPrice.setBillEndDate(new
						// LocalDate(billEndDate));
					}
					orderPrice.setInvoiceTillDate(invoicetillDate);
					orderPrice.setNextBillableDay(order.getPrice().get(0).getNextBillableDay());
				}
			}

			newOrder.setuserAction(UserActionStatusTypeEnum.CHANGE_PLAN.toString());
			this.orderRepository.save(newOrder);

			Plan plan = this.findOneWithNotFoundDetection(newOrder.getPlanId());
			/*
			 * HardwareAssociation association =
			 * this.associationRepository.findOneByOrderAndClient(order.getId(),
			 * order.getClientId());
			 * 
			 * if (association != null) { association.delete();
			 * this.associationRepository.save(association); }
			 */
			Long processResuiltId = new Long(0);

			if (!plan.getProvisionSystem().equalsIgnoreCase("None")) {
				CommandProcessingResult processingResult = this.provisioningWritePlatformService
						.postOrderDetailsForProvisioning(newOrder, plan.getCode(),
								UserActionStatusTypeEnum.CHANGE_PLAN.toString(), new Long(0), null, null,
								newOrder.getId(), plan.getProvisionSystem(), null);
				processResuiltId = processingResult.commandId();
			} else {
				// Notify details for change plan
				processNotifyMessages(EventActionConstants.EVENT_CHANGE_PLAN, newOrder.getClientId(),
						newOrder.getId().toString(), null);
			}

			// For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					DateTimeUtils.getLocalDateTimeOfTenant(), processResuiltId,
					UserActionStatusTypeEnum.CHANGE_PLAN.toString(), null, null);

			this.orderHistoryRepository.save(orderHistory);
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, newOrder.getClientId(),
					newOrder.getId().toString(), "CHANGE PLAN");
			return new CommandProcessingResult(result.resourceId(), order.getClientId());

		} catch (DataIntegrityViolationException exception) {
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(new Long(-1));
		} catch (JSONException e) {
			e.printStackTrace();
			return new CommandProcessingResult(new Long(-1));
		}

	}

	@Override
	public CommandProcessingResult applyPromo(JsonCommand command) {
		try {
			this.context.authenticatedUser().getUsername();
			this.fromApiJsonDeserializer.validateForPromo(command.json());
			final Long promoId = command.longValueOfParameterNamed("promoId");
			final LocalDateTime startDate = command.localDateTimeValueOfParameterNamed("startDate");
			PromotionCodeMaster promotion = this.promotionCodeRepository.findOne(promoId);

			if (promotion == null) {
				throw new PromotionCodeNotFoundException(promoId.toString());
			}
			Order order = this.orderRepository.findOne(command.entityId());
			List<OrderDiscount> orderDiscounts = order.getOrderDiscount();
			LocalDateTime enddate = this.orderAssembler.calculateEndDate(startDate, promotion.getDurationType(),
					promotion.getDuration());

			for (OrderDiscount orderDiscount : orderDiscounts) {
				orderDiscount.updateDates(promotion.getDiscountRate(), promotion.getDiscountType(), enddate, startDate);
				// this.orderDiscountRepository.save(orderDiscount);
			}
			this.orderRepository.save(order);
			return new CommandProcessingResult(command.entityId(), order.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return null;
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult scheduleOrderCreation(Long clientId, JsonCommand command) {

		try {
			String actionType = command.stringValueOfParameterNamed("actionType");
			if (!actionType.equalsIgnoreCase("renewalorder")) {
				this.fromApiJsonDeserializer.validateForCreate(command.json());
			}
			LocalDate startDate = command.localDateValueOfParameterNamed("start_date");

			char status = 'N';
			if (command.hasParameter("status")) {
				status = command.stringValueOfParameterNamed("status").trim().charAt(0);
			}

			EventAction eventAction = null;
			JSONObject jsonObject = new JSONObject();
			Long userId = getUserId();

			if (actionType.equalsIgnoreCase("renewalorder")) {

				// Check for Custome_Validation
				this.eventValidationReadPlatformService.checkForCustomValidations(clientId,
						EventActionConstants.EVENT_ORDER_RENEWAL, command.json(), userId);

				jsonObject.put("renewalPeriod", command.longValueOfParameterNamed("renewalPeriod"));
				jsonObject.put("description", command.stringValueOfParameterNamed("description"));

				eventAction = new EventAction(DateUtils.getLocalDateOfTenant().toDate(), "RENEWAL", "ORDER",
						EventActionConstants.ACTION_RENEWAL, "/orders/renewalorder/" + clientId, clientId,
						command.json(), null, clientId);

			} else if (actionType.equalsIgnoreCase("changeorder")) {
				// Check for Custome_Validation
				this.eventValidationReadPlatformService.checkForCustomValidations(clientId,
						EventActionConstants.EVENT_CHANGE_ORDER, command.json(), userId);
				Long orderId = command.longValueOfParameterNamed("orderId");
				jsonObject.put("billAlign", command.booleanPrimitiveValueOfParameterNamed("billAlign"));
				jsonObject.put("contractPeriod", command.longValueOfParameterNamed("contractPeriod"));
				jsonObject.put("dateFormat", command.booleanPrimitiveValueOfParameterNamed("dateFormat"));
				jsonObject.put("locale", command.booleanPrimitiveValueOfParameterNamed("locale"));
				jsonObject.put("isNewPlan", command.booleanPrimitiveValueOfParameterNamed("isNewPlan"));
				jsonObject.put("paytermCode", command.stringValueOfParameterNamed("paytermCode"));
				jsonObject.put("planCode", command.longValueOfParameterNamed("planCode"));
				jsonObject.put("start_date", command.stringValueOfParameterNamed("start_date"));
				jsonObject.put("disconnectionDate", command.stringValueOfParameterNamed("disconnectionDate"));
				jsonObject.put("disconnectReason", command.stringValueOfParameterNamed("disconnectReason"));

				eventAction = new EventAction(startDate.toDate(), "CHANGEPLAN", "ORDER",
						EventActionConstants.ACTION_CHNAGE_PLAN, "/orders/changPlan/" + orderId, clientId,
						command.json(), orderId, clientId);

			} else {

				// Check for Custome_Validation
				this.eventValidationReadPlatformService.checkForCustomValidations(clientId,
						EventActionConstants.EVENT_CREATE_ORDER, command.json(), userId);

				// Check for Active Orders
				/*
				 * Long activeorderId=this.orderReadPlatformService.
				 * retrieveClientActiveOrderDetails(clientId,null); /* Long activeorderId
				 * =this.orderReadPlatformService.retrieveClientActiveOrderDetails
				 * (clientId,null); >>>>>>> upstream/obsplatform-3.0 >>>>>>> obsplatform-3.0
				 * if(activeorderId !=null && activeorderId !=0){ Order
				 * order=this.orderRepository.findOne(activeorderId); if(order.getEndDate() ==
				 * null || !startDate.isAfter(new LocalDate(order.getEndDate()))){ throw new
				 * SchedulerOrderFoundException(activeorderId); } }
				 */

				jsonObject.put("billAlign", command.booleanPrimitiveValueOfParameterNamed("billAlign"));
				jsonObject.put("contractPeriod", command.longValueOfParameterNamed("contractPeriod"));
				jsonObject.put("dateFormat", "dd MMMM yyyy");
				jsonObject.put("locale", "en");
				jsonObject.put("isNewPlan", "true");
				jsonObject.put("paytermCode", command.stringValueOfParameterNamed("paytermCode"));
				jsonObject.put("planCode", command.longValueOfParameterNamed("planCode"));
				jsonObject.put("start_date", startDate.toDate());
				jsonObject.put("serialnumber", "");

				eventAction = new EventAction(startDate.toDate(), "CREATE", "ORDER", EventActionConstants.ACTION_NEW,
						"/orders/" + clientId, clientId, command.json(), null, clientId);

			}

			eventAction.updateStatus(status);
			this.eventActionRepository.save(eventAction);
			return new CommandProcessingResult(command.entityId(), clientId);

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, null);
			return new CommandProcessingResult(Long.valueOf(-1));
		} catch (JSONException dve) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	@Override
	public CommandProcessingResult deleteSchedulingOrder(Long entityId, JsonCommand command) {

		try {
			this.context.authenticatedUser();
			EventAction eventAction = this.eventActionRepository.findOne(entityId);
			if (eventAction.IsProcessed() == 'Y') {
				throw new PrepareRequestActivationException();
			} else {
				eventAction.updateStatus('C');
				this.eventActionRepository.saveAndFlush(eventAction);
			}
			return new CommandProcessingResult(Long.valueOf(entityId), eventAction.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	public CommandProcessingResult scheduleOrderUpdation(Long entityId, JsonCommand command) {

		try {

			String actionType = command.stringValueOfParameterNamed("actionType");
			String serialnum = command.stringValueOfParameterNamed("serialnumber");
			String allcation_type = command.stringValueOfParameterNamed("allcation_type");
			if (!actionType.equalsIgnoreCase("renewalorder")) {
				if (serialnum.isEmpty()) {
					this.fromApiJsonDeserializer.validateForUpdate(command.json());
				}
			}
			String startDate = command.stringValueOfParameterNamed("start_date");

			char status = 'N';
			if (command.hasParameter("status")) {
				status = command.stringValueOfParameterNamed("status").trim().charAt(0);
			}

			EventAction eventAction = this.eventActionRepository.findOne(entityId);

			JSONObject jsonObject = new JSONObject(eventAction.getCommandAsJson());
			Long clientId = eventAction.getClientId();
			this.eventValidationReadPlatformService.checkForCustomValidations(entityId,
					EventActionConstants.EVENT_CREATE_ORDER, command.json(), clientId);
			if (!serialnum.isEmpty()) {
				jsonObject.remove("serialnumber");
				jsonObject.put("serialnumber", serialnum);
				jsonObject.put("allocation_type", allcation_type);
			}
			if (!startDate.isEmpty()) {
				jsonObject.remove("start_date");
				jsonObject.put("start_date", startDate);
				Date startDate1 = command.DateValueOfParameterNamed("start_date");
				eventAction.setTransDate(startDate1);

			}
			eventAction.setCommandAsJson(jsonObject.toString());
			eventAction.updateStatus(status);
			this.eventActionRepository.save(eventAction);
			return new CommandProcessingResult(command.entityId(), entityId);

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, null);
			return new CommandProcessingResult(Long.valueOf(-1));
		} catch (JSONException dve) {

			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	@Transactional
	@Override
	public CommandProcessingResult orderExtension(JsonCommand command, Long entityId) {

		try {

			Long userId = this.context.authenticatedUser().getId();
			Order order = this.orderRepository.findOne(entityId);
			String extensionperiod = command.stringValueOfParameterNamed("extensionPeriod");
			String extensionReason = command.stringValueOfParameterNamed("extensionReason");
			LocalDateTime newStartdate = new LocalDateTime(order.getEndDate());
			newStartdate = newStartdate.plusDays(1);
			String[] periodData = extensionperiod.split(" ");
			LocalDateTime endDate = this.orderAssembler.calculateEndDate(newStartdate, periodData[1],
					new Long(periodData[0]));
			List<OrderPrice> orderPrices = order.getPrice();
			Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());
			if (order.getStatus().intValue() == StatusTypeEnum.ACTIVE.getValue()) {
				order.setEndDate(endDate);
				for (OrderPrice orderprice : orderPrices) {
					orderprice.setBillEndDate(endDate);
					LocalDateTime endDateTimeDate = new LocalDateTime(endDate.toDate());
					orderprice.setInvoiceTillDate(endDateTimeDate);
					orderprice.setNextBillableDay(endDate.toDate());
					this.orderPriceRepository.save(orderprice);
				}
			} else if (order.getStatus().intValue() == StatusTypeEnum.DISCONNECTED.getValue()) {
				for (OrderPrice orderprice : orderPrices) {
					orderprice.setBillStartDate(newStartdate);
					orderprice.setBillEndDate(endDate);
					orderprice.setNextBillableDay(null);
					orderprice.setInvoiceTillDate(null);
					this.orderPriceRepository.save(orderprice);
				}
				if (plan.getProvisionSystem().equalsIgnoreCase("None")) {
					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					Client client = this.clientRepository.findOne(order.getClientId());
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.clientRepository.save(client);
				} /*
					 * else { // Check For HardwareAssociation AssociationData associationData =
					 * this.hardwareAssociationReadplatformService. retrieveSingleDetails(entityId);
					 * if (associationData == null) { throw new
					 * HardwareDetailsNotFoundException(entityId.toString()); } order
					 * .setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum
					 * .PENDING).getId()); }
					 */
			}
			order.setEndDate(endDate);
			order.setuserAction(UserActionStatusTypeEnum.RECONNECTION.toString());
			this.orderRepository.save(order);

			// for Prepare Request
			String requstStatus = UserActionStatusTypeEnum.RECONNECTION.toString().toString();
			this.prepareRequestWriteplatformService.prepareNewRequest(order, plan, requstStatus);

			// For Order History
			SecurityContext context = SecurityContextHolder.getContext();
			if (context.getAuthentication() != null) {
				AppUser appUser = this.context.authenticatedUser();
				userId = appUser.getId();
			} else {
				userId = new Long(0);
			}

			// For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					DateTimeUtils.getLocalDateTimeOfTenant(), entityId, UserActionStatusTypeEnum.EXTENSION.toString(),
					userId, extensionReason);
			this.orderHistoryRepository.save(orderHistory);
			return new CommandProcessingResult(entityId, order.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(new Long(-1));

		}
	}

	@Override
	public CommandProcessingResult orderTermination(JsonCommand command, Long orderId) {

		try {
			AppUser appUser = this.context.authenticatedUser();
			Order order = this.orderRepository.findOne(orderId);
			Long resourceId = Long.valueOf(0);

			if (order == null) {
				throw new OrderNotFoundException(orderId);
			}

			Long orderStatus = null;
			Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());

			if (plan.getProvisionSystem().equalsIgnoreCase("None")) {
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.TERMINATED).getId();

			} else {
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
				/*
				 * CommandProcessingResult processingResult =
				 * this.provisioningWritePlatformService .postOrderDetailsForProvisioning(order,
				 * plan.getCode(), UserActionStatusTypeEnum.TERMINATION.toString(),resourceId,
				 * null, null, order.getId(), plan.getProvisionSystem(), null); resourceId =
				 * processingResult.resourceId();
				 */
				JsonObject provisioningObject = new JsonObject();
				provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.TERMINATION.toString());
				provisioningObject.addProperty("clientServiceId", order.getClientServiceId());
				JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
						fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
				List<Order> orders = new ArrayList<Order>();
				orders.add(order);
				this.provisioningWritePlatformService.createProvisioningRequest(orders, com, false);
			}
			order.setStatus(orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.TERMINATION.toString());
			this.orderRepository.saveAndFlush(order);

			OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					DateTimeUtils.getLocalDateTimeOfTenant(), resourceId,
					UserActionStatusTypeEnum.TERMINATION.toString(), appUser.getId(), null);

			// checking for Paypal Recurring DisConnection
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_ORDER_TERMINATE, order.getClientId(),
					order.getId().toString(), null);
			processPaypalRecurringActions(orderId, EventActionConstants.EVENT_PAYPAL_RECURRING_TERMINATE_ORDER);

			this.orderHistoryRepository.save(orderHistory);
			return new CommandProcessingResult(orderId, order.getClientId());

		} catch (DataIntegrityViolationException exception) {
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(new Long(-1));
		}
	}

	@Override
	public CommandProcessingResult ordersTermination(JsonCommand command, List<Order> orders) {
		Long clientServiceId = null;
		try {
			AppUser appUser = this.context.authenticatedUser();
			for (Order order : orders) {
				Long resourceId = Long.valueOf(0);
				Long orderStatus = null;
				clientServiceId = order.getClientServiceId();
				Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());

				if (plan.getProvisionSystem().equalsIgnoreCase("None")) {
					orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.TERMINATED).getId();

				} else {
					orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
				}
				order.setStatus(orderStatus);
				order.setuserAction(UserActionStatusTypeEnum.TERMINATION.toString());
				this.orderRepository.saveAndFlush(order);

				OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
						DateTimeUtils.getLocalDateTimeOfTenant(), resourceId,
						UserActionStatusTypeEnum.TERMINATION.toString(), appUser.getId(), null);

				// checking for Paypal Recurring DisConnection
				processNotifyMessages(EventActionConstants.EVENT_NOTIFY_ORDER_TERMINATE, order.getClientId(),
						order.getId().toString(), null);
				processPaypalRecurringActions(order.getId(),
						EventActionConstants.EVENT_PAYPAL_RECURRING_TERMINATE_ORDER);

				this.orderHistoryRepository.save(orderHistory);
			}
			if (!orders.isEmpty()) {
				JsonObject provisioningObject = new JsonObject();
				provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.TERMINATION.toString());
				provisioningObject.addProperty("clientServiceId", clientServiceId);
				JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
						fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
				this.provisioningWritePlatformService.createProvisioningRequest(orders, com, false);
			}
			return new CommandProcessingResultBuilder().withEntityId(clientServiceId).build();
		} catch (DataIntegrityViolationException exception) {
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(new Long(-1));
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult orderSuspention(final JsonCommand command, final Long entityId) {

		try {
			final AppUser appUser = this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForOrderSuspension(command.json());
			final Order order = this.orderRepository.findOne(entityId);
			Long resourceId = Long.valueOf(0);
			if (order == null) {
				throw new OrderNotFoundException(entityId);
			}

			final EnumDomainService enumDomainService = this.enumDomainServiceRepository
					.findOneByEnumMessageProperty(StatusTypeEnum.SUSPENDED.toString());
			order.setStatus(enumDomainService.getEnumId());

			final Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());
			if (!plan.getProvisionSystem().equalsIgnoreCase("None")) {
				final Long pendingId = this.enumDomainServiceRepository
						.findOneByEnumMessageProperty(StatusTypeEnum.PENDING.toString()).getEnumId();
				order.setStatus(pendingId);

				/*
				 * CommandProcessingResult commandProcessingResult =
				 * this.provisioningWritePlatformService .postOrderDetailsForProvisioning(order,
				 * plan.getCode(), UserActionStatusTypeEnum.SUSPENTATION.toString(), resourceId,
				 * null, null,order.getId(), plan.getProvisionSystem(), null); resourceId =
				 * commandProcessingResult.resourceId();
				 */
				JsonObject provisioningObject = new JsonObject();
				provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.SUSPENTATION.toString());
				provisioningObject.addProperty("clientServiceId", order.getClientServiceId());
				JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
						fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
				List<Order> orders = new ArrayList<Order>();
				orders.add(order);
				this.provisioningWritePlatformService.createProvisioningRequest(orders, com, false);

				// }

			}
			order.setuserAction(UserActionStatusTypeEnum.SUSPENTATION.toString());

			// Post Details in Payment followup
			final PaymentFollowup paymentFollowup = PaymentFollowup.fromJson(command, order.getClientId(),
					order.getId(), StatusTypeEnum.ACTIVE.toString(), StatusTypeEnum.SUSPENDED.toString());
			this.paymentFollowupRepository.save(paymentFollowup);
			this.orderRepository.save(order);

			// checking for Paypal Recurring DisConnection
			processPaypalRecurringActions(entityId, EventActionConstants.EVENT_PAYPAL_RECURRING_DISCONNECT_ORDER);

			final OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					DateTimeUtils.getLocalDateTimeOfTenant(), resourceId,
					UserActionStatusTypeEnum.TERMINATION.toString(), appUser.getId(), null);
			this.orderHistoryRepository.save(orderHistory);
			return new CommandProcessingResult(entityId, order.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	/*
	 * @Transactional
	 * 
	 * @Override public CommandProcessingResult ordersSuspention(final JsonCommand
	 * command, List<Order> orders) { Long clientServiceId = Long.valueOf(0);
	 * List<Order> provOrders = new ArrayList<Order>(); try { final AppUser appUser
	 * = this.context.authenticatedUser();
	 * this.fromApiJsonDeserializer.validateForOrderSuspension(command.json());
	 * 
	 * for(Order order:orders){ Long resourceId = Long.valueOf(0); final
	 * EnumDomainService enumDomainService =
	 * this.enumDomainServiceRepository.findOneByEnumMessageProperty
	 * (StatusTypeEnum.SUSPENDED.toString());
	 * order.setStatus(enumDomainService.getEnumId()); final Plan plan =
	 * this.findOneWithNotFoundDetection(order.getPlanId()); if
	 * (!plan.getProvisionSystem().equalsIgnoreCase("None")) { final Long pendingId
	 * = this.enumDomainServiceRepository.findOneByEnumMessageProperty(
	 * StatusTypeEnum.PENDING.toString()).getEnumId(); order.setStatus(pendingId);
	 * clientServiceId = order.getClientServiceId(); provOrders.add(order); }
	 * order.setuserAction(UserActionStatusTypeEnum.SUSPENTATION.toString());
	 * 
	 * // Post Details in Payment followup final PaymentFollowup paymentFollowup =
	 * PaymentFollowup.fromJson(command, order.getClientId(), order.getId(),
	 * StatusTypeEnum.ACTIVE.toString(), StatusTypeEnum.SUSPENDED.toString());
	 * this.paymentFollowupRepository.save(paymentFollowup);
	 * this.orderRepository.save(order);
	 * 
	 * // checking for Paypal Recurring DisConnection
	 * processPaypalRecurringActions(order.getId(),
	 * EventActionConstants.EVENT_PAYPAL_RECURRING_DISCONNECT_ORDER);
	 * 
	 * final OrderHistory orderHistory = new OrderHistory(order.getId(),
	 * DateUtils.getLocalDateOfTenant(), DateUtils.getLocalDateOfTenant(),
	 * resourceId, UserActionStatusTypeEnum.SUSPENTATION.toString(),
	 * appUser.getId(), null); this.orderHistoryRepository.save(orderHistory); }
	 * 
	 * //provisioning request generation if(!provOrders.isEmpty()){ JsonObject
	 * provisioningObject = new JsonObject();
	 * provisioningObject.addProperty("requestType",
	 * UserActionStatusTypeEnum.SUSPENTATION.toString());
	 * provisioningObject.addProperty("clientServiceId", clientServiceId);
	 * JsonCommand com = new JsonCommand(null,
	 * provisioningObject.toString(),provisioningObject, fromJsonHelper, null, null,
	 * null, null, null, null, null, null, null, null, null,null);
	 * this.provisioningWritePlatformService .createProvisioningRequest(provOrders,
	 * com,false); }
	 * 
	 * return new
	 * CommandProcessingResultBuilder().withEntityId(clientServiceId).build(); }
	 * catch (DataIntegrityViolationException dve) {
	 * handleCodeDataIntegrityIssues(command, dve); return new
	 * CommandProcessingResult(Long.valueOf(-1)); }
	 * 
	 * }
	 */

	@Override
	public CommandProcessingResult reactiveOrder(final JsonCommand command, final Long entityId) {

		try {
			final AppUser appUser = this.context.authenticatedUser();
			Order order = this.orderRepository.findOne(entityId);
			Long resourceId = Long.valueOf(0);

			if (order == null) {
				throw new OrderNotFoundException(entityId);
			}

			final Long pendingId = this.enumDomainServiceRepository
					.findOneByEnumMessageProperty(StatusTypeEnum.PENDING.toString()).getEnumId();
			final Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());

			if (!"None".equalsIgnoreCase(plan.getProvisionSystem())) {
				order.setStatus(pendingId);
				/*
				 * CommandProcessingResult commandProcessingResult =
				 * this.provisioningWritePlatformService .postOrderDetailsForProvisioning(order,
				 * plan.getCode(), UserActionStatusTypeEnum.REACTIVATION.toString(), resourceId,
				 * null, null, order.getId(), plan.getProvisionSystem(), null); resourceId =
				 * commandProcessingResult.resourceId();
				 */
				JsonObject provisioningObject = new JsonObject();
				provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.REACTIVATION.toString());
				provisioningObject.addProperty("clientServiceId", order.getClientServiceId());
				JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
						fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
				List<Order> orders = new ArrayList<Order>();
				orders.add(order);
				this.provisioningWritePlatformService.createProvisioningRequest(orders, com, false);

			} else {
				EnumDomainService enumDomainService = this.enumDomainServiceRepository
						.findOneByEnumMessageProperty(StatusTypeEnum.ACTIVE.toString());
				order.setStatus(enumDomainService.getEnumId());
			}

			order.setuserAction(UserActionStatusTypeEnum.REACTIVATION.toString());
			PaymentFollowup paymentFollowup = this.paymentFollowupRepository.findOneByorderId(order.getId());

			if (paymentFollowup != null) {
				paymentFollowup.setReactiveDate(DateUtils.getDateOfTenant());
				this.paymentFollowupRepository.save(paymentFollowup);
			}

			this.orderRepository.save(order);

			// checking for Paypal Recurring Reconnection
			processPaypalRecurringActions(entityId, EventActionConstants.EVENT_PAYPAL_RECURRING_RECONNECTION_ORDER);

			final OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
					DateTimeUtils.getLocalDateTimeOfTenant(), resourceId,
					UserActionStatusTypeEnum.REACTIVATION.toString(), appUser.getId(), null);
			this.orderHistoryRepository.save(orderHistory);

			return new CommandProcessingResult(entityId, order.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult reactiveOrders(final JsonCommand command, List<Order> orders) {
		Long clientServiceId = Long.valueOf(0);
		List<Order> provOrders = new ArrayList<Order>();
		try {
			final LocalDateTime startDate = DateTimeUtils.getLocalDateTimeOfTenant();
			for (Order order : orders) {
				List<SubscriptionData> subscriptionDatas = this.contractPeriodReadPlatformService
						.retrieveSubscriptionDatabyOrder(order.getId());
				Contract contractPeriod = this.subscriptionRepository.findOne(subscriptionDatas.get(0).getId());
				LocalDateTime EndDate = this.orderAssembler.calculateEndDate(startDate,
						contractPeriod.getSubscriptionType(), contractPeriod.getUnits());
				order.setStartDate(startDate);
				order.setEndDate(EndDate);
				order.setNextBillableDay(null);
				final List<OrderPrice> orderPrices = order.getPrice();

				for (OrderPrice price : orderPrices) {
					if (price.isAddon() == 'N') {

						price.setBillStartDate(startDate);
						price.setBillEndDate(EndDate);
						price.setNextBillableDay(null);
						price.setInvoiceTillDate(null);
					}
				}

				Plan plan = this.findOneWithNotFoundDetection(order.getPlanId());
				String requstStatus = UserActionStatusTypeEnum.REACTIVATION.toString().toString();
				Long processingResultId = Long.valueOf(0);

				if (plan.getProvisionSystem().equalsIgnoreCase("None")) {
					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					/*
					 * Client client = this.clientRepository.findOne(order.getClientId());
					 * client.setStatus(ClientStatus.ACTIVE.getValue());
					 * this.clientRepository.save(client);
					 */
					processNotifyMessages(EventActionConstants.EVENT_REACTIVE_ORDER, order.getClientId(),
							order.getId().toString(), null);

				} else {
					clientServiceId = order.getClientServiceId();
					provOrders.add(order);
				}
				order.setuserAction(UserActionStatusTypeEnum.REACTIVATION.toString());
				this.orderRepository.save(order);

				// For Order History
				OrderHistory orderHistory = new OrderHistory(order.getId(), DateTimeUtils.getLocalDateTimeOfTenant(),
						DateTimeUtils.getLocalDateTimeOfTenant(), processingResultId, requstStatus, getUserId(), null);
				this.orderHistoryRepository.save(orderHistory);

				processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, order.getClientId(),
						order.getId().toString(), "RECONNECTION");
			}
			// provisioning request generation
			if (!provOrders.isEmpty()) {
				JsonObject provisioningObject = new JsonObject();
				provisioningObject.addProperty("requestType", UserActionStatusTypeEnum.REACTIVATION.toString());
				provisioningObject.addProperty("clientServiceId", clientServiceId);
				JsonCommand com = new JsonCommand(null, provisioningObject.toString(), provisioningObject,
						fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
				this.provisioningWritePlatformService.createProvisioningRequest(provOrders, com, false);
			}

			return new CommandProcessingResultBuilder().withEntityId(clientServiceId).build();

		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

		/*
		 * Long clientServiceId = null; try { final AppUser appUser =
		 * this.context.authenticatedUser(); List<Order> provOrders = new
		 * ArrayList<Order>(); for(Order order:orders){ Long resourceId =
		 * Long.valueOf(0);
		 * 
		 * 
		 * final Long pendingId =
		 * this.enumDomainServiceRepository.findOneByEnumMessageProperty
		 * (StatusTypeEnum.PENDING.toString()).getEnumId(); final Plan plan =
		 * this.findOneWithNotFoundDetection(order.getPlanId());
		 * 
		 * if (!"None".equalsIgnoreCase(plan.getProvisionSystem())) {
		 * order.setStatus(pendingId); clientServiceId = order.getClientServiceId();
		 * provOrders.add(order); } else { EnumDomainService enumDomainService =
		 * this.enumDomainServiceRepository
		 * .findOneByEnumMessageProperty(StatusTypeEnum.ACTIVE.toString());
		 * order.setStatus(enumDomainService.getEnumId());
		 * 
		 * }
		 * 
		 * order.setuserAction(UserActionStatusTypeEnum.REACTIVATION.toString());
		 * PaymentFollowup paymentFollowup =
		 * this.paymentFollowupRepository.findOneByorderId(order.getId());
		 * 
		 * if (paymentFollowup != null) {
		 * paymentFollowup.setReactiveDate(DateUtils.getDateOfTenant());
		 * this.paymentFollowupRepository.save(paymentFollowup); }
		 * 
		 * this.orderRepository.save(order);
		 * 
		 * // checking for Paypal Recurring Reconnection
		 * processPaypalRecurringActions(order.getId(),
		 * EventActionConstants.EVENT_PAYPAL_RECURRING_RECONNECTION_ORDER);
		 * 
		 * final OrderHistory orderHistory = new OrderHistory(order.getId(),
		 * DateUtils.getLocalDateOfTenant(), DateUtils.getLocalDateOfTenant(),
		 * resourceId, UserActionStatusTypeEnum.REACTIVATION.toString(),
		 * appUser.getId(), null); this.orderHistoryRepository.save(orderHistory); }
		 * if(!provOrders.isEmpty()){ JsonObject provisioningObject = new JsonObject();
		 * provisioningObject.addProperty("requestType",
		 * UserActionStatusTypeEnum.REACTIVATION.toString());
		 * provisioningObject.addProperty("clientServiceId", clientServiceId);
		 * JsonCommand com = new JsonCommand(null,
		 * provisioningObject.toString(),provisioningObject, fromJsonHelper, null, null,
		 * null, null, null, null, null, null, null, null, null,null);
		 * this.provisioningWritePlatformService.createProvisioningRequest (provOrders,
		 * com,false); } return new CommandProcessingResultBuilder()
		 * .withEntityId(clientServiceId).build();
		 * 
		 * } catch (DataIntegrityViolationException dve) {
		 * handleCodeDataIntegrityIssues(command, dve); return new
		 * CommandProcessingResult(Long.valueOf(-1)); }
		 */
	}

	private void processPaypalRecurringActions(Long orderId, String eventActionName) {

		// checking for Paypal Recurring DisConnection
		PaypalRecurringBilling billing = this.paypalRecurringBillingRepository.findOneByOrderId(orderId);

		if (null != billing && null != billing.getSubscriberId()) {

			List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService
					.retrieveActionDetails(eventActionName);

			if (actionDetaislDatas.size() != 0) {
				this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, billing.getClientId(),
						orderId.toString(), null);
			}
		}
	}

	/*
	 * private void checkingContractPeriodAndBillfrequncyValidation(Long
	 * contractPeriod, String paytermCode){
	 * 
	 * 
	 * Contract contract = contractRepository.findOne(contractPeriod);
	 * List<ChargeCodeMaster> chargeCodeMaster =
	 * chargeCodeRepository.findOneByBillFrequency(paytermCode); Integer
	 * chargeCodeDuration = chargeCodeMaster.get(0).getChargeDuration(); if(contract
	 * == null){ throw new ContractNotNullException(); } if(chargeCodeDuration >
	 * contract.getUnits().intValue()){ throw new
	 * ChargeCodeAndContractPeriodException(); }
	 * 
	 * }
	 */
	@Override
	public void checkingContractPeriodAndBillfrequncyValidation(Long contractPeriod, String paytermCode) {

		Contract contract = contractRepository.findOne(contractPeriod);
		List<ChargeCodeMaster> chargeCodeMaster = chargeCodeRepository.findOneByBillFrequency(paytermCode);
		// Integer chargeCodeDuration =
		// chargeCodeMaster.get(0).getChargeDuration();
		if (contract == null) {
			throw new ContractNotNullException();
		}
		LocalDateTime contractEndDate = this.orderAssembler.calculateEndDate(DateTimeUtils.getLocalDateTimeOfTenant(),
				contract.getSubscriptionType(), contract.getUnits());
		LocalDateTime chargeCodeEndDate = this.orderAssembler.calculateEndDate(DateTimeUtils.getLocalDateTimeOfTenant(),
				chargeCodeMaster.get(0).getDurationType(), chargeCodeMaster.get(0).getChargeDuration().longValue());
		if (contractEndDate != null && chargeCodeEndDate != null) {
			if (contractEndDate.toDate().before(chargeCodeEndDate.toDate())) {
				throw new ChargeCodeAndContractPeriodException();
			}
		}
	}

	@Override
	public Plan findOneWithNotFoundDetection(final Long planId) {
		Plan plan = this.planRepository.findOne(planId);

		if (plan == null) {
			throw new PlanNotFundException(planId);
		}
		return plan;
	}

	@Override
	public CommandProcessingResult renewalOrderWithClient(JsonCommand command, Long clientId) throws Exception {

		try {

			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForOrderRenewalWithClient(command.json());
			Long planId = command.longValueOfParameterNamed("planId");
			String contractPeriod = command.stringValueOfParameterNamed("duration");
			Contract contract = this.contractRepository.findOneByContractId(contractPeriod);
			if (contract == null) {
				throw new ContractPeriodNotFoundException(contractPeriod, clientId);
			}
			List<Long> orderIds = this.orderReadPlatformService.retrieveOrderActiveAndDisconnectionIds(clientId,
					planId);
			if (orderIds.isEmpty()) {
				throw new NoOrdersFoundException(clientId, planId);

			}
			Plan planData = this.planRepository.findOne(planId);
			if (planData == null) {
				throw new PlanNotFundException(planId);
			}

			String isPrepaid = planData.getIsPrepaid() == 'N' ? "postpaid" : "prepaid";

			List<SubscriptionData> subscriptionDatas = this.planReadPlatformService
					.retrieveSubscriptionData(orderIds.get(0), isPrepaid);
			if (subscriptionDatas.isEmpty()) {
				throw new PriceNotFoundException(orderIds.get(0), clientId);
			}
			Long priceId = Long.valueOf(0);

			if (planData.getIsPrepaid() == 'Y') {
				for (SubscriptionData subscriptionData : subscriptionDatas) {
					if (subscriptionData.getContractdata().equalsIgnoreCase(contractPeriod)) {
						priceId = subscriptionData.getPriceId();
						break;
					}
				}
			}

			if (planData.getIsPrepaid() == 'Y' && priceId.equals(Long.valueOf(0))) {
				throw new ContractPeriodNotFoundException(contractPeriod, orderIds.get(0), clientId);
			}

			JSONObject renewalJson = new JSONObject();
			renewalJson.put("renewalPeriod", contract.getId());
			renewalJson.put("priceId", priceId);
			renewalJson.put("description", "Order renewal with clientId=" + clientId + " and planId=" + planId);
			final JsonElement element = fromJsonHelper.parse(renewalJson.toString());
			JsonCommand renewalCommand = new JsonCommand(null, renewalJson.toString(), element, fromJsonHelper, null,
					null, null, null, null, null, null, null, null, null, null, null);

			return this.renewalClientOrder(renewalCommand, orderIds.get(0));
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		} catch (JSONException e) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	@Override
	public CommandProcessingResult createMultipleOrder(Long clientId, JsonCommand command, Order oldOrder) {

		try {
			CommandProcessingResult commandProcessingResult = this.crmServices.addPlans(command);
			String[] substancesArray = null;
			if (commandProcessingResult != null) {
				Set<String> subtances = commandProcessingResult.getSubstances();
				substancesArray = subtances.toArray(new String[subtances.size()]);
			}
			int i = 0;
			final JsonArray multiplePlans = command.arrayOfParameterNamed("plans").getAsJsonArray();
			JsonCommand newCommand = null;
			List<CommandProcessingResult> commandProcessingResultList = new ArrayList<CommandProcessingResult>();
			CommandProcessingResult result = null;
			for (JsonElement planElement : multiplePlans) {
				JsonObject planObject = planElement.getAsJsonObject();
				if (substancesArray != null) {
					planObject.addProperty("purchaseProductPoid", this.retreivePurchaseProductPoid(substancesArray[i]));
					planObject.addProperty("orderNo", this.retreiveOrderNo(substancesArray[i]));
				}
				planElement = planObject;
				newCommand = new JsonCommand(null, planElement.toString(), planElement, this.fromJsonHelper, null, null,
						null, null, null, null, null, null, null, null, null, null);

				// check client balance
				Price price = priceRepository
						.findoneByPlanID(Long.parseLong(newCommand.stringValueOfParameterName("id")));

				this.slabRateReadPlatformService.prepaidService(clientId, price.getPrice());

				this.createOrder(clientId, newCommand, oldOrder);
				i++;
			}

			/* return new CommandProcessingResult((long) 0); */
			return new CommandProcessingResultBuilder().withClientId(clientId).build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void updateNextBillableDate(Order order, Long billingPackageId) {

		if (!order.getPlanId().equals(billingPackageId)) {
			LocalDateTime nxtBDate = new LocalDateTime(order.getStartDate());
			nxtBDate = nxtBDate.plusYears(30);
			order.setNextBillableDay(nxtBDate.toDate());
			orderRepository.saveAndFlush(order);

			for (OrderPrice orderPrice : order.getPrice()) {
				orderPrice.setNextBillableDay(nxtBDate.toDate());
				orderPrice.setInvoiceTillDate(nxtBDate);
				orderPriceRepository.saveAndFlush(orderPrice);
			}
		}
	}

	static Double existingSumOfOrders = null;

	private void checkAndIncludeBaseBillingPackage(Long billingPkgId, Long clientId, Double sumOfOrders) {
		try {
			Long clientServiceId = clientServiceRepository.findwithClientId1(clientId).get(0).getId();

			Order order = orderRepository.findOrderByClientIdAndPlanId(clientId, billingPkgId);

			OrderPrice orderprice = orderPriceRepository.findOrders(order);

			if (order != null && orderprice != null) {

				if (order != null && orderprice != null) {

					if (sumOfOrders == 0) {
						sumOfOrders = existingSumOfOrders;
						existingSumOfOrders = 0.0;
					}

					orderprice.setPrice(orderprice.getPrice().add(new BigDecimal(sumOfOrders)));
					orderPriceRepository.saveAndFlush(orderprice);
				}
			} else {

				existingSumOfOrders = sumOfOrders;

				JSONArray plans = new JSONArray();
				JSONObject planObject = new JSONObject();

				String dateFormat = "dd MMMM yyyy";
				SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
				String strDate = formatter.format(new Date());

				planObject.put("id", billingPkgId);
				planObject.put("planCode", billingPkgId);

				planObject.put("planDescription", "Billing Package");
				planObject.put("planPoId", 0);
				planObject.put("dealPoId", 0);
				planObject.put("paytermCode", "Daily");
				planObject.put("contractPeriod", 1);
				planObject.put("clientServiceId", clientServiceId.toString());
				planObject.put("billAlign", true);
				planObject.put("locale", "en");
				planObject.put("clientId", clientId.toString());
				planObject.put("dateFormat", dateFormat);
				planObject.put("start_date", strDate);
				planObject.put("isNewplan", true);

				plans.put(planObject);

				JSONObject plansObject = new JSONObject();
				plansObject.put("plans", plans);

				final JsonElement orderCreateElement = fromApiJsonHelper.parse(plansObject.toString());

				JsonCommand multipleOrderJson = new JsonCommand(null, orderCreateElement.toString(), orderCreateElement,
						fromApiJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);

				this.createMultipleOrder(clientId, multipleOrderJson, null);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String retreiveOrderNo(String substancesArray) {
		String[] split = substancesArray.split("_");
		return split[0];
	}

	private String retreivePurchaseProductPoid(String substancesArray) {
		String[] split = substancesArray.split("_");
		return split[3];
	}

	@Override
	public CommandProcessingResult disconnectMultipleOrder(Long clientId, JsonCommand command, Order oldOrder) {
		try {
			this.crmServices.cancelPlans(command);
			JsonCommand newCommand = null;
			String orderId = null;
			final JsonArray multiplePlans = command.arrayOfParameterNamed("plans").getAsJsonArray();
			for (JsonElement planElement : multiplePlans) {
				orderId = this.fromApiJsonHelper.extractStringNamed("id", planElement);
				newCommand = new JsonCommand(null, planElement.toString(), planElement, this.fromJsonHelper, null, null,
						null, null, null, null, null, null, null, null, null, null);
				this.disconnectOrder(newCommand, Long.parseLong(orderId));
			}
			/* return new CommandProcessingResult((long) 0); */
			return new CommandProcessingResultBuilder().withClientId(clientId).build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	@SuppressWarnings("unused")
	@Override
	@Transactional
	public CommandProcessingResult topUp(JsonCommand command, Long orderId) {

		Configuration isPaywizard = configurationRepository.findOneByName(ConfigurationConstants.PAYWIZARD_INTEGRATION);

		try {
			String stbNo = command.stringValueOfParameterName("stbNo");
			String paymentType = null;
			String voucherId = null;
			String txId = null;
			Long clientId = null;
			String jsonDataString = command.json();
			JSONObject jsonObject = new JSONObject(jsonDataString);
			JSONObject paymentData = jsonObject.getJSONObject("paymentDetails");
			paymentType = paymentData.getString("paymentType");
			final Order order = retrieveOrderById(orderId);

			ItemDetails itemData = itemDetailsRepository.getItemDetailBySerialNum(stbNo);
			if (itemData == null) {
				throw new SerialNumberNotFoundException(stbNo);
			}

			clientId = order.getClientId();
			VoucherData voucherData = null;
			JSONObject redemptionCommand = new JSONObject();

			if (paymentType.equals("voucherPayment")) {

				voucherId = paymentData.getString("voucherId");
				redemptionCommand.put("clientId", clientId);
				redemptionCommand.put("pinNumber", voucherId);

				voucherData = voucherReadPlatformService.retriveVoucherPinDetails(voucherId, itemData.getOfficeId());
				if (voucherData == null) {
					throw new PinNumberNotFoundException(voucherId);
				}
				if (null != isPaywizard && isPaywizard.isEnabled()) {
					topUpPaywizard(voucherId, stbNo);
				}
				// voucherData.getPinType().equalsIgnoreCase(PRODUCT_PINTYPE) &&
				if (voucherData.getPinValue() != null) {
					String redemptionResult = redemptionApiResource.createRedemption(redemptionCommand.toString());
					return new CommandProcessingResult(redemptionResult);
				} else {
					throw new VoucherIsNotProductTypeException();
				}

			} else if (paymentType.equals("OnlinePayment")) {
				txId = paymentData.getString("transactionNo");
				String amount = command.stringValueOfParameterName("amount");

			}

			Price price = priceRepository.findoneByPlanID(order.getPlanId());

			JSONObject renwalCommand = new JSONObject();
			renwalCommand.put("renewalPeriod", order.getContarctPeriod());
			renwalCommand.put("priceId", price.getId());

			final JsonElement renwalCommandElement = fromApiJsonHelper.parse(renwalCommand.toString());

			JsonCommand renwalCommandJson = new JsonCommand(null, renwalCommandElement.toString(), renwalCommandElement,
					fromApiJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);

			return this.renewalClientOrder(renwalCommandJson, orderId);

		} catch (JSONException e) {
			return new CommandProcessingResult("Json Exception");
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(dve);
		}
	}

	@Override
	@Transactional
	public CommandProcessingResult tovdtopUp(JsonCommand command) {
		Configuration isPaywizard = configurationRepository.findOneByName(ConfigurationConstants.PAYWIZARD_INTEGRATION);

		try {
			Long itemId = command.longValueOfParameterNamed("itemId");
			String stbNo = command.stringValueOfParameterName("stbNo");
			String paymentType = null;
			String voucherId = null;
			String txId = null;
			Long clientId = null;

			String jsonDataString = command.json();
			JSONObject jsonObject = new JSONObject(jsonDataString);
			JSONObject paymentData = jsonObject.getJSONObject("paymentDetails");
			paymentType = paymentData.getString("paymentType");

			ItemDetails itemData = itemDetailsRepository.getItemDetailBySerialNum(stbNo);
			if (itemData == null) {
				throw new SerialNumberNotFoundException(stbNo);
			}

			ClientServiceData clientServiceData = clientReadPlatformService.retriveServiceId(stbNo);
			clientId = clientServiceData.getClientId();
			MediaAsset media = mediaAssetRepository.findOne(itemId);

			if (media == null) {
				throw new NoMoviesFoundException();
			}

			EventPriceData eventPriceData = this.eventPriceReadPlatformService.retriveMoviePriceDetails(itemId);

			if (paymentType.equals("voucherPayment")) {
				voucherId = paymentData.getString("voucherId");
				VoucherData voucherData = voucherReadPlatformService.retriveVoucherPinDetailsWithPriceValue(voucherId,
						itemData.getOfficeId(), eventPriceData.getPrice());

				if (voucherData == null) {
					throw new PinNumberNotFoundException(voucherId);
				}

				if (null != isPaywizard && isPaywizard.isEnabled()) {
					for (MediaassetLocation mediaLocation : media.getMediaassetLocations()) {
						TvodPaywizardCall(voucherId, mediaLocation.getLocation(), stbNo);
					}
				}
				JSONObject redemptionCommand = new JSONObject();
				redemptionCommand.put("clientId", clientId);
				redemptionCommand.put("pinNumber", voucherId);
				String redemptionResult = null;
				if (voucherData.getPinType().equalsIgnoreCase(VALUE_PINTYPE)) {
					redemptionResult = redemptionApiResource.createRedemption(redemptionCommand.toString());
				} else {
					throw new PinNumberNotFoundException(voucherId);
				}
				Object obj = JSONValue.parse(redemptionResult);
				org.json.simple.JSONObject redemptionJson = (org.json.simple.JSONObject) obj;

				String result = (String) redemptionJson.get("Result");

				if (result.equals("FAILURE")) {
					return new CommandProcessingResult(result);
				}
			}

			else if (paymentType.equals("OnlinePayment")) {
				txId = paymentData.getString("transactionNo");

			}

			JSONObject eventJson = new JSONObject();
			eventJson.put("eventId", eventPriceData.getEventId());
			eventJson.put("formatType", "SD");
			eventJson.put("optType", "RENT");
			eventJson.put("locale", "en");
			eventJson.put("clientId", clientId);
			eventJson.put("dateFormat", "dd MMMM yyyy");
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
			String eventBookedDate = formatter.format(new Date());
			eventJson.put("eventBookedDate", eventBookedDate);

			String eventOrderResult = eventOrderApiResource.createNewEventOrder(clientId, eventJson.toString());

			return CommandProcessingResult.parsingResult(eventOrderResult);
		} catch (JSONException e) {
			return new CommandProcessingResult(e);
		}

		catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(dve);
		}

	}

	@Override
	@Transactional
	public CommandProcessingResult orderUssd(JsonCommand command, Long StbNO, Long refernceID) {
		JSONObject transactionResult = new JSONObject();

		String amount = command.stringValueOfParameterName("amount");

		try {
			String serialNo = Long.toString(StbNO);
			if (serialNo.length() == 15) {
				String HWno = serialNo.substring(0, 12);
				String movieCode = serialNo.substring(13, 15);
				ClientData hwdata = clientReadPlatformService.retrieveSearchClientId("serial_no", HWno);
				if (hwdata == null) {
					throw new SerialNumberNotFoundException(HWno);
				}
				OrderData orderData = null;
				orderData = orderReadPlatformService.getRenewalOrdersByClient(hwdata.getId(), 120l);
				if (orderData.getStatus() == "true") {
					MediaAsset moviedata = mediaAssetRepository.findOneByTitle(movieCode);
					JSONObject tvodCommand = new JSONObject();
					tvodCommand.put("itemId", moviedata.getId());
					tvodCommand.put("amount", amount);
					tvodCommand.put("stbNo", serialNo);

					JSONObject paymentDetails = new JSONObject();
					paymentDetails.put("transactionNo", refernceID);
					paymentDetails.put("paymentType", "OnlinePayment");

					tvodCommand.put("paymentDetails", paymentDetails);

					final CommandWrapper topupAndRenew = new CommandWrapperBuilder().TOVDtopUp()
							.withJson(tvodCommand.toString()).build();
					this.portfolioCommandSourceWritePlatformService.logCommandSource(topupAndRenew);

					Order updateOrder = orderRepository.findOne(orderData.getId());
					// updateOrder.setBillingAlign();

					transactionResult.put("status", "Successful");
					transactionResult.put("message", "Transaction Successful");
				}

			} else {
				ClientData hwdata = null;
				try {
					hwdata = clientReadPlatformService.retrieveSearchClientId("serial_no", serialNo);
				} catch (Exception e) {
					throw new SerialNumberNotFoundException(Long.toString(StbNO));
				}
				OrderData orderData = orderReadPlatformService.getRenewalOrdersByClient(hwdata.getId(), 210l);

				JSONObject topUpCommand = new JSONObject();
				topUpCommand.put("orderNo", orderData.getOrderNo());
				topUpCommand.put("amount", command.stringValueOfParameterName("amount"));
				topUpCommand.put("stbNo", serialNo);

				JSONObject paymentDetails = new JSONObject();
				paymentDetails.put("transactionNo", refernceID);

				paymentDetails.put("paymentType", "OnlinePayment");
				topUpCommand.put("paymentDetails", paymentDetails);

				final CommandWrapper topupAndRenew = new CommandWrapperBuilder().topUp(orderData.getId())
						.withJson(topUpCommand.toString()).build();
				this.portfolioCommandSourceWritePlatformService.logCommandSource(topupAndRenew);

				Order updateOrder = orderRepository.findOne(orderData.getId());

				// updateOrder.setOrderNo(refernceID.toString());
				transactionResult.put("status", "Successful");
				transactionResult.put("message", "Transaction Successful");

			}
		}

		catch (Exception e) {
			try {
				transactionResult.put("status", "Unsuccessful");
				transactionResult.put("message", "Transaction Unsuccessful");
				transactionResult.put("error", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return new CommandProcessingResult(transactionResult);
	}

	public String topUpPaywizard(String voucherCode, String stbNo) {
		try {
			String redeemVoucherURl = "http://45.63.98.216:9091/api/v1/paywizard/topup";
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			JSONObject json = new JSONObject();
			json.put("voucherCode", voucherCode);
			json.put("username", stbNo);
			HttpEntity<String> request = new HttpEntity<String>(json.toString(), headers);
			String result = restTemplate.postForObject(redeemVoucherURl, request, String.class);
			return result.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			throw new EntityExistsException("something went wrong");
		}
	}

	public String revpayStatusCompleted(String txid) {

		try {
			String revpayStatusUpdateURl = "http://45.63.98.216:9091/api/v1/revpay/orderCompleted";
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			JSONObject json = new JSONObject();
			json.put("txid", "txid");
			HttpEntity<String> request = new HttpEntity<String>(json.toString(), headers);
			String result = restTemplate.postForObject(revpayStatusUpdateURl, request, String.class);
			return result.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			throw new EntityExistsException("some thing went wrong");
		}
	}

	public String TvodPaywizardCall(String vocher, String VodCode, String stb) {
		try {
			String revpayStatusUpdateURl = "http://45.63.98.216:9091/api/v1/paywizard/purchaseTVOD";
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			JSONObject json = new JSONObject();
			json.put("username", stb);
			json.put("voucherCode", vocher);
			json.put("itemCode", VodCode);
			json.put("deviceID", stb);
			HttpEntity<String> request = new HttpEntity<String>(json.toString(), headers);
			String result = restTemplate.postForObject(revpayStatusUpdateURl, request, String.class);
			return result.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			throw new EntityExistsException("some thing went wrong");
		}
	}

}
