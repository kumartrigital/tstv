Drop procedure IF EXISTS columnType;
DELIMITER //
create procedure columnType()
Begin
IF NOT EXISTS (
SELECT * FROM information_schema.COLUMNS
WHERE COLUMN_NAME = 'is_escalated'
and TABLE_NAME = 'b_office_ticket'
and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_office_ticket`
ADD COLUMN `type` VARCHAR(45) NOT NULL AFTER `is_escalated`;

END IF;
END //
DELIMITER ;
call columnType();
Drop procedure IF EXISTS columnType;

Drop procedure IF EXISTS columnOfficeType;
DELIMITER //
create procedure columnOfficeType()
Begin
IF NOT EXISTS (
SELECT * FROM information_schema.COLUMNS
WHERE COLUMN_NAME = 'team_user_id'
and TABLE_NAME = 'b_ticket_master'
and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_ticket_master`
ADD COLUMN `type` VARCHAR(45) NOT NULL AFTER `team_user_id`;

END IF;
END //
DELIMITER ;
call columnOfficeType();
Drop procedure IF EXISTS columnOfficeType;
