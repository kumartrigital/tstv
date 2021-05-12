INSERT IGNORE INTO `obstenant-default`.job (`name`,`display_name`,`cron_expression`,`cron_description`,`create_time`,`task_priority`,`group_name`,`previous_run_start_time`,`next_run_time`,`job_key`,`initializing_errorlog`,`is_active`,`currently_running`,`updates_allowed`,`scheduler_group`,`is_misfired`,`user_id`) VALUES ('UPCOMINGRENEWALOSD','UPCOMINGRENEWALOSD','0 30 23 1/1 * ? *','Every Once in day','2020-05-06 12:27:52',5,NULL,'2021-01-08 10:12:51','2021-01-08 23:30:00','UPCOMINGRENEWALOSDJobDetaildefault _ DEFAULT',NULL,1,0,1,0,0,1);

INSERT IGNORE INTO `obstenant-default`.job_parameters (`job_id`,`param_name`,`param_type`,`param_default_value`,`param_value`,`is_dynamic`,`query_values`) VALUES (18,'UPCOMINGRENEWALOSD','','','UPCOMINGRENEWALOSD','N',NULL);

