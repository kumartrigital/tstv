package org.mifosplatform.portfolio.order.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDateTime;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.portfolio.order.data.OrderStatusEnumaration;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_orders")
public class OrdersCharge extends AbstractPersistable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "plan_id")
	private Long planId;

	@Column(name = "order_status")
	private Long status;

	@Column(name = "transaction_type")
	private String transactionType;

	@Column(name = "billing_frequency")
	private String billingFrequency;

	@Column(name = "next_billable_day")
	@Temporal(TemporalType.TIMESTAMP)
	private Date nextBillableDay;

	@Column(name = "end_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "active_date")
	private Date activeDate;

	@Column(name = "contract_period")
	private Long contarctPeriod;

	@Column(name = "client_service_id")
	private Long clientServiceId;

	@Column(name = "is_deleted")
	private char isDeleted;

	@Column(name = "billing_align")
	private char billingAlign;

	@Column(name = "disconnect_reason")
	private String disconnectReason;

	@Column(name = "user_action")
	private String userAction;

	@Column(name = "order_no")
	private String orderNo;

	@Column(name = "auto_renew")
	private char autoRenew;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "orders", orphanRemoval = true)
	private List<OrderPricecharge> price = new ArrayList<OrderPricecharge>();

	public OrdersCharge() {

	}

	public List<OrderPricecharge> getPrice() {
		return price;
	}

	public void setPrice(List<OrderPricecharge> price) {
		this.price = price;
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getPlanId() {
		return planId;
	}

	public Long getStatus() {
		return status;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public String getBillingFrequency() {
		return billingFrequency;
	}

	public Date getNextBillableDay() {
		return nextBillableDay;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setBillingAlign(char billingAlign) {
		this.billingAlign = billingAlign;
	}

	public Long getContarctPeriod() {
		return contarctPeriod;
	}

	public void delete() {
		this.isDeleted = 'y';
		this.endDate = DateUtils.getLocalDateOfTenant().toDate();
	}

	public void update(JsonCommand command, Long orderStatus) {

		if (this.status != 3) {
			this.endDate = command.localDateValueOfParameterNamed("disconnectionDate").toDate();
			this.disconnectReason = command.stringValueOfParameterNamed("disconnectReason");
			this.status = orderStatus;
		}

	}

	public void updateForSuspend(JsonCommand command, Long orderStatus) {

		if (this.status != 3) {
			this.endDate = command.localDateValueOfParameterNamed("suspensionDate").toDate();
			this.disconnectReason = command.stringValueOfParameterNamed("suspensionReason");
			this.status = orderStatus;
		}

	}

	public char getbillAlign() {
		return billingAlign;
	}

	public void setNextBillableDay(Date dateTime) {
		this.nextBillableDay = dateTime;

	}

	public void setEndDate(LocalDateTime localDateTime) {
		this.endDate = null;
		if (localDateTime != null) {
			this.endDate = localDateTime.toDate();
		}
	}

	public void setEndDate(Date endDate) {
		this.endDate = null;
		if (endDate != null) {
			this.endDate = endDate;
		}
	}

	public Date getActiveDate() {
		return activeDate;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setStartDate(LocalDateTime startDate) {

		this.startDate = startDate.toDate();
	}

	public void setStartDate(Date startDate) {

		this.startDate = startDate;
	}

	public void setStatus(Long statusId) {
		this.status = statusId;

	}

	public void setuserAction(String actionType) {
		this.userAction = actionType;
	}

	/*
	 * this method is written for retriving price data and for storing that data in
	 * transaction hsitory table
	 */

	public char getIsDeleted() {
		return isDeleted;
	}

	public char getBillingAlign() {
		return billingAlign;
	}

	public char isAutoRenewal() {
		return autoRenew;
	}

	public String getDisconnectReason() {
		return disconnectReason;
	}

	public String getUserAction() {
		return userAction;
	}

	public void updateDisconnectionstate() {
		this.endDate = DateUtils.getDateOfTenant();
		this.disconnectReason = "Change Plan";
		this.isDeleted = 'Y';
		this.userAction = UserActionStatusTypeEnum.DISCONNECTION.toString();
		this.status = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.DISCONNECTED).getId();

	}

	public void setRenewalDate(Date date) {
		this.startDate = date;

	}

	public void updateActivationDate(Date activeDate) {
		this.activeDate = activeDate;
	}

	public void setContractPeriod(Long contarctPeriod) {
		this.contarctPeriod = contarctPeriod;
	}

	public Long getClientServiceId() {
		return clientServiceId;
	}

	public void setClientServiceId(Long clientServiceId) {
		this.clientServiceId = clientServiceId;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public void setActiveDate(LocalDateTime activeDate) {
		this.activeDate = activeDate.toDate();
	}

}
