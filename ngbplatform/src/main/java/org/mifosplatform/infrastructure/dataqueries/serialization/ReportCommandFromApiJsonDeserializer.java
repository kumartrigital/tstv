/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class ReportCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("reportName", "reportType", "reportSubType",
            "reportCategory", "description", "reportSql", "useReport","reportParameters"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ReportCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors);

        final JsonElement element = fromApiJsonHelper.parse(json);
        
        	final String reportName = fromApiJsonHelper.extractStringNamed("reportName", element);
        	baseDataValidator.reset().parameter("reportName").value(reportName).notBlank().notExceedingLengthOf(50);
        	final String reportType = fromApiJsonHelper.extractStringNamed("reportType", element);
        	baseDataValidator.reset().parameter("reportType").value(reportType).notBlank();
        	final String  reportCategory = fromApiJsonHelper.extractStringNamed("reportCategory", element);
        	baseDataValidator.reset().parameter("reportCategory").value(reportCategory).notBlank().notExceedingLengthOf(50);
        	final String  reportSql = fromApiJsonHelper.extractStringNamed("reportSql", element);
        	baseDataValidator.reset().parameter("reportSql").value(reportSql).notBlank();
   
   
        	
        	//validating details
        	final JsonArray reportParameters = fromApiJsonHelper.extractJsonArrayNamed("reportParameters", element);
            String[] details = new String[reportParameters.size()];
            final int detailsArraySize = reportParameters.size();
            baseDataValidator.reset().parameter("reportParameters").value(detailsArraySize).integerGreaterThanZero();
            
//            if(detailsArraySize > 0){
//	    	    for(int i = 0; i < reportParameters.size(); i++){
//	    	    	details[i] = reportParameters.get(i).toString();
//	    	    }
//	    	    for (final String detail : details) {
//	    	    	
//	    	    	final JsonElement detailElement = fromApiJsonHelper.parse(detail);
//	    	    
//	    	    	final Long parameterId = this.fromApiJsonHelper.extractLongNamed("parameterId", detailElement);
//	    	    	baseDataValidator.reset().parameter("parameterId").value(parameterId).notNull();
//	    	    	
//	    	    	final String parameterName = this.fromApiJsonHelper.extractStringNamed("parameterName", detailElement);
//	    	    	baseDataValidator.reset().parameter("parameterName").value(parameterName).notNull();
//	    	    	
//	    	    	final Long reportParameterId = this.fromApiJsonHelper.extractLongNamed("reportParameterId", detailElement);
//	    	    	baseDataValidator.reset().parameter("reportParameterId").value(reportParameterId).notNull();
//	    	    	
//	    	    	final String reportParameterName = this.fromApiJsonHelper.extractStringNamed("reportParameterName", detailElement);
//	    	    	baseDataValidator.reset().parameter("reportParameterName").value(reportParameterName).notNull();
//	    	    	
//	    	    }
//            }
        	
        	
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { 
        	throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist","Validation errors exist.",dataValidationErrors); }
    }
}