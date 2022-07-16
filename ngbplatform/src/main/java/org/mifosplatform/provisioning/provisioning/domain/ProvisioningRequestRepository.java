package org.mifosplatform.provisioning.provisioning.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProvisioningRequestRepository  extends JpaRepository<ProvisioningRequest, Long>, 
JpaSpecificationExecutor<ProvisioningRequest>{

	@Query("from ProvisioningRequest provisioningRequest where provisioningRequest.status = 'N'")
	List<ProvisioningRequest> findUnProcessedProvisioningRequests(); 
	
	
	@Query( value =  "select * from b_provisioning_request bpr  where request_type  ='Retrack' and clientservice_id  =:clientServiceId order by id desc limit 1",nativeQuery = true)
	ProvisioningRequest findLatestRetrackRequest( @Param("clientServiceId")   Long clientServiceId);
	
	
}
