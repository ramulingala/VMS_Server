/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.configuration.exception.ConfigurationPropertyNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.address.domain.Address;
import org.mifosplatform.organisation.address.domain.AddressRepository;
import org.mifosplatform.organisation.groupsdetails.domain.GroupsDetails;
import org.mifosplatform.organisation.groupsdetails.domain.GroupsDetailsRepository;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.client.data.ClientDataValidator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientAdditionalFieldsRepository;
import org.mifosplatform.portfolio.client.domain.ClientAdditionalfields;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.client.exception.ClientAdditionalDataNotFoundException;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.InvalidClientStateTransitionException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.property.domain.PropertyHistoryRepository;
import org.mifosplatform.portfolio.property.domain.PropertyMaster;
import org.mifosplatform.portfolio.property.domain.PropertyMasterRepository;
import org.mifosplatform.portfolio.property.domain.PropertyTransactionHistory;
import org.mifosplatform.portfolio.property.exceptions.PropertyCodeAllocatedException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ClientWritePlatformServiceJpaRepositoryImpl.class);

    private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
    private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
   //private final PrepareRequestWriteplatformService prepareRequestWriteplatformService;
    private final PortfolioCommandSourceWritePlatformService  portfolioCommandSourceWritePlatformService;
    //private final PlanRepository planRepository;
    //private final OrderReadPlatformService orderReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ConfigurationRepository configurationRepository;
    //private final ServiceParametersRepository serviceParametersRepository;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
    //private final ProvisioningWritePlatformService ProvisioningWritePlatformService;
    //private final OrderRepository orderRepository;
    private final PlatformSecurityContext context;
    private final OfficeRepository officeRepository;
    private final ClientAdditionalFieldsRepository clientAdditionalFieldsRepository;
    private final AddressRepository addressRepository;
    //private final SelfCareRepository selfCareRepository;
    private final CodeValueRepository codeValueRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientDataValidator fromApiJsonDeserializer;
    private final GroupsDetailsRepository groupsDetailsRepository;
    //private final ProvisioningActionsRepository provisioningActionsRepository;
    //private final ProcessRequestRepository processRequestRepository;
    private final PropertyMasterRepository propertyMasterRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;
    //private final SelfCareTemporaryRepository selfCareTemporaryRepository;
    
   

    @Autowired
    public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,final AddressRepository addressRepository,
            final ClientRepositoryWrapper clientRepository, final OfficeRepository officeRepository,final ClientDataValidator fromApiJsonDeserializer, 
            final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,final ActiondetailsWritePlatformService actiondetailsWritePlatformService,final ConfigurationRepository configurationRepository,
            final ActionDetailsReadPlatformService actionDetailsReadPlatformService,final CodeValueRepository codeValueRepository,
            /*final OrderReadPlatformService orderReadPlatformService,*/final GroupsDetailsRepository groupsDetailsRepository,/*final OrderRepository orderRepository,final PlanRepository planRepository,*/
            /*final PrepareRequestWriteplatformService prepareRequestWriteplatformService,*/final ClientReadPlatformService clientReadPlatformService,
            /*final SelfCareRepository selfCareRepository,*/final PortfolioCommandSourceWritePlatformService  portfolioCommandSourceWritePlatformService,
            final ClientAdditionalFieldsRepository clientAdditionalFieldsRepository,
            final PropertyMasterRepository propertyMasterRepository, final PropertyHistoryRepository propertyHistoryRepository
            /*final SelfCareTemporaryRepository selfCareTemporaryRepository*/) {
    	
        this.context = context;
        //this.ProvisioningWritePlatformService=ProvisioningWritePlatformService;
        this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
        //this.prepareRequestWriteplatformService=prepareRequestWriteplatformService;
        //this.planRepository=planRepository;
        this.groupsDetailsRepository=groupsDetailsRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        //this.orderReadPlatformService=orderReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        //this.serviceParametersRepository = serviceParametersRepository;
        this.clientAdditionalFieldsRepository = clientAdditionalFieldsRepository;
        this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
        this.portfolioCommandSourceWritePlatformService=portfolioCommandSourceWritePlatformService;
        //this.orderRepository=orderRepository;
        this.clientRepository = clientRepository;
        this.addressRepository=addressRepository;
        //this.selfCareTemporaryRepository = selfCareTemporaryRepository;
        this.officeRepository = officeRepository;
        //this.provisioningActionsRepository=provisioningActionsRepository;
        //this.selfCareRepository=selfCareRepository;
        this.codeValueRepository=codeValueRepository;
        this.configurationRepository=configurationRepository;
       // this.processRequestRepository = processRequestRepository;
        this.propertyMasterRepository = propertyMasterRepository;
        this.propertyHistoryRepository = propertyHistoryRepository;
       
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClient(final Long clientId,final JsonCommand command) {

        try {

            final AppUser currentUser = this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateClose(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final LocalDate closureDate = command.localDateValueOfParameterNamed(ClientApiConstants.closureDateParamName);
            final Long closureReasonId = command.longValueOfParameterNamed(ClientApiConstants.closureReasonIdParamName);
            final CodeValue closureReason = this.codeValueRepository.findByCodeNameAndId(ClientApiConstants.CLIENT_CLOSURE_REASON, closureReasonId);
            
            //final List<OrderData> orderDatas=this.orderReadPlatformService.getActivePlans(clientId, null);
            
            /*if(!orderDatas.isEmpty()){
            	
            	 throw new ActivePlansFoundException(clientId);
            }*/

            if (ClientStatus.fromInt(client.getStatus()).isClosed()) {
                final String errorMessage = "Client is already closed.";
                throw new InvalidClientStateTransitionException("close", "is.already.closed", errorMessage);
            } 

            if (client.isNotPending() && client.getActivationLocalDate().isAfter(closureDate)) {
                final String errorMessage = "The client closureDate cannot be before the client ActivationDate.";
                throw new InvalidClientStateTransitionException("close", "date.cannot.before.client.actvation.date", errorMessage,
                        closureDate, client.getActivationLocalDate());
            }

            client.close(currentUser,closureReason, closureDate.toDate());
            this.clientRepository.saveAndFlush(client);
            
            /*if(client.getEmail() != null){
            	final SelfCare selfCare=this.selfCareRepository.findOneByEmail(client.getEmail());
            	 if(selfCare != null){
            		 selfCare.setIsDeleted(true);
            		 this.selfCareRepository.save(selfCare);
            	 }
               final SelfCareTemporary selfCareTemporary  =this.selfCareTemporaryRepository.findOneByEmailId(client.getEmail());
               if(selfCareTemporary !=null){
            	   selfCareTemporary.delete();
            	   this.selfCareTemporaryRepository.save(selfCareTemporary);
               }
            }*/
            
            
            final List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CLOSE_CLIENT);
			if(actionDetaislDatas.size() != 0){
			this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,command.entityId(), clientId.toString(),null);
			}
			
			 /* ProvisionActions provisionActions=this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_CLOSE_CLIENT);
				
	            if(provisionActions != null && provisionActions.isEnable() == 'Y'){
				
					this.ProvisioningWritePlatformService.postDetailsForProvisioning(clientId,Long.valueOf(0),ProvisioningApiConstants.REQUEST_TERMINATION,
							               provisionActions.getProvisioningSystem(),null);
				}*/
			
			
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    
    	}

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

    	final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId", "Client with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
            
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo", "Client with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        }else if (realCause.getMessage().contains("username")) {
            final String username = command.stringValueOfParameterNamed("username");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.username", "Client with username" + username
                    + "` already exists", "username", username);
        }else if (realCause.getMessage().contains("email_key")) {
            final String email = command.stringValueOfParameterNamed("email");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.email", "Client with email `" + email
                    + "` already exists", "email", email);
            
        }else if (realCause.getMessage().contains("login_key")) {
            final String login = command.stringValueOfParameterNamed("login");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.login", "Client with login `" + login
                    + "` already exists", "login", login);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    
    @Override
    public CommandProcessingResult createClient(final JsonCommand command) {

        try {
            context.authenticatedUser();
             Configuration configuration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SELFCAREUSER);
             
             Configuration propertyConfiguration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_PROPERTY_MASTER);
             boolean isPropertyConfiguration = false;
            
            if(configuration == null){
            	throw new ConfigurationPropertyNotFoundException(ConfigurationConstants.CONFIG_IS_SELFCAREUSER);
            }
            
            isPropertyConfiguration = (propertyConfiguration == null) ? false : propertyConfiguration.isEnabled();
            
            this.fromApiJsonDeserializer.validateForCreate(command.json(),configuration.isEnabled(),isPropertyConfiguration);
            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);
            final Office clientOffice = this.officeRepository.findOne(officeId);

            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Long groupId = command.longValueOfParameterNamed(ClientApiConstants.groupIdParamName);
            final Group clientParentGroup = null;
            PropertyMaster propertyMaster=null;
           
			if(propertyConfiguration != null && propertyConfiguration.isEnabled()) {		
				 propertyMaster=this.propertyMasterRepository.findoneByPropertyCode(command.stringValueOfParameterNamed("addressNo"));
				if(propertyMaster != null && propertyMaster.getClientId() != null ){
					throw new PropertyCodeAllocatedException(propertyMaster.getPropertyCode());
				}
			}

            final Client newClient = Client.createNew(clientOffice, clientParentGroup, command);
            this.clientRepository.save(newClient);
            
            final Address address = Address.fromJson(newClient.getId(),command);
			this.addressRepository.save(address);

            if (newClient.isAccountNumberRequiresAutoGeneration()) {
                final AccountNumberGenerator accountNoGenerator = this.accountIdentifierGeneratorFactory
                        .determineClientAccountNoGenerator(newClient.getId());
                newClient.updateAccountNo(accountNoGenerator.generate());
                this.clientRepository.saveAndFlush(newClient);
            }

			if (configuration.isEnabled()) {

				final JSONObject selfcarecreation = new JSONObject();
				selfcarecreation.put("userName", newClient.getEmail());
				selfcarecreation.put("uniqueReference", newClient.getEmail());
				selfcarecreation.put("clientId", newClient.getId());
				selfcarecreation.put("device", command.stringValueOfParameterNamed("device"));
				selfcarecreation.put("mailNotification", true);
				selfcarecreation.put("password", newClient.getPassword());

				final CommandWrapper selfcareCommandRequest = new CommandWrapperBuilder().createSelfCare()
						.withJson(selfcarecreation.toString()).build();
				this.portfolioCommandSourceWritePlatformService.logCommandSource(selfcareCommandRequest);
			}
			
			 //for property code updation with client details
				//configuration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_PROPERTY_MASTER);
				if(propertyConfiguration != null && propertyConfiguration.isEnabled()) {		
				//	PropertyMaster propertyMaster=this.propertyMasterRepository.findoneByPropertyCode(address.getAddressNo());
					if(propertyMaster !=null){
						propertyMaster.setClientId(newClient.getId());
						propertyMaster.setStatus(CodeNameConstants.CODE_PROPERTY_OCCUPIED);
					    this.propertyMasterRepository.saveAndFlush(propertyMaster);
					    PropertyTransactionHistory propertyHistory = new PropertyTransactionHistory(DateUtils.getLocalDateOfTenant(),propertyMaster.getId(),
					    		CodeNameConstants.CODE_PROPERTY_ALLOCATE,newClient.getId(),propertyMaster.getPropertyCode());
					    this.propertyHistoryRepository.save(propertyHistory);
					}
					
				}
            
            final List<ActionDetaislData> actionDetailsDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_CLIENT);
            if(!actionDetailsDatas.isEmpty()){
            this.actiondetailsWritePlatformService.AddNewActions(actionDetailsDatas,newClient.getId(),newClient.getId().toString(),null);
            }
            
           // ProvisionActions provisionActions=this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_CREATE_CLIENT);
			
            /*if(provisionActions != null && provisionActions.isEnable() == 'Y'){
				this.prepareRequestWriteplatformService.prepareRequestForRegistration(newClient.getId(),provisionActions.getAction(),
						   provisionActions.getProvisioningSystem());
				//this.ProvisioningWritePlatformService.postDetailsForProvisioning(newClient.getId(),Long.valueOf(0),ProvisioningApiConstants.REQUEST_CLIENT_ACTIVATION,
						              // provisionActions.getProvisioningSystem(),null);
			}*/

            
            
            return new CommandProcessingResultBuilder() 
                    .withCommandId(command.commandId()) 
                    .withOfficeId(clientOffice.getId()) 
                    .withClientId(newClient.getId())
                    .withResourceIdAsString(newClient.getId().toString())
                    .withGroupId(groupId) 
                    .withEntityId(newClient.getId()) 
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        } catch (JSONException e) {
        	   return CommandProcessingResult.empty();
		}
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {

        try {
            context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            
            final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);
            final Office clientOffice = this.officeRepository.findOne(officeId);
            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }
            final Map<String, Object> changes = clientForUpdate.update(command);
            clientForUpdate.setOffice(clientOffice);
            
            this.clientRepository.saveAndFlush(clientForUpdate);
            
            Configuration isSelfcareUser = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SELFCAREUSER);
            /*if(isSelfcareUser.isEnabled()){
            	SelfCare selfCare =selfCareRepository.findOneByClientId(clientId);
            	if(selfCare != null){
            	String newUserName = command.stringValueOfParameterNamed("userName");
            	String newPassword = command.stringValueOfParameterNamed("password");
            	String existingUserName = selfCare.getUserName();
            	String existingPassword = selfCare.getPassword();
            	
                if((selfCare != null) && (newUserName != null && newPassword != null) && (!newUserName.isEmpty() && !newPassword.isEmpty()) &&
                		((!existingUserName.equalsIgnoreCase(newUserName)) || (!existingPassword.equalsIgnoreCase(newPassword)))){				
                	
                	selfCare.setUserName(newUserName);
                	selfCare.setPassword(newPassword);
                	
    				this.selfCareRepository.save(selfCare);
    				
    				ProvisionActions provisionActions=this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_Change_CREDENTIALS);
    				if(provisionActions.getIsEnable() == 'Y'){
    					JSONObject object = new JSONObject();
    					try {
							object.put("newUserName", newUserName);
							object.put("newPassword", newPassword);
							object.put("existingUserName", existingUserName);
							object.put("existingPassword", existingPassword);
						} catch (JSONException e) {
							e.printStackTrace();
						}
    					 ProcessRequest processRequest = new ProcessRequest(Long.valueOf(0), clientId, Long.valueOf(0),
    							 provisionActions.getProvisioningSystem(),provisionActions.getAction(), 'N', 'N');

    					 ProcessRequestDetails processRequestDetails = new ProcessRequestDetails(Long.valueOf(0),
    							 Long.valueOf(0), object.toString(), "Recieved",
    							 null, DateUtils.getDateOfTenant(), null, null, null, 'N', provisionActions.getAction(), null);

    					 processRequest.add(processRequestDetails);
    					 this.processRequestRepository.save(processRequest);
    					
    				}
                }
    			}
            }*/
            
            if (changes.containsKey(ClientApiConstants.groupParamName)) {
            	
            		//final List<ServiceParameters> serviceParameters=this.serviceParametersRepository.findGroupNameByclientId(clientId);
            	   String newGroup=null;
            	   if(clientForUpdate.getGroupName() != null){
            		   final GroupsDetails groupsDetails=this.groupsDetailsRepository.findOne(clientForUpdate.getGroupName());
            		   newGroup=groupsDetails.getGroupName();
            	   }
            		   //for(ServiceParameters serviceParameter:serviceParameters){
            		   
            			 //final Order  order=this.orderRepository.findOne(serviceParameters.get(0).getOrderId());
            		   
            			 //final Plan plan=this.planRepository.findOne(order.getPlanId());
            			// final String oldGroup=serviceParameter.getParameterValue();
            		   /*if(newGroup == null){
            			   newGroup=plan.getPlanCode();
            		   }*/
            		  // serviceParameter.setParameterValue(newGroup);
            		   //this.serviceParametersRepository.saveAndFlush(serviceParameter);
            		   
                      /*if(order.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())){
                    	  final CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order, plan, UserActionStatusTypeEnum.CHANGE_GROUP.toString());
               	        this.ProvisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getCode(),UserActionStatusTypeEnum.CHANGE_GROUP.toString(),
               			processingResult.resourceId(),oldGroup,null,order.getId(),plan.getProvisionSystem(),null);
                      }*/
            	   }
            		
            	//}
           
            return new CommandProcessingResultBuilder() 
                    .withCommandId(command.commandId()) 
                    .withOfficeId(clientForUpdate.officeId()) 
                    .withClientId(clientId) 
                    .withEntityId(clientId) 
                    .with(changes) 
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult activateClient(final Long clientId, final JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateActivation(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

            client.activate(fmt, activationDate);

            this.clientRepository.saveAndFlush(client);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(client.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final String imageName, final InputStream inputStream) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(inputStream, imageUploadLocation, imageName);

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException(imageName);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClientImage(final Long clientId) {

        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

        // delete image from the file system
        if (StringUtils.isNotEmpty(client.imageKey())) {
            FileUtils.deleteClientImage(clientId, client.imageKey());
        }
        return updateClientImage(clientId, client, null);
    }

    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final Base64EncodedImage encodedImage) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(encodedImage, imageUploadLocation, "image");

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException("image");
        }
    }

    private String setupForClientImageUpdate(final Long clientId, final Client client) {
        if (client == null) { throw new ClientNotFoundException(clientId); }

        final String imageUploadLocation = FileUtils.generateClientImageParentDirectory(clientId);
        // delete previous image from the file system
        if (StringUtils.isNotEmpty(client.imageKey())) {
            FileUtils.deleteClientImage(clientId, client.imageKey());
        }

        /** Recursively create the directory if it does not exist **/
        if (!new File(imageUploadLocation).isDirectory()) {
            new File(imageUploadLocation).mkdirs();
        }
        return imageUploadLocation;
    }

    private CommandProcessingResult updateClientImage(final Long clientId, final Client client, final String imageLocation) {
        client.updateImageKey(imageLocation);
        this.clientRepository.save(client);

        return new CommandProcessingResult(clientId);
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    /* (non-Javadoc)
     * @see #updateClientTaxExemption(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override
	public CommandProcessingResult updateClientTaxExemption(final Long clientId,final JsonCommand command) {
		
		Client clientTaxStatus=null;
		
		try{
			 this.context.authenticatedUser();
			 clientTaxStatus = this.clientRepository.findOneWithNotFoundDetection(clientId);
			 char taxValue=clientTaxStatus.getTaxExemption();
			 final boolean taxStatus=command.booleanPrimitiveValueOfParameterNamed("taxExemption");
			 if(taxStatus){
				  taxValue='Y';
				  clientTaxStatus.setTaxExemption(taxValue);
			 }else{
				 taxValue='N';
				 clientTaxStatus.setTaxExemption(taxValue);
			 }
			 this.clientRepository.save(clientTaxStatus); 
			 return new CommandProcessingResultBuilder().withEntityId(clientTaxStatus.getId()).build();
		 }catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
			 return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

    /* (non-Javadoc)
     * @see #updateClientBillModes(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override

	public CommandProcessingResult updateClientBillModes(final Long clientId,final JsonCommand command) {

		Client clientBillMode=null;
	
		try{
			 this.context.authenticatedUser();
			 this.fromApiJsonDeserializer.ValidateBillMode(command);
			 clientBillMode=this.clientRepository.findOneWithNotFoundDetection(clientId);
			 final String billMode=command.stringValueOfParameterNamed("billMode");
			 if(billMode.equals(clientBillMode.getBillMode())==false){
				 clientBillMode.setBillMode(billMode);
			 }else{
				 
			 }
		 this.clientRepository.save(clientBillMode); 
		 return new CommandProcessingResultBuilder().withCommandId(command.commandId())
				 .withEntityId(clientBillMode.getId()).build();
		}catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
		
	}

    /* (non-Javadoc)
     * @see #createClientParent(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override

	public CommandProcessingResult createParentClient(final Long entityId,final JsonCommand command) {
  
			Client childClient=null;
			Client parentClient=null;
		
				try {
					this.context.authenticatedUser();
					this.fromApiJsonDeserializer.ValidateParent(command);
					final String parentAcntId=command.stringValueOfParameterNamed("accountNo");
					childClient = this.clientRepository.findOneWithNotFoundDetection(entityId);
					//count no of childs for a given client 
					final Boolean count =this.clientReadPlatformService.countChildClients(entityId);
					parentClient=this.clientRepository.findOneWithAccountId(parentAcntId);
					
						if(parentClient.getParentId() == null && !parentClient.getId().equals(childClient.getId())&&count.equals(false)){	
							childClient.setParentId(parentClient.getId());
							this.clientRepository.saveAndFlush(childClient);
						}else if(parentClient.getId().equals(childClient.getId())){
							final String errorMessage="himself can not be parent to his account.";
							throw new InvalidClientStateTransitionException("Not parent", "himself.can.not.be.parent.to his.account", errorMessage);
						}else if(count){ 
							final String errorMessage="he is already parent to some other clients";
							throw new InvalidClientStateTransitionException("Not Parent", "he.is. already. a parent.to.some other clients", errorMessage);
						}else{
							final String errorMessage="can not be parent to this account.";
							throw new InvalidClientStateTransitionException("Not parent", "can.not.be.parent.to this.account", errorMessage);
						}
						
				return new CommandProcessingResultBuilder().withEntityId(childClient.getId()).withClientId(childClient.getId()).build();
						
			  	}catch(DataIntegrityViolationException dve){
					handleDataIntegrityIssues(command, dve);
					return new CommandProcessingResult(Long.valueOf(-1));
				}
		}
	
	
	/* (non-Javadoc)
	 * @see #deleteChildFromParentClient(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override

	public CommandProcessingResult deleteChildFromParentClient(final Long childId, final JsonCommand command) {
		
		try {
			context.authenticatedUser();
			Client childClient = this.clientRepository.findOneWithNotFoundDetection(childId);
			final Long parentId=childClient.getParentId();
			childClient.setParentId(null);
			this.clientRepository.saveAndFlush(childClient);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(parentId).build();
	
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

	@Override
	public CommandProcessingResult createClientAdditionalInfo(JsonCommand command, Long entityId) {
            try{
                 this.context.authenticatedUser();
     				
     			final Long genderId = command.longValueOfParameterNamed(ClientApiConstants.genderParamName);
     			final CodeValue gender = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_GENDER, genderId);
     				
     			final Long nationalityId = command.longValueOfParameterNamed(ClientApiConstants.nationalityParamName);
     			final CodeValue nationality = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_NATIONALITY, nationalityId);
     				
     			final Long customerTypeId = command.longValueOfParameterNamed(ClientApiConstants.idTypeParamName);
     			final CodeValue customerIdentifier = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_CUSTOMER_IDENTIFIER, customerTypeId);
     			
     			final Long prefLangId = command.longValueOfParameterNamed(ClientApiConstants.preferredLangParamName);
     			final CodeValue preferLang = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_PREFER_LANG, prefLangId);
     				
     			final Long prefCommId = command.longValueOfParameterNamed(ClientApiConstants.preferredCommunicationParamName);
     			final CodeValue preferCommunication = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_PREFER_COMMUNICATION, prefCommId);
     				
     			final Long ageGroupId = command.longValueOfParameterNamed(ClientApiConstants.ageGroupParamName);
     			final CodeValue ageGroup = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_AGE_GROUP, ageGroupId);
     			
     			ClientAdditionalfields clientAdditionalData = ClientAdditionalfields.fromJson(entityId,gender,nationality,customerIdentifier,preferLang,preferCommunication,ageGroup,command);
     			this.clientAdditionalFieldsRepository.save(clientAdditionalData);
     			
                 return new CommandProcessingResult(Long.valueOf(entityId));
            }catch(DataIntegrityViolationException dve){
            	handleDataIntegrityIssues(command, dve);
    			return new CommandProcessingResult(Long.valueOf(-1));
            }
	}

	@Override
	public CommandProcessingResult updateClientAdditionalInfo(JsonCommand command) {
		try{
				
			   ClientAdditionalfields additionalfields=this.clientAdditionalFieldsRepository.findOneByClientId(command.entityId());
			   if(additionalfields == null){
				   throw new ClientAdditionalDataNotFoundException(command.entityId());
			   }
				final Long genderId = command.longValueOfParameterNamed(ClientApiConstants.genderParamName);
				final CodeValue gender = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_GENDER, genderId);
					
				final Long nationalityId = command.longValueOfParameterNamed(ClientApiConstants.nationalityParamName);
				final CodeValue nationality = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_NATIONALITY, nationalityId);
					
				final Long customerTypeId = command.longValueOfParameterNamed(ClientApiConstants.idTypeParamName);
				final CodeValue customerIdentifier = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_CUSTOMER_IDENTIFIER, customerTypeId);
				
				final Long prefLangId = command.longValueOfParameterNamed(ClientApiConstants.preferredLangParamName);
				final CodeValue preferLang = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_PREFER_LANG, prefLangId);
					
				final Long prefCommId = command.longValueOfParameterNamed(ClientApiConstants.preferredCommunicationParamName);
				final CodeValue preferCommunication = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_PREFER_COMMUNICATION, prefCommId);
					
				final Long ageGroupId = command.longValueOfParameterNamed(ClientApiConstants.ageGroupParamName);
				final CodeValue ageGroup = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_AGE_GROUP, ageGroupId);
				
				additionalfields.upadate(gender,nationality,customerIdentifier,preferLang,preferCommunication,ageGroup,command);
				this.clientAdditionalFieldsRepository.save(additionalfields);
				
			return new CommandProcessingResult(command.entityId());
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
	
	@Override
	public CommandProcessingResult updateBeesmartClient(JsonCommand command) {
		try{
			
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.ValidateBeesmartUpdateClient(command);
			Client client=this.clientRepository.findOneWithAccountId(command.stringValueOfParameterNamed("accountNo"));
			
			/*if(client.getStatus() == 300){
				SelfCare clientUser = this.selfCareRepository.findOneByClientId(client.getId());
				if(clientUser == null){
					throw new ClientNotFoundException(client.getId());
				}
				
				final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();
				 actualChanges.put("zebraSubscriberId", command.longValueOfParameterNamed("userId"));
				 
				clientUser.setZebraSubscriberId(command.longValueOfParameterNamed("userId"));
				this.selfCareRepository.save(clientUser);
				
				return new CommandProcessingResultBuilder().withClientId(clientUser.getClientId()).with(actualChanges).build();
			}else{
				throw new PlatformDataIntegrityException("error.msg.client.status.not.active", 
						"Client status is not in Active state", client.getId(),client.getStatus());
			}*/
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		return null;
		
	}
	
	@Override
	public CommandProcessingResult deleteBeesmartClient(JsonCommand command,Long entityId) {
		try{
			
			this.context.authenticatedUser();
			Client client=this.clientRepository.findOneWithNotFoundDetection(entityId);
			
			/*if(client.getStatus() == 300){
				SelfCare clientUser = this.selfCareRepository.findOneByClientId(client.getId());
				if(clientUser == null){
					throw new ClientNotFoundException(client.getId());
				}
				
				final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();
				 actualChanges.put("zebraSubscriberId", "null");
				 
				clientUser.setZebraSubscriberId(null);
				this.selfCareRepository.save(clientUser);
				
				return new CommandProcessingResultBuilder().withClientId(clientUser.getClientId()).with(actualChanges).build();
			}else{
				throw new PlatformDataIntegrityException("error.msg.client.status.not.active", 
						"Client status is not in Active state", client.getId(),client.getStatus());
			}*/
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		return null;
		
	}

}