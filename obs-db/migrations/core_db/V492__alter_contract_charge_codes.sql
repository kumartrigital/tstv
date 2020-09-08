ALTER TABLE `obstenant-default`.`b_contract_period` 
ADD COLUMN `status` TINYINT NULL AFTER `is_deleted`;

ALTER TABLE `obstenant-default`.`b_charge_codes` 
ADD COLUMN `status` VARCHAR(45) NULL AFTER `is_aggregate`;

update b_contract_period set status=0 where id!=1;
update b_contract_period set status=1 where id=1;

update b_charge_codes set status=0 where id!=8;
update b_charge_codes set status=1 where id=8;


