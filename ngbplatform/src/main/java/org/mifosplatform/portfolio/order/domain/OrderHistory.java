package org.mifosplatform.portfolio.order.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDateTime;
import org.mifosplatform.infrastructure.core.service.DateTimeUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_orders_history")
public class OrderHistory extends AbstractPersistable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "transaction_type")
	private String transactionType;

	@Column(name = "transaction_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date transactionDate;

	@Column(name = "actual_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date actualDate;

	@Column(name = "prepare_id")
	private Long prepareId;

	@Column(name = "created_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name = "createdby_id")
	private Long createdbyId;

	@Column(name = "remarks")
	private String remarks;

	public OrderHistory(Long orderId, LocalDateTime transactionDate, LocalDateTime actualDate, Long provisioningId,
			String tranType, Long userId, String extensionReason) {
		this.orderId = orderId;
		this.transactionDate = transactionDate.toDate();
		this.actualDate = actualDate.toDate();
		this.prepareId = provisioningId;
		this.transactionType = tranType;
		this.createdbyId = userId;
		this.createdDate = DateTimeUtils.getDateTimeOfTenant().toDate();
		this.remarks = extensionReason;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = transactionDate.toDate();
	}

	public Date getActualDate() {
		return actualDate;
	}

	public void setActualDate(LocalDateTime actualDate) {
		this.actualDate = actualDate.toDate();
	}

	public Long getPrepareId() {
		return prepareId;
	}

	public void setPrepareId(Long prepareId) {
		this.prepareId = prepareId;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate.toDate();
	}

	public Long getCreatedbyId() {
		return createdbyId;
	}

	public void setCreatedbyId(Long createdbyId) {
		this.createdbyId = createdbyId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
