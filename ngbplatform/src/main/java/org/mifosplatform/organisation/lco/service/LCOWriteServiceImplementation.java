package org.mifosplatform.organisation.lco.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.lco.serialization.LCOCommandFromApiJsonDesrializer;
import org.mifosplatform.portfolio.jvtransaction.domain.JVTransaction;
import org.mifosplatform.portfolio.jvtransaction.domain.JVTransactionRepository;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderPrice;
import org.mifosplatform.portfolio.order.domain.OrderPriceRepository;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class LCOWriteServiceImplementation implements LCOWritePlatformService{

	private final PlatformSecurityContext context;
	private final LCOCommandFromApiJsonDesrializer apiJsonDeserializer;
	private final JVTransactionRepository jVTransactionRepository;
	private final FromJsonHelper fromApiJsonHelper;
	private final OrderRepository orderRepository;
	private final OrderPriceRepository orderPriceRepository;
	
	@Autowired
	public LCOWriteServiceImplementation(PlatformSecurityContext context,
			LCOCommandFromApiJsonDesrializer apiJsonDesrializer,
			JVTransactionRepository jvTransactionRepository,
			FromJsonHelper fromApiJsonHelper,
			 OrderRepository orderRepository,
			 OrderPriceRepository orderPriceRepository) {
		
		this.context = context;
		this.apiJsonDeserializer=apiJsonDesrializer;
		this.jVTransactionRepository=jvTransactionRepository;
		this.fromApiJsonHelper=fromApiJsonHelper;
		this.orderRepository=orderRepository;
		this.orderPriceRepository=orderPriceRepository;
		
		}
	@Override
	public CommandProcessingResult renewal(final JsonCommand command) {
		List<JVTransaction> jvTransactions = null;

		try{
			this.context.authenticatedUser();
			apiJsonDeserializer.validateForRenewal(command.json());
			
			final JsonArray lcoclientArray = command.arrayOfParameterNamed("lco").getAsJsonArray();
			jvTransactions= this.assembleDetails(lcoclientArray,command);
			this.jVTransactionRepository.save(jvTransactions);
			   return new CommandProcessingResult(Long.valueOf(0));
		}catch (Exception e) {
		        System.out.println(e);
		        return  CommandProcessingResult.empty();
		}
	}
	
	
private List<JVTransaction> assembleDetails(JsonArray lcoClientArray, final JsonCommand command) {
		List<JVTransaction> jvTransactions = new ArrayList<JVTransaction>();
		JVTransaction jVTransaction = null;
		String dateFormat = command.stringValueOfParameterName("dateFormat");
		String[]  lcoClients = null;
		lcoClients = new String[lcoClientArray.size()];
		if(lcoClientArray.size() > 0){
			for(int i = 0; i < lcoClientArray.size(); i++){
				lcoClients[i] = lcoClientArray.get(i).toString();
			}
	
		for (final String lcoClient : lcoClients) {
			final JsonElement element = this.fromApiJsonHelper.parse(lcoClient);
			final Long orderId= fromApiJsonHelper.extractLongNamed("orderId", element);
			Order order = this.orderRepository.findOne(orderId);
			final LocalDate startDate1 = fromApiJsonHelper.extractLocalDateNamed("startDate", element,dateFormat,Locale.getDefault());
			Date startDate=null;
				try {
					startDate= new SimpleDateFormat("yyyy-MM-dd").parse(startDate1.toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startDate=org.apache.commons.lang3.time.DateUtils.addMonths(startDate, 1);
			
			order.setNextBillableDay(startDate);
			this.orderRepository.save(order);
			OrderPrice orderPrice=this.orderPriceRepository.findOrders(order);
				orderPrice.setNextBillableDay(startDate);
				this.orderPriceRepository.save(orderPrice);
			
			
			final BigDecimal transAmount = new BigDecimal(fromApiJsonHelper.extractStringNamed("balanceAmount", element));
			final Long clientId=fromApiJsonHelper.extractLongNamed("id", element);
			final LocalDate endDate = fromApiJsonHelper.extractLocalDateNamed("endDate", element,dateFormat,Locale.getDefault());
			final LocalDate jvDate= new LocalDate();
			jVTransaction = new JVTransaction(clientId, orderId,jvDate,startDate1,endDate,transAmount);
			jvTransactions.add(jVTransaction);
		}	 
	}	
	
	return jvTransactions;
}
	
}
