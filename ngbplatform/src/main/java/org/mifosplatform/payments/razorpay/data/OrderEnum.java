package org.mifosplatform.payments.razorpay.data;

public enum OrderEnum {

	CREATED("CREATED","created"),
	ATTEMPTED("ATTEMPTED","attempted"),
	PAID("PAID","paid");

	private final String code;
	private final String value;

	private OrderEnum(final String value,final String code) {
		this.code=code;
		this.value = value;
	}

	
	public String getCode() {
		return code;
	}


	public String getValue() {
		return this.value;
	}
	
	public static OrderEnum fromInt(final Integer frequency) {

		OrderEnum orderEnum;
		
		switch (frequency) {
		case 1:
			orderEnum = OrderEnum.CREATED;
			break;
		case 2:
			orderEnum = OrderEnum.ATTEMPTED;
			break;
	
		default:
			orderEnum = OrderEnum.PAID;
			break;
		}
		return orderEnum;
	}
}
