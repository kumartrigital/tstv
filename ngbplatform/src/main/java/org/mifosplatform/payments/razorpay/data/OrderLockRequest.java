package org.mifosplatform.payments.razorpay.data;


public class OrderLockRequest {
private String razorpayPaymentId;
private String razorpayOrderId ;
private String razorpaySignature;

public OrderLockRequest(String razorpayPaymentId, String razorpayOrderId, String razorpaySignature) {
	super();
	this.razorpayPaymentId = razorpayPaymentId;
	this.razorpayOrderId = razorpayOrderId;
	this.razorpaySignature = razorpaySignature;
}
public String getRazorpayPaymentId() {
	return razorpayPaymentId;
}
public void setRazorpayPaymentId(String razorpayPaymentId) {
	this.razorpayPaymentId = razorpayPaymentId;
}
public String getRazorpayOrderId() {
	return razorpayOrderId;
}
public void setRazorpayOrderId(String razorpayOrderId) {
	this.razorpayOrderId = razorpayOrderId;
}
public String getRazorpaySignature() {
	return razorpaySignature;
}
public void setRazorpaySignature(String razorpaySignature) {
	this.razorpaySignature = razorpaySignature;
}
@Override
public String toString() {
	return "OrderLockRequest [razorpayPaymentId=" + razorpayPaymentId + ", razorpayOrderId=" + razorpayOrderId
			+ ", razorpaySignature=" + razorpaySignature + "]";
}

}
