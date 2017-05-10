package org.mifosplatform.infrastructure.configuration.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class GlobalConfigurationDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public GlobalConfigurationDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForUpdate(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,ConfigurationConstants.UPDATE_CONFIGURATION_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(ConfigurationConstants.CONFIGURATION_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(ConfigurationConstants.ENABLED, element)) {
            final boolean enabledBool = this.fromApiJsonHelper.extractBooleanNamed(ConfigurationConstants.ENABLED, element);
            baseDataValidator.reset().parameter(ConfigurationConstants.ENABLED).value(enabledBool).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(ConfigurationConstants.VALUE, element)) {
            final String valueStr = this.fromApiJsonHelper.extractStringNamed(ConfigurationConstants.VALUE, element);
            baseDataValidator.reset().parameter(ConfigurationConstants.ENABLED).value(valueStr);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }
    
    public void validateForCreate(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,ConfigurationConstants.CREATE_CONFIGURATION_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(ConfigurationConstants.CONFIGURATION_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        
        final String strttls = this.fromApiJsonHelper.extractStringNamed(ConfigurationConstants.STARTTLS, element);
        baseDataValidator.reset().parameter(ConfigurationConstants.STARTTLS).value(strttls).notBlank();
        
        final String name = this.fromApiJsonHelper.extractStringNamed(ConfigurationConstants.NAME, element);
        baseDataValidator.reset().parameter(ConfigurationConstants.NAME).value(name).notBlank();
       
        final String mailId = this.fromApiJsonHelper.extractStringNamed(ConfigurationConstants.MAIL, element);
        baseDataValidator.reset().parameter(ConfigurationConstants.MAIL).value(mailId).notBlank();
        
        final String password = this.fromApiJsonHelper.extractStringNamed(ConfigurationConstants.PASSWORD, element);
        baseDataValidator.reset().parameter(ConfigurationConstants.PASSWORD).value(password).notBlank();
        
        final String hostName = this.fromApiJsonHelper.extractStringNamed(ConfigurationConstants.HOSTNAME, element);
        baseDataValidator.reset().parameter(ConfigurationConstants.HOSTNAME).value(hostName).notBlank();
        
        final String setContentString = this.fromApiJsonHelper.extractStringNamed(ConfigurationConstants.SETCONENTSTRING, element);
        baseDataValidator.reset().parameter(ConfigurationConstants.SETCONENTSTRING).value(setContentString).notBlank();
            

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }
}