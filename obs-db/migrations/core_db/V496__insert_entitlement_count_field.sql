
Drop procedure IF EXISTS entitlementCount;
DELIMITER //
create procedure addwalletinclientbal() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'entitlement_count'
     and TABLE_NAME = 'b_provisioning_request'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_provisioning_request 
add column `entitlement_count` int DEFAULT 0 After `version`;
END IF;
END //
DELIMITER ;
call entitlementCount();
Drop procedure IF EXISTS entitlementCount;

