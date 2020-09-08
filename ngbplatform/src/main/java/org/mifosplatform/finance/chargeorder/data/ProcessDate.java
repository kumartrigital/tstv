package org.mifosplatform.finance.chargeorder.data;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.stereotype.Service;

@Service
public class ProcessDate {
	
	public LocalDate processDate;
	
	public static LocalDate  fromJson(JsonCommand command){
		return command.localDateValueOfParameterNamed("systemDate");
	}

	
	public static LocalDateTime fromJsonDateTime(JsonCommand command) {
		return command.localDateTimeValueOfParameterNamed("systemDate");
	}
}
