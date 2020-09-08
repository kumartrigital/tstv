package org.mifosplatform.finance.paymentsgateway.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 
 * @author ashokreddy
 *
 */
public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long>, JpaSpecificationExecutor<PaymentGateway> {

	@Query("from PaymentGateway paymentGateway where paymentGateway.paymentId=:paymentId")
	PaymentGateway findPaymentDetailsByPaymentId(@Param("paymentId") String paymentId);

}

