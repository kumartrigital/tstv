package org.mifosplatform.portfolio.order.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_order_price")
public class OrderPricecharge extends AbstractPersistable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "product_id")
	private Long productId;

	@Column(name = "charge_code")
	private String chargeCode;

	@Column(name = "charge_type")
	private String chargeType;

	@Column(name = "price")
	private BigDecimal price;

	@Column(name = "charge_duration")
	private String chargeDuration;

	@Column(name = "duration_type")
	private String durationType;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "invoice_tilldate")
	private Date invoiceTillDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "bill_start_date")
	private Date billStartDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "next_billable_day")
	private Date nextBillableDay;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "bill_end_date")
	private Date billEndDate;

	@Column(name = "is_deleted")
	private char isDeleted = 'N';

	@Column(name = "is_addon")
	private char isAddon = 'N';

	@Column(name = "tax_inclusive")
	private boolean taxInclusive;

	@Column(name = "currency_id")
	private Long currencyId;

	@Column(name = "chargeOwner")
	private String chargeOwner;

	/*
	 * @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "order_id", insertable = true, updatable = true, nullable
	 * = true, unique = true) private Order orders;
	 */

	@Column(name = "order_id")
	private Long orders;

	public OrderPricecharge(final Long productId, final String chargeCode, final String chargeType,
			final BigDecimal price, final LocalDateTime invoiceTillDate, final String chargetype,
			final String chargeduration, final String durationType, final LocalDateTime billStartDate,
			final LocalDateTime billEndDate, boolean isTaxInclusive, final String chargeOwner) {

		this.orders = null;
		this.productId = productId;
		this.chargeCode = chargeCode;
		this.chargeType = chargetype;
		this.chargeDuration = chargeduration;
		this.durationType = durationType;
		this.price = price;
		if (invoiceTillDate == null) {
			this.invoiceTillDate = null;
		} else {
			this.invoiceTillDate = invoiceTillDate.toDate();
		}
		this.billStartDate = billStartDate.toDate();
		if (billEndDate == null) {
			this.billEndDate = null;
		} else {
			this.billEndDate = billEndDate.toDate() != null ? billEndDate.toDate() : null;
		}
		this.taxInclusive = isTaxInclusive;
		this.chargeOwner = chargeOwner;

	}

	public String getChargeOwner() {
		return chargeOwner;
	}

	public void setChargeOwner(String chargeOwner) {
		this.chargeOwner = chargeOwner;
	}

	public OrderPricecharge() {
		// TODO Auto-generated constructor stub
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	@Transient
	private Long orderId;

	/*
	 * public Long getServiceId() { return serviceId; }
	 */

	public Long getProductId() {
		return productId;
	}

	public void setIsAddon(char isAddon) {
		this.isAddon = isAddon;
	}

	public Date getNextBillableDay() {
		return nextBillableDay;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public boolean isTaxInclusive() {
		return taxInclusive;
	}

	/*
	 * public OrderDiscount getOrderDiscount() { return orderDiscount; }
	 */

	public void updateDates(LocalDateTime date) {
		this.billEndDate = date.toDate();
		// this.nextBillableDay=date.plusDays(1).toDate();
	}

	public char isAddon() {
		return isAddon;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public String getChargeType() {
		return chargeType;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getChargeDuration() {
		return chargeDuration;
	}

	public String getDurationType() {
		return durationType;
	}

	public Date getInvoiceTillDate() {
		return invoiceTillDate;
	}

	public void setInvoiceTillDate(LocalDateTime invoiceTillDate) {
		if (invoiceTillDate != null)
			this.invoiceTillDate = invoiceTillDate.toDate();
		else
			this.invoiceTillDate = null;
	}

	public char isIsDeleted() {
		return isDeleted;
	}

	public void delete() {
		this.isDeleted = 'y';

	}

	/*
	 * public Long getId() { return id; }
	 */

	public void setChargeDuration(String chargeDuration) {
		this.chargeDuration = chargeDuration;
	}

	public Date getBillStartDate() {
		return billStartDate;
	}

	public Date getBillEndDate() {
		return billEndDate;
	}

	public void setNextBillableDay(Date dateTime) {
		this.nextBillableDay = dateTime;

	}

	public void setPrice(BigDecimal price) {

		// BigDecimal price=command.bigDecimalValueOfParameterNamed("price");
		this.price = price;

	}

	public void setBillEndDate(LocalDateTime endDate) {

		if (endDate != null) {
			this.billEndDate = endDate.toDate();
		} else {
			this.billEndDate = null;
		}
	}

	public void setBillStartDate(LocalDateTime billstartDate) {
		this.billStartDate = billstartDate.toDate();

	}

	public void setChargeCode(String chargeCode) {
		this.chargeCode = chargeCode;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	public void setChargeDurationType(String durationType) {
		this.durationType = durationType;
	}

	public Long getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(Long currencyId) {
		this.currencyId = currencyId;
	}

	public void setDatesOnOrderStatus(LocalDateTime newStartdate, LocalDate renewalEndDate, String orderstatus) {

		if (this.isAddon == 'N') {
			if (orderstatus.equalsIgnoreCase("RENEWAL AFTER AUTOEXIPIRY")) {

				if (newStartdate != null) {
					this.billStartDate = newStartdate.toDate();
				}
				this.nextBillableDay = null;
				this.invoiceTillDate = null;
			}

			if (renewalEndDate != null) {
				this.billEndDate = renewalEndDate.toDate();
			} else {
				this.billEndDate = null;
			}
		}

	}

	/*
	 * public void addOrderDiscount(OrderDiscount orderDiscount) {
	 * orderDiscount.updateOrderPrice(this); this.orderDiscount=orderDiscount;
	 * 
	 * }
	 */
}
