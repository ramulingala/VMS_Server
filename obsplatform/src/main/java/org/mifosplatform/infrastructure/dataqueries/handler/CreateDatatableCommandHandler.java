package org.mifosplatform.infrastructure.dataqueries.handler;


import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hugo
 *
 */
@Service
public class CreateDatatableCommandHandler implements NewCommandSourceHandler {

    private final ReadWriteNonCoreDataService writePlatformService;

    @Autowired
    public CreateDatatableCommandHandler(final ReadWriteNonCoreDataService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.createDatatable(command);
    }
}