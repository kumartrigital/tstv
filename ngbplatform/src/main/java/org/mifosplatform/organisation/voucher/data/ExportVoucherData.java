package org.mifosplatform.organisation.voucher.data;

import java.util.Date;
import java.util.List;

public class ExportVoucherData {
	
	
	private String id;
	
	private Date requestDate;

	private String status;

	private Long saleReqNo;

	private Long quantity;
	
	private Long requestBy;
	
	private List<VoucherData> voucherData;
	public ExportVoucherData()
	{
		
	}
	public ExportVoucherData(String id, Date requestDate, String status, Long saleReqNo, Long quantity,
			Long requestBy) {
		super();
		this.id = id;
		this.requestDate = requestDate;
		this.status = status;
		this.saleReqNo = saleReqNo;
		this.quantity = quantity;
		this.requestBy = requestBy;
	}

	public List<VoucherData> getVoucherData() {
		return voucherData;
	}
	public void setVoucherData(List<VoucherData> voucherData) {
		this.voucherData = voucherData;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}

	public Long getRequestBy() {
		return requestBy;
	}

	public Long getSaleReqNo() {
		return saleReqNo;
	}
	public void setSaleReqNo(Long saleReqNo) {
		this.saleReqNo = saleReqNo;
	}
	public void setRequestBy(Long requestBy) {
		this.requestBy = requestBy;
	}

}
