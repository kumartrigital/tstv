package org.mifosplatform.portfolio.order.data;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class OrderHistoryData {
	
	private final Long id;
	private final LocalDateTime transactionDate;
	private final LocalDateTime actualDate;
	private final LocalDateTime provisioningDate;
	private final String transactioType;
	private final Long PrepareRequsetId;
	private final String userName;

	public OrderHistoryData(Long id, LocalDateTime transDate, LocalDateTime actualDate,LocalDateTime provisionongDate, 
			String transactionType, Long prepareRequsetId, String userName) {
               this.id=id;
               this.transactionDate=transDate;
               this.actualDate=actualDate;
               this.provisioningDate=provisionongDate;
               this.transactioType=transactionType;
               this.PrepareRequsetId=prepareRequsetId;
               this.userName=userName;
	
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the transactionDate
	 */
	public LocalDateTime getTransactionDate() {
		return transactionDate;
	}

	/**
	 * @return the actualDate
	 */
	public LocalDateTime getActualDate() {
		return actualDate;
	}

	/**
	 * @return the provisioningDate
	 */
	public LocalDateTime getProvisioningDate() {
		return provisioningDate;
	}

	/**
	 * @return the transactioType
	 */
	public String getTransactioType() {
		return transactioType;
	}

}
