package org.mifosplatform.logistics.ownedhardware.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;

@Component
public class OwnedHardwareFromApiJsonDeserializer {
	private final Set<String> supportedParams = new HashSet<String>(Arrays.asList("allocationDate","itemType","serialNumber","provisioningSerialNumber",
			"locale","dateFormat","status", "authToken", "generatedKey", "returnUrl"));
	private final FromJsonHelper fromJsonHelper;  
	
	
	@Autowired
	public OwnedHardwareFromApiJsonDeserializer(final FromJsonHelper fromJsonHelper) {
		this.fromJsonHelper = fromJsonHelper;
	}
	
	public void validateForCreate(String json){
		
		if(StringUtils.isBlank(json)){
			throw new InvalidJsonException();
		}
		final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType(); 
		fromJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
		
		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseValidatorBuilder = new DataValidatorBuilder(dataValidationErrors);
		
		final JsonElement element = fromJsonHelper.parse(json);
		
		final LocalDate allocationDate = fromJsonHelper.extractLocalDateNamed("allocationDate", element);
		baseValidatorBuilder.reset().parameter("allocationDate").value(allocationDate).notBlank();
		
		Long itemType = null;
		
		if(fromJsonHelper.parameterExists("itemType", element)){
			itemType = fromJsonHelper.extractLongNamed("itemType", element);
		}	
		
		baseValidatorBuilder.reset().parameter("itemType").value(itemType).notBlank().integerGreaterThanZero();
		
		final String serialNumber= fromJsonHelper.extractStringNamed("serialNumber", element);
		baseValidatorBuilder.reset().parameter("serialNumber").value(serialNumber).notBlank().notExceedingLengthOf(100);
		
		final String provisioningSerialNumber= fromJsonHelper.extractStringNamed("provisioningSerialNumber", element);
		baseValidatorBuilder.reset().parameter("provisioningSerialNumber").value(provisioningSerialNumber).notBlank().notExceedingLengthOf(100);
	
			
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
		
		
	}

	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
