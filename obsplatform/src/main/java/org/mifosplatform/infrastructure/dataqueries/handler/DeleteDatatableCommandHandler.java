package org.mifosplatform.infrastructure.dataqueries.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hugo
 *
 */
@Service
public class DeleteDatatableCommandHandler implements NewCommandSourceHandler {

	private final ReadWriteNonCoreDataService writePlatformService;

	@Autowired
	public DeleteDatatableCommandHandler(
			final ReadWriteNonCoreDataService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		final String datatableName = command.getUrl().replaceAll(
				"/datatables/", "");

		this.writePlatformService.deleteDatatable(datatableName);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.withResourceIdAsString(datatableName).build();
	}
}