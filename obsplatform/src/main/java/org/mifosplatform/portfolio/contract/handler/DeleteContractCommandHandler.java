package org.mifosplatform.portfolio.contract.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.contract.service.ContractPeriodWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteContractCommandHandler implements NewCommandSourceHandler {

    private final ContractPeriodWritePlatformService writePlatformService;

    @Autowired
    public DeleteContractCommandHandler(final ContractPeriodWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.writePlatformService.deleteContract(command.entityId());
    }
}