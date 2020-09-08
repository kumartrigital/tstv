package org.mifosplatform.logistics.itemdetails.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemDetailsRepository extends JpaRepository<ItemDetails, Long> {

	@Query("from ItemDetails item where item.serialNumber = :macId")
	ItemDetails getInventoryItemDetailBySerialNum(@Param("macId") String macId);

	@Query("from ItemDetails item where item.cartoonNumber = :macId")
	List<ItemDetails> getInventoryItemDetailByCartonNum(@Param("macId") String macId);

	@Query("from ItemDetails item where item.serialNumber = :serialNo")
	ItemDetails getItemDetailBySerialNum(@Param("serialNo") String serialNo);

}
