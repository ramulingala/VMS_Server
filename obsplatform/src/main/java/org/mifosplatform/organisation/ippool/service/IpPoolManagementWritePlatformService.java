package org.mifosplatform.organisation.ippool.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface IpPoolManagementWritePlatformService {

	CommandProcessingResult createIpPoolManagement(JsonCommand command);

	CommandProcessingResult editIpPoolManagement(JsonCommand command);

	CommandProcessingResult updateIpStatus(Long entityId);

	CommandProcessingResult updateIpDescription(JsonCommand command);

	CommandProcessingResult updateIpAddressStatus(JsonCommand command);

	CommandProcessingResult updateStaticIpStatus(JsonCommand command);
	

}
