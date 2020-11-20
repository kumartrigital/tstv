SET sql_safe_updates=0;

Update m_appuser set email='admin@clientmail.com' where id in (0,1,2);
--alter table m_office_balance add credit_limit decimal(24,4);
alter table m_office_balance add wallet_amount decimal(24,4);


DROP VIEW IF EXISTS `provisioning_requests_vw`;

Create view provisioning_requests_vw as Select a.id,a.client_id,a.request_type,a.status,a.created_date, 
b.response_message,b.response_status from b_provisioning_request a,
b_provisioning_request_detail b where a.id=b.provisioning_req_id ;

DROP TABLE IF EXISTS `m_office_statistics`;

CREATE TABLE IF NOT EXISTS `m_office_statistics` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `client_active` bigint(20) DEFAULT 0,
  `client_inactive` bigint(20) DEFAULT 0,
  `in_stock` bigint(20) DEFAULT 0,
  `stock_allocated` bigint(20) DEFAULT 0,  
  PRIMARY KEY (`id`),
  KEY `stats_office_key` (`office_id`),
  CONSTRAINT `stats_office_key` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert  into m_office_statistics  select id,id,0,0,0,0 from m_office;

drop  procedure IF EXISTS update_stat2;


DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_stat2`()
BEGIN
DECLARE v_finished INTEGER DEFAULT 0;
DECLARE v_officeid INTEGER DEFAULT 0;
DECLARE l_text varchar(20) default NULL;
DECLARE v_stock INTEGER DEFAULT 0;
DECLARE v_allocated INTEGER DEFAULT 0;

 DEClARE i_cursor CURSOR FOR
 Select office_id,case when counts=1 then 'Allocated' else 'In Stock' end as stock_status ,
	count(0) from (select office_id, case when client_id>0 then 1 else 0 end as counts 
	from b_item_detail where item_master_id=18) x group by 1,2;

 DECLARE CONTINUE HANDLER
        FOR NOT FOUND SET v_finished = 1;
 OPEN i_cursor;
 get_stats: LOOP

 FETCH i_cursor INTO v_officeid,l_text,v_stock;
 IF v_finished = 1 THEN
	LEAVE get_stats;
 END IF;
	if l_text ='Allocated' then
	 Update m_office_statistics set  stock_allocated=v_stock where office_id = v_officeid;
	else
	 Update m_office_statistics set  in_stock=v_stock where office_id = v_officeid;
	end if;
 END LOOP get_stats;
 CLOSE i_cursor;
 
END //
DELIMITER ;


drop procedure IF EXISTS update_stat1;

DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_stat1`()
BEGIN
DECLARE v_finished INTEGER DEFAULT 0;
DECLARE v_officeid INTEGER DEFAULT 0;
DECLARE l_text varchar(20) default NULL;
DECLARE v_stock INTEGER DEFAULT 0;
DECLARE v_allocated INTEGER DEFAULT 0;

 DEClARE i_cursor CURSOR FOR
 select c.id,b.status_enum,count(0) counts from b_client_service a, m_client b,m_office c 
 where a.client_id=b.id and b.office_id=c.id and a.status='ACTIVE' and b.status_enum in (300,600) group by b.status_enum,c.id;
 
 DECLARE CONTINUE HANDLER
        FOR NOT FOUND SET v_finished = 1;
 OPEN i_cursor;
 get_stats: LOOP

 FETCH i_cursor INTO v_officeid,l_text,v_stock;
 IF v_finished = 1 THEN
	LEAVE get_stats;
 END IF;
	if l_text ='300' then
	 Update m_office_statistics set  client_active=v_stock where office_id = v_officeid;
	else
	 Update m_office_statistics set  client_inactive=v_stock where office_id = v_officeid;
	end if;
 END LOOP get_stats;
 CLOSE i_cursor;
 
END //
DELIMITER ;

drop procedure IF EXISTS update_stats;


DELIMITER //
CREATE PROCEDURE update_stats()
BEGIN
insert into m_office_statistics  select id,id,0,0,0,0 from m_office where id not in (select office_id from m_office_statistics);
 call update_stat1();
 call update_stat2();
END //
DELIMITER ;



DROP EVENT  IF EXISTS dashboard;

SET GLOBAL event_scheduler = ON;


	

CREATE DEFINER=`root`@`localhost` EVENT `dashboard` ON SCHEDULE EVERY
 30 MINUTE DO call update_stats();





	
Select count(0) lco_cnt,sum(active) as c_active,sum(inactive) as c_inactive , sum(instock) as c_instock,sum(allocated) as c_allocated from (
select a.id,IFNULL(b.client_active, 0) as active,IFNULL(b.client_inactive, 0) as inactive
,IFNULL(b.in_stock, 0) as instock,IFNULL(b.stock_allocated, 0) as allocated
from m_office a left join m_office_statistics b on a.id=b.office_id where a.hierarchy like '%') X;


drop procedure IF EXISTS update_dash;

---> Single procedure 


DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_dash`()
BEGIN
	DECLARE v_finished INTEGER DEFAULT 0;
begin
DECLARE v_officeid INTEGER DEFAULT 0;
DECLARE l_text varchar(20) default NULL;
DECLARE v_stock INTEGER DEFAULT 0;
DECLARE v_allocated INTEGER DEFAULT 0;

 DEClARE i_cursor CURSOR FOR
  Select office_id,case when counts=1 then 'Allocated' else 'In Stock' end as stock_status ,
	count(0) from (select office_id, case when client_id>0 then 1 else 0 end as counts 
	from b_item_detail where item_master_id=18) x group by 1,2;


 DECLARE CONTINUE HANDLER
        FOR NOT FOUND SET v_finished = 1;
 OPEN i_cursor;
 get_stats: LOOP

 FETCH i_cursor INTO v_officeid,l_text,v_stock;
 IF v_finished = 1 THEN
	LEAVE get_stats;
 END IF;
	if l_text ='Allocated' then
	 Update m_office_statistics set  stock_allocated=v_stock where office_id = v_officeid;
	else
	 Update m_office_statistics set  in_stock=v_stock where office_id = v_officeid;
	end if;
 END LOOP get_stats;
 CLOSE i_cursor;
End;

Begin
DECLARE v_officeid INTEGER DEFAULT 0;
DECLARE l_text varchar(20) default NULL;
DECLARE v_stock INTEGER DEFAULT 0;
DECLARE v_allocated INTEGER DEFAULT 0;

 DEClARE i_cursor CURSOR FOR
 select c.id,a.status,count(0) counts from b_client_service a, m_client b,m_office c 
 where a.client_id=b.id and b.office_id=c.id and a.status='ACTIVE' and b.status_enum in (300,600) group by b.status_enum,c.id;

 DECLARE CONTINUE HANDLER
        FOR NOT FOUND SET v_finished = 1;
 OPEN i_cursor;
 get_stats: LOOP

 FETCH i_cursor INTO v_officeid,l_text,v_stock;
 IF v_finished = 1 THEN
	LEAVE get_stats;
 END IF;
	if l_text ='ACTIVE' then
	 Update m_office_statistics set  client_active=v_stock where office_id = v_officeid;
	else
	 Update m_office_statistics set  client_inactive=v_stock where office_id = v_officeid;
	end if;
 END LOOP get_stats;
 CLOSE i_cursor;
End;
 
END //
DELIMITER ;






