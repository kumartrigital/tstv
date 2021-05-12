package org.mifosplatform.portfolio.order.domain;

import java.math.BigDecimal;
import java.util.List;

import org.mifosplatform.portfolio.plan.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderPriceRepository extends

JpaRepository<OrderPrice, Long>,
JpaSpecificationExecutor<OrderPrice>{
	
	@Query("from OrderPrice op where op.orders =:orderId and is_deleted='N'")
	OrderPrice findOrders(@Param("orderId") Order orderId);
	
	@Transactional
	@Modifying
	@Query(value = "update b_order_price set price=:price where order_id in (select id from b_orders where plan_id=:planId and order_status=1)",nativeQuery = true)
	void updateOrderPriceAlongWithPlanPrice(@Param("price") BigDecimal price,@Param("planId") Long planId);
}
