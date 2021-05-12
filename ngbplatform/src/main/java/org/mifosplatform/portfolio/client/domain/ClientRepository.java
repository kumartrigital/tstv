/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

	
	@Query("from Client client where client.accountNumber=:accountId")
	Client findwithAccountNo(@Param("accountId") String accountId);
	
	@Query("from Client client where client.email=:email")
	Client findwithEmail(@Param("email") String email);
	
	@Query("from Client client where client.status = :statusEnum")
	List<Client> findByStatusEnum(@Param("statusEnum") Integer statusEnum);
	

	
}