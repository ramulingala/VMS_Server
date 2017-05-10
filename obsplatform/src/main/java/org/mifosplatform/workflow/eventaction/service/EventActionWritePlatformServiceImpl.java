package org.mifosplatform.workflow.eventaction.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.userchat.domain.UserChat;
import org.mifosplatform.crm.userchat.domain.UserChatRepository;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.exception.BillingMessageTemplateNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.mifosplatform.useradministration.service.AppUserReadPlatformService;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.data.EventActionProcedureData;
import org.mifosplatform.workflow.eventaction.data.OrderNotificationData;
import org.mifosplatform.workflow.eventaction.domain.EventAction;
import org.mifosplatform.workflow.eventaction.domain.EventActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
//import net.java.dev.obs.beesmart.AddExternalBeesmartMethod;

@Service
public class EventActionWritePlatformServiceImpl implements ActiondetailsWritePlatformService{
	
	
	
	//private final OrderRepository orderRepository;
	//private final TicketMasterRepository repository;
	private final ClientRepository clientRepository;
	//private final EventOrderRepository eventOrderRepository;
	//private final EventMasterRepository eventMasterRepository;
	private final EventActionRepository eventActionRepository;
	private final BillingMessageRepository messageDataRepository;
	private final AppUserReadPlatformService readPlatformService;
	//private final BillingOrderApiResourse billingOrderApiResourse;
	//private final ProcessRequestRepository processRequestRepository;
	private final BillingMessageTemplateRepository messageTemplateRepository;
	//private final TicketMasterReadPlatformService ticketMasterReadPlatformService ;
    private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;	
    private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
    //private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
    //private final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    //private final PaypalRecurringBillingRepository paypalRecurringBillingRepository;
    private final EventActionReadPlatformService eventActionReadPlatformService;
    private final ConfigurationRepository configurationRepository;
    private final UserChatRepository userChatRepository;
    
    private BillingMessageTemplate activationTemplates;
    private BillingMessageTemplate reConnectionTemplates;
    private BillingMessageTemplate disConnectionTemplates;
    private BillingMessageTemplate paymentTemplates;
    private BillingMessageTemplate changePlanTemplates;
    private BillingMessageTemplate orderTerminationTemplates;
    
    private BillingMessageTemplate smsActivationTemplates;
    private BillingMessageTemplate smsDisconnectionTemplates;
    private BillingMessageTemplate smsReConnectionTemplates;
    private BillingMessageTemplate smsPaymentTemplates;
    private BillingMessageTemplate smsChangePlanTemplates;
    private BillingMessageTemplate smsOrderTerminationTemplates;
    
    private BillingMessageTemplate notifyTechicalTeam;


	@Autowired
	public EventActionWritePlatformServiceImpl(final ActionDetailsReadPlatformService actionDetailsReadPlatformService,final EventActionRepository eventActionRepository,
			/*final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,*/final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
			/*final OrderRepository orderRepository,*//*final TicketMasterRepository repository,*/final BillingMessageRepository messageDataRepository,final ClientRepository clientRepository,
			final BillingMessageTemplateRepository messageTemplateRepository,/*final EventMasterRepository eventMasterRepository,*//*final EventOrderRepository eventOrderRepository,*/
			/*final TicketMasterReadPlatformService ticketMasterReadPlatformService,*/final AppUserReadPlatformService readPlatformService,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final EventActionReadPlatformService eventActionReadPlatformService,
			final ConfigurationRepository configurationRepository, final UserChatRepository userChatRepository)
	    {
		//this.repository=repository;
		//this.orderRepository=orderRepository;
		this.clientRepository=clientRepository;
		this.readPlatformService=readPlatformService;
		//this.eventOrderRepository=eventOrderRepository;
		this.eventActionRepository=eventActionRepository;
		//this.eventMasterRepository=eventMasterRepository;
		this.messageDataRepository=messageDataRepository;
		//this.billingOrderApiResourse=billingOrderApiResourse;
		//this.processRequestRepository=processRequestRepository;
		this.messageTemplateRepository=messageTemplateRepository;
		//this.ticketMasterReadPlatformService=ticketMasterReadPlatformService;
        this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
        this.contractPeriodReadPlatformService=contractPeriodReadPlatformService;
        //this.hardwareAssociationReadplatformService=hardwareAssociationReadplatformService;
        //this.paymentGatewayRecurringWritePlatformService = paymentGatewayRecurringWritePlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        //this.paypalRecurringBillingRepository = paypalRecurringBillingRepository;
        this.eventActionReadPlatformService = eventActionReadPlatformService;
        this.configurationRepository = configurationRepository;
        this.userChatRepository = userChatRepository;
	}
	
	
	
	@Override
	public String AddNewActions(List<ActionDetaislData> actionDetaislDatas,final Long clientId,final String resourceId,String ticketURL) {
    
  try{
    	
	if(actionDetaislDatas!=null){
	   EventAction eventAction=null;
	   String headerMessage = null, bodyMessage = null, footerMessage = null;
	   BillingMessage billingMessage = null;
	   OrderNotificationData orderData = null;
	   BillingMessageTemplate template = null;
			
	   	for(ActionDetaislData detailsData:actionDetaislDatas){
	   		
		      EventActionProcedureData actionProcedureData=this.actionDetailsReadPlatformService.checkCustomeValidationForEvents(clientId, detailsData.getEventName(),detailsData.getActionName(),resourceId);
			  JSONObject jsonObject=new JSONObject();
			  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
			  	if(actionProcedureData.isCheck()){
				   
				    List<SubscriptionData> subscriptionDatas=this.contractPeriodReadPlatformService.retrieveSubscriptionDatabyContractType("Month(s)",1);
				   
				    switch(detailsData.getActionName()){
				  
				    case EventActionConstants.ACTION_SEND_EMAIL :
				    	

				    	   
				          //TicketMasterData data = this.ticketMasterReadPlatformService.retrieveTicket(clientId,new Long(resourceId));
				         // TicketMaster ticketMaster=this.repository.findOne(new Long(resourceId));
				         // AppUserData user = this.readPlatformService.retrieveUser(new Long(data.getUserId()));
				          BillingMessageTemplate billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_TICKET_TEMPLATE);
				          if(billingMessageTemplate !=null){
				          String value=ticketURL+""+resourceId;
				          String removeUrl="<br/><b>URL : </b>"+"<a href="+value+">View Ticket</a>";
				         // removeUrl.replaceAll("(PARAMURL)", ticketURL+""+resourceId); 	
				        	if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CREATE_TICKET)){
				        	  	/*if(!user.getEmail().isEmpty()){
				        	  		billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
				        	  	    +ticketMaster.getDescription()+"\n"+removeUrl, "", user.getEmail(), user.getEmail(),
									 "Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
									 BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  		this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  	   if(actionProcedureData.getEmailId().isEmpty()){
				        	  		   
				        	  			throw new EmailNotFoundException(new Long(data.getUserId()));
				        	  		}else{
				        	  			
				        	  			billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
				        	  		    +ticketMaster.getDescription()+"\n"+removeUrl, "", actionProcedureData.getEmailId(), actionProcedureData.getEmailId(),
										"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
										BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  			this.messageDataRepository.save(billingMessage);
				        	  		}
				        	  	}*/
				        	
				        	}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_EDIT_TICKET)){
				        	  		
				        	    /*if(!user.getEmail().isEmpty()){
				        	    	
				        	  		billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
				        	        +ticketMaster.getDescription()+"<br/>"+"COMMENT: "+data.getLastComment()+"<br/>"+removeUrl, "", user.getEmail(), user.getEmail(),
									"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  		this.messageDataRepository.save(billingMessage);
				        	  	
				        	    }else{
				        	  		
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  			throw new EmailNotFoundException(new Long(data.getUserId()));	
					        	  	}else{
					        	  		billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
					        	  	     +ticketMaster.getDescription()+"<br/>"+"COMMENT: \t"+data.getLastComment()+"<br/>"+removeUrl, "", actionProcedureData.getEmailId(),
					        	  	     actionProcedureData.getEmailId(),"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
					        	  	     BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
						        	  		this.messageDataRepository.save(billingMessage);
					        	  	}
				        	  	}*/
				        	
				        	/*}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CLOSE_TICKET)){
				        		
				        	  	if(!user.getEmail().isEmpty()){
				        	  			billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
				        	  			+ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", user.getEmail(), user.getEmail(),
										"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
										BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  			this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  		throw new EmailNotFoundException(new Long(data.getUserId()));	
					        	  	}else{
					        	  		     billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
					        	  		    +ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", actionProcedureData.getEmailId(),
					        	  	         actionProcedureData.getEmailId(),"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
					        	  	       BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
						        	        this.messageDataRepository.save(billingMessage);
					        	  }
				        	  	}*/
				        	  }
				        	}else{
				        		  throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_TICKET_TEMPLATE);
				          }
				       break;
				       
				    case EventActionConstants.ACTION_ACTIVE : 
				    	
				          /*AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(actionProcedureData.getOrderId());
				          		if(associationData ==null){
				          			throw new HardwareDetailsNotFoundException(actionProcedureData.getOrderId().toString());
				          		}*/
				          		jsonObject.put("renewalPeriod",subscriptionDatas.get(0).getId());	
				          		jsonObject.put("description","Order Renewal By Scheduler");
				          		eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",EventActionConstants.ACTION_RENEWAL.toString(),"/orders/renewal", 
			        			 Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
				          		this.eventActionRepository.save(eventAction);
				         break; 		

				    case EventActionConstants.ACTION_NEW :

				    	jsonObject.put("billAlign","false");
				    	jsonObject.put("contractPeriod",subscriptionDatas.get(0).getId());
				    	jsonObject.put("dateFormat","dd MMMM yyyy");
                        jsonObject.put("locale","en");
                        jsonObject.put("paytermCode","Monthly");
                        jsonObject.put("planCode",actionProcedureData.getPlanId());
                        jsonObject.put("isNewplan","true");
                        jsonObject.put("start_date",dateFormat.format(DateUtils.getDateOfTenant()));
                        eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",actionProcedureData.getActionName(),"/orders/"+clientId, 
                        		Long.parseLong(resourceId), jsonObject.toString(),null,clientId);
			        	this.eventActionRepository.save(eventAction);
			        	   
				    	break;
				    	
				    case EventActionConstants.ACTION_DISCONNECT :

			        	   eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",EventActionConstants.ACTION_ACTIVE.toString(),"/orders/reconnect/"+clientId, 
			        	   Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
			        	   this.eventActionRepository.save(eventAction);

			        	   break; 
			        	   
			        	  
					default:
						break;
				    }
			  	}
				    
				    switch(detailsData.getActionName()){
				 
				    case EventActionConstants.ACTION_PROVISION_IT : 

				    	Client client=this.clientRepository.findOne(clientId);
			  	    	//EventOrder eventOrder=this.eventOrderRepository.findOne(Long.valueOf(resourceId));
			  	    	//EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
			  	    	//String response= AddExternalBeesmartMethod.addVodPackage(client.getOffice().getExternalId().toString(),client.getAccountNo(),
			  	    	//		eventMaster.getEventName());

			  	    	/*ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0), eventOrder.getClientId(),eventOrder.getId(),ProvisioningApiConstants.PROV_BEENIUS,
								ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,'Y','Y');*/
						//List<EventOrderdetials> eventDetails=eventOrder.getEventOrderdetials();
						//EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
						//JSONObject jsonObject=new JSONObject();
						jsonObject.put("officeUid",client.getOffice().getExternalId());
						jsonObject.put("subscriberUid",client.getAccountNo());
						//jsonObject.put("vodUid",eventMaster.getEventName());
								
							/*for(EventOrderdetials details:eventDetails){
								ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(details.getId(),details.getEventDetails().getId(),jsonObject.toString(),
										null,null,eventMaster.getEventStartDate(), eventMaster.getEventEndDate(),DateUtils.getDateOfTenant(),DateUtils.getDateOfTenant(),'N',
										ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,null);
								processRequest.add(processRequestDetails);
							}*/
						//this.processRequestRepository.save(processRequest);

						break;
						
				    case EventActionConstants.ACTION_SEND_PROVISION : 

				    	eventAction=new EventAction(DateUtils.getDateOfTenant(), "CLOSE", "Client",EventActionConstants.ACTION_SEND_PROVISION.toString(),"/processrequest/"+clientId, 
				    	Long.parseLong(resourceId),jsonObject.toString(),clientId,clientId);
				    	this.eventActionRepository.save(eventAction);
				  	
			        	break;
			        	
				    /*case EventActionConstants.ACTION_ACTIVE_LIVE_EVENT :
				    	 eventMaster=this.eventMasterRepository.findOne(Long.valueOf(resourceId));
				    	 
				    	 eventAction=new EventAction(eventMaster.getEventStartDate(),"Create","Live Event",EventActionConstants.ACTION_ACTIVE_LIVE_EVENT.toString(),
				    			 "/eventmaster",Long.parseLong(resourceId),jsonObject.toString(),Long.valueOf(0),Long.valueOf(0));
				    	 this.eventActionRepository.saveAndFlush(eventAction);
				    	 
				    	 eventAction=new EventAction(eventMaster.getEventEndDate(),"Disconnect","Live Event",EventActionConstants.ACTION_INACTIVE_LIVE_EVENT.toString(),
				    			 "/eventmaster",Long.parseLong(resourceId),jsonObject.toString(),Long.valueOf(0),Long.valueOf(0));
				    	 this.eventActionRepository.saveAndFlush(eventAction);
			      
				    	break;*/ 	
				    	
				    case EventActionConstants.ACTION_INVOICE : 
				    	//Order order=this.orderRepository.findOne(new Long(resourceId));
			        	  jsonObject.put("dateFormat","dd MMMM yyyy");
			        	  jsonObject.put("locale","en");
			        	  //jsonObject.put("systemDate",dateFormat.format(order.getStartDate()));
			        	  	if(detailsData.IsSynchronous().equalsIgnoreCase("N")){
			        	  		eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE",EventActionConstants.EVENT_ACTIVE_ORDER.toString(),
			        	  		EventActionConstants.ACTION_INVOICE.toString(),"/billingorder/"+clientId,Long.parseLong(resourceId),
			        	  		jsonObject.toString(),Long.parseLong(resourceId),clientId);
					        	this.eventActionRepository.save(eventAction);
			        	  	
			        	  	}else{
			        	  		
			        	  		jsonObject.put("dateFormat","dd MMMM yyyy");
			        	  		jsonObject.put("locale","en");
			        	  		//jsonObject.put("systemDate",dateFormat.format(order.getStartDate()));
			        	  		//this.billingOrderApiResourse.retrieveBillingProducts(order.getClientId(),jsonObject.toString());
			        	  	}
			        	  break;
			        	  
				    case EventActionConstants.ACTION_SEND_PAYMENT :
						
					  	eventAction=new EventAction(DateUtils.getDateOfTenant(), "SEND", "Payment Receipt",EventActionConstants.ACTION_SEND_PAYMENT.toString(),"/billmaster/payment/"+clientId+"/"+Long.parseLong(resourceId), 
						    	Long.parseLong(resourceId),jsonObject.toString(),Long.parseLong(resourceId),clientId);
					        	this.eventActionRepository.save(eventAction);
						break;
							      
				    case EventActionConstants.ACTION_TOPUP_INVOICE_MAIL : 
			        	  		eventAction=new EventAction(DateUtils.getDateOfTenant(), "SEND",EventActionConstants.EVENT_TOPUP_INVOICE_MAIL.toString(),
			        	  		EventActionConstants.ACTION_TOPUP_INVOICE_MAIL.toString(),"/billmaster/invoice/"+clientId+"/"+resourceId,Long.parseLong(resourceId),
			        	  		jsonObject.toString(),Long.parseLong(resourceId),clientId);
					        	this.eventActionRepository.save(eventAction);
			        	  break;
		
				    case EventActionConstants.ACTION_RECURRING_DISCONNECT : 
				    	
				    	JsonObject apiRequestBodyAsJson = new JsonObject();
				    	apiRequestBodyAsJson.addProperty("orderId", Long.parseLong(resourceId));
				    	apiRequestBodyAsJson.addProperty("recurringStatus", "SUSPEND");
				    	
				    	//final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(apiRequestBodyAsJson.toString()).build();
						//final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
					
						//Map<String,Object> resultMap = result.getChanges();
						
						JsonObject resultJson = new JsonObject();
						//resultJson.addProperty("result", resultMap.get("result").toString());
						//resultJson.addProperty("acknoledgement", resultMap.get("acknoledgement").toString());
						//resultJson.addProperty("error", resultMap.get("error").toString());
						
						
						EventAction eventAction1=new EventAction(DateUtils.getDateOfTenant(),"Recurring Disconnect","Recurring Disconnect",EventActionConstants.ACTION_RECURRING_DISCONNECT.toString(),
				    			 "/eventmaster",Long.parseLong(resourceId),resultJson.toString(),Long.valueOf(0),Long.valueOf(0));
						
						eventAction1.updateStatus('Y');
						this.eventActionRepository.saveAndFlush(eventAction1);
						
			        	break;
			        	
			        	
				    case EventActionConstants.ACTION_RECURRING_RECONNECTION : 
				    	
				    	JsonObject JsonString = new JsonObject();
				    	JsonString.addProperty("orderId", Long.parseLong(resourceId));
				    	JsonString.addProperty("recurringStatus", "REACTIVATE");
				    	
				    	//final CommandWrapper commandRequestForReconn = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(JsonString.toString()).build();
						//final CommandProcessingResult commandResult = this.commandsSourceWritePlatformService.logCommandSource(commandRequestForReconn);
					
						//Map<String,Object> resultMapObj = commandResult.getChanges();
						
						JsonObject resultJsonObj = new JsonObject();
						//resultJsonObj.addProperty("result", resultMapObj.get("result").toString());
						//resultJsonObj.addProperty("acknoledgement", resultMapObj.get("acknoledgement").toString());
						//resultJsonObj.addProperty("error", resultMapObj.get("error").toString());
						
						
						EventAction eventActionObj=new EventAction(DateUtils.getDateOfTenant(),"Recurring Reconnection","Recurring Reconnection",EventActionConstants.ACTION_RECURRING_RECONNECTION.toString(),
				    			 "/eventmaster",Long.parseLong(resourceId),resultJsonObj.toString(),Long.valueOf(0),Long.valueOf(0));
						
						eventActionObj.updateStatus('Y');
						this.eventActionRepository.saveAndFlush(eventActionObj);
				  	
			        	break;
			        	
				    case EventActionConstants.ACTION_RECURRING_TERMINATION : 
				    	
				    	Long orderId = Long.parseLong(resourceId);
				    	
				    	//PaypalRecurringBilling billing = this.paypalRecurringBillingRepository.findOneByOrderId(orderId);
				    	
				    	/*if(billing.getDeleted() == 'N'){
				    		JsonObject terminationObj = new JsonObject();
					    	terminationObj.addProperty("orderId", orderId);
					    	terminationObj.addProperty("recurringStatus", "CANCEL");
					    	
					    	final CommandWrapper terminateCommandRequest = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(terminationObj.toString()).build();
							final CommandProcessingResult terminateResult = this.commandsSourceWritePlatformService.logCommandSource(terminateCommandRequest);
						
							Map<String,Object> resultMapForTerminate = terminateResult.getChanges();
							
							JsonObject resultJsonObject = new JsonObject();
							resultJsonObject.addProperty("result", resultMapForTerminate.get("result").toString());
							resultJsonObject.addProperty("acknoledgement", resultMapForTerminate.get("acknoledgement").toString());
							resultJsonObject.addProperty("error", resultMapForTerminate.get("error").toString());
							
							
							EventAction eventActionTermination=new EventAction(DateUtils.getDateOfTenant(),"Cancel Recurring","Cancel Recurring Profile",EventActionConstants.ACTION_RECURRING_TERMINATION.toString(),
					    			 "/eventmaster",Long.parseLong(resourceId),resultJsonObject.toString(),Long.valueOf(0),Long.valueOf(0));
							
							eventActionTermination.updateStatus('Y');
							this.eventActionRepository.saveAndFlush(eventActionTermination);
				    	}*/	
						
			        	break;
			        	

				    case EventActionConstants.ACTION_NOTIFY_TECHNICALTEAM : 
				    	
				    	String userName= "billing";
				    	
				    	Configuration configValue = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_APPUSER);
				    	
				    	if(null != configValue && configValue.isEnabled() && configValue.getValue() != null && !configValue.getValue().isEmpty()) {
				    		userName = configValue.getValue();
				    	}
				    	
				    	String data = userName.replace("{", "").replace("}", "").trim();
				    		
				    	String[] valArray = data.split(",");
				    	
				    	template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_TECHNICAL_TEAM);
				    	
				    	bodyMessage = template.getBody().replaceAll("<ActionType>", ticketURL);
				    	bodyMessage = bodyMessage.replaceAll("<clientId>", clientId.toString());
				    	bodyMessage = bodyMessage.replaceAll("<id>", resourceId == null ? "":resourceId);
				    	
						final LocalDate messageDate = DateUtils.getLocalDateOfTenant();
						
						for (String val : valArray) {
							UserChat userChat=new UserChat(val, messageDate.toDate(), bodyMessage, ConfigurationConstants.OBSUSER);
							this.userChatRepository.save(userChat);
						}
						
				    	
				    	break;

				    case EventActionConstants.ACTION_NOTIFY_ACTIVATION : 
				    	
				    	orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
				    	
				    	template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION);
				    	
				    	headerMessage = template.getHeader().replaceAll("<CustomerName>", orderData.getFirstName() + " " + orderData.getLastName());
				    	bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
				    	bodyMessage = bodyMessage.replaceAll("<Activation Date>", dateFormat.format(orderData.getActivationDate().toDate()));
				    	
				    	footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
				    	footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
				    	footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
				    	
				    	billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, 
				    			orderData.getOfficeEmail(), orderData.getEmailId(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(billingMessage);
			        	  		
				    	break;
				    
				    case EventActionConstants.ACTION_NOTIFY_DISCONNECTION : 
				    	
				    	orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
				    	
				    	template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION);
				    	
				    	headerMessage = template.getHeader().replaceAll("<CustomerName>", orderData.getFirstName() + " " + orderData.getLastName());
				    	bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
				    	bodyMessage = bodyMessage.replaceAll("<Disconnection Date>", dateFormat.format(orderData.getEndDate().toDate()));
				    	
				    	footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
				    	footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
				    	footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
				    	
				    	billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, 
				    			orderData.getOfficeEmail(), orderData.getEmailId(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(billingMessage);
				    	
				    	break;
				    	
				    case EventActionConstants.ACTION_NOTIFY_RECONNECTION : 
				    	
				    	orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
				    	
				    	template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION);
				    	
				    	headerMessage = template.getHeader().replaceAll("<CustomerName>", orderData.getFirstName() + " " + orderData.getLastName());
				    	bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
				    	bodyMessage = bodyMessage.replaceAll("<Reconnection Date>", dateFormat.format(orderData.getStartDate().toDate()));
				    	
				    	footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
				    	footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
				    	footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
				    	
				    	billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, 
				    			orderData.getOfficeEmail(), orderData.getEmailId(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(billingMessage);
				    	
				    	break;
				    	
				    case EventActionConstants.ACTION_NOTIFY_PAYMENT : 
				    	
				    	orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);
				    
				    	template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT);
				    	
				    	headerMessage = template.getHeader().replaceAll("<CustomerName>", orderData.getFirstName() + " " + orderData.getLastName());
				    	bodyMessage = template.getBody().replaceAll("<Amount>", resourceId);
				    	bodyMessage = bodyMessage.replaceAll("<Payment Date>", dateFormat.format(new Date()));
				    	
				    	footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
				    	footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
				    	footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
				    	
				    	billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, 
				    			orderData.getOfficeEmail(), orderData.getEmailId(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(billingMessage);
				    	
				    	break;
				    	
				    case EventActionConstants.ACTION_NOTIFY_CHANGEPLAN : 
				    	
				    	orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
				    	
				    	template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_CHANGEPLAN);
				    	
				    	headerMessage = template.getHeader().replaceAll("<CustomerName>", orderData.getFirstName() + " " + orderData.getLastName());
				    	bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
				    	bodyMessage = bodyMessage.replaceAll("<Activation Date>", dateFormat.format(orderData.getActivationDate().toDate()));
				    	
				    	footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
				    	footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
				    	footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
				    	
				    	billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, 
				    			orderData.getOfficeEmail(), orderData.getEmailId(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(billingMessage);
				    	
				    	break;
				    	
				    case EventActionConstants.ACTION_NOTIFY_ORDER_TERMINATE : 
				    	
				    	orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
				    	
				    	template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ORDERTERMINATION);
				    	
				    	headerMessage = template.getHeader().replaceAll("<CustomerName>", orderData.getFirstName() + " " + orderData.getLastName());
				    	bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
				    	bodyMessage = bodyMessage.replaceAll("<Disconnection Date>", dateFormat.format(new Date()));
				    	
				    	footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
				    	footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
				    	footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
				    	
				    	billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, 
				    			orderData.getOfficeEmail(), orderData.getEmailId(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(billingMessage);
				    	
				    	break;
				    	
				    default:			
						break;
				    }
				    
					Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_SMS);
					
					if(null != configuration && configuration.isEnabled()) {
						
						 switch(detailsData.getActionName()){
						 
						 case EventActionConstants.ACTION_NOTIFY_SMS_ACTIVATION : 
						    	
								orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
						    	
								template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ACTIVATION);
						    	
						    	bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
						    	bodyMessage = bodyMessage.replaceAll("<Activation Date>", dateFormat.format(orderData.getActivationDate().toDate()));
						    	
						    	billingMessage = new BillingMessage(null, bodyMessage, null, 
						    			orderData.getOfficeEmail(), orderData.getClientPhone(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
						    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null );
						    		
						    	this.messageDataRepository.save(billingMessage);
					        	  		
						    	break;
						    
							case EventActionConstants.ACTION_NOTIFY_SMS_DISCONNECTION : 
						    	
								orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
						    	
								template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_DISCONNECTION);
						    	
						    	bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
						    	bodyMessage = bodyMessage.replaceAll("<Disconnection Date>", dateFormat.format(orderData.getEndDate().toDate()));
						 
						    	billingMessage = new BillingMessage(null, bodyMessage, null, 
						    			orderData.getOfficeEmail(), orderData.getClientPhone(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
						    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null );
						    		
						    	this.messageDataRepository.save(billingMessage);
						    	
						    	break;
						    
							case EventActionConstants.ACTION_NOTIFY_SMS_RECONNECTION : 
						    	
								orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
						    	
								template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_RECONNECTION);
						    	
						    	bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
						    	bodyMessage = bodyMessage.replaceAll("<Reconnection Date>", dateFormat.format(orderData.getStartDate().toDate()));
						    	
						    	billingMessage = new BillingMessage(null, bodyMessage, null, 
						    			orderData.getOfficeEmail(), orderData.getClientPhone(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
						    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null );
						    		
						    	this.messageDataRepository.save(billingMessage);
						    	
						    	break;
						    
							case EventActionConstants.ACTION_NOTIFY_SMS_PAYMENT : 
						    	
								orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);
						    
								template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT);
						    	
						    	bodyMessage = template.getBody().replaceAll("<Amount>", resourceId);
						    	bodyMessage = bodyMessage.replaceAll("<Payment Date>", dateFormat.format(new Date()));
						    	
						    	billingMessage = new BillingMessage(null, bodyMessage, null, 
						    			orderData.getOfficeEmail(), orderData.getClientPhone(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
						    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null );
						    		
						    	this.messageDataRepository.save(billingMessage);
						    	
						    	break;
						    	
							case EventActionConstants.ACTION_NOTIFY_SMS_CHANGEPLAN :
						    	
								orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);
						    
								template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_CHANGEPLAN);
						    	
								bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
								bodyMessage = bodyMessage.replaceAll("<Activation Date>", dateFormat.format(orderData.getActivationDate().toDate()));
						    	
						    	billingMessage = new BillingMessage(null, bodyMessage, null, 
						    			orderData.getOfficeEmail(), orderData.getClientPhone(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
						    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null );
						    		
						    	this.messageDataRepository.save(billingMessage);
						    	
						    	break;
						    	
							case EventActionConstants.ACTION_NOTIFY_SMS_ORDER_TERMINATE :
						    	
								orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);
						    
								template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ORDERTERMINATION);
						    	
								bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
								bodyMessage = bodyMessage.replaceAll("<Disconnection Date>", dateFormat.format(new Date()));
						    	
						    	billingMessage = new BillingMessage(null, bodyMessage, null, 
						    			orderData.getOfficeEmail(), orderData.getClientPhone(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
						    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null );
						    		
						    	this.messageDataRepository.save(billingMessage);
						    	
						    	break;
						    	
						    default:
								break;
						    }
						 
					}
						
					
				   	 	
				      /* if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_SEND_EMAIL)){
				    	   
				          TicketMasterData data = this.ticketMasterReadPlatformService.retrieveTicket(clientId,new Long(resourceId));
				          TicketMaster ticketMaster=this.repository.findOne(new Long(resourceId));
				          AppUserData user = this.readPlatformService.retrieveUser(new Long(data.getUserId()));
				          BillingMessageTemplate billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_TICKET_TEMPLATE);
				          String value=ticketURL+""+resourceId;
				          String removeUrl="<br/><b>URL : </b>"+"<a href="+value+">View Ticket</a>";
				         // removeUrl.replaceAll("(PARAMURL)", ticketURL+""+resourceId); 	
				        	if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CREATE_TICKET)){
				        	  	if(!user.getEmail().isEmpty()){
				        	  		BillingMessage billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
				        	  	    +ticketMaster.getDescription()+"\n"+removeUrl, "", user.getEmail(), user.getEmail(),
									 "Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
									 BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  		this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  	   if(actionProcedureData.getEmailId().isEmpty()){
				        	  		   
				        	  			throw new EmailNotFoundException(new Long(data.getUserId()));
				        	  		}else{
				        	  			BillingMessage billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
				        	  		    +ticketMaster.getDescription()+"\n"+removeUrl, "", actionProcedureData.getEmailId(), actionProcedureData.getEmailId(),
										"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
										BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  			this.messageDataRepository.save(billingMessage);
				        	  		}
				        	  	}
				        	
				        	}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_EDIT_TICKET)){
				        	  		
				        	    if(!user.getEmail().isEmpty()){
				        	  		BillingMessage billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
				        	        +ticketMaster.getDescription()+"<br/>"+"COMMENT: "+data.getLastComment()+"<br/>"+removeUrl, "", user.getEmail(), user.getEmail(),
									"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  		this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  			throw new EmailNotFoundException(new Long(data.getUserId()));	
					        	  	}else{
					        	  		BillingMessage billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
					        	  	     +ticketMaster.getDescription()+"<br/>"+"COMMENT: \t"+data.getLastComment()+"<br/>"+removeUrl, "", actionProcedureData.getEmailId(),
					        	  	     actionProcedureData.getEmailId(),"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
					        	  	     BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
						        	  		this.messageDataRepository.save(billingMessage);
					        	  	}
				        	  	}
				        	
				        	}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CLOSE_TICKET)){
				        	  	if(!user.getEmail().isEmpty()){
				        	  			BillingMessage billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
				        	  			+ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", user.getEmail(), user.getEmail(),
										"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
										BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  			this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  		throw new EmailNotFoundException(new Long(data.getUserId()));	
					        	  	}else{
					        	  		     BillingMessage billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
					        	  		    +ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", actionProcedureData.getEmailId(),
					        	  	         actionProcedureData.getEmailId(),"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
					        	  	       BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
						        	        this.messageDataRepository.save(billingMessage);
					        	  }
				        	  	}
				        	  }
				      
				       }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_ACTIVE)){
				    	   
					          AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(actionProcedureData.getOrderId());
					          		if(associationData ==null){
					          			throw new HardwareDetailsNotFoundException(actionProcedureData.getOrderId().toString());
					          		}
					          		jsonObject.put("renewalPeriod",subscriptionDatas.get(0).getId());	
					          		jsonObject.put("description","Order Renewal By Scheduler");
					          		eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",EventActionConstants.ACTION_RENEWAL.toString(),"/orders/renewal", 
				        			 Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
					          		this.eventActionRepository.save(eventAction);
				         
				       }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_NEW)){
				        	  
				        	   jsonObject.put("billAlign","false");
				        	   jsonObject.put("contractPeriod",subscriptionDatas.get(0).getId());
				        	   jsonObject.put("dateFormat","dd MMMM yyyy");
                               jsonObject.put("locale","en");
				        	   jsonObject.put("paytermCode","Monthly");
				        	   jsonObject.put("planCode",actionProcedureData.getPlanId());
				        	   jsonObject.put("isNewplan","true");
				        	   jsonObject.put("start_date",dateFormat.format(new Date()));
				        	   eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",actionProcedureData.getActionName(),"/orders/"+clientId, 
					        			 Long.parseLong(resourceId), jsonObject.toString(),null,clientId);
				        	   this.eventActionRepository.save(eventAction);
				        	   
				      }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_DISCONNECT)){
				        	   
				        	   eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",EventActionConstants.ACTION_ACTIVE.toString(),"/orders/reconnect/"+clientId, 
				        	   Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
				        	   this.eventActionRepository.save(eventAction);
				        	   	
				      }else if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_INVOICE)){
				    	  
				        	  jsonObject.put("dateFormat","dd MMMM yyyy");
                              jsonObject.put("locale","en");
				        	  jsonObject.put("systemDate",dateFormat.format(new Date()));
				        	  	
				        	  if(detailsData.IsSynchronous().equalsIgnoreCase("N")){
				        		  
				        	  		eventAction=new EventAction(new Date(), "CREATE",EventActionConstants.EVENT_ACTIVE_ORDER.toString(),
				        	  		EventActionConstants.ACTION_INVOICE.toString(),"/billingorder/"+clientId,Long.parseLong(resourceId),
				        	  		jsonObject.toString(),Long.parseLong(resourceId),clientId);
						        	this.eventActionRepository.save(eventAction);
				        	  	
				        	  	}else{
				            	 
				        	  		Order order=this.orderRepository.findOne(new Long(resourceId));
				        	  		jsonObject.put("dateFormat","dd MMMM yyyy");
				        	  		jsonObject.put("locale","en");
				        	  		jsonObject.put("systemDate",dateFormat.format(order.getStartDate()));
				        	  		this.billingOrderApiResourse.retrieveBillingProducts(order.getClientId(),jsonObject.toString());
				        	  	}
				     
				      }else if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_PROVISION_IT)){
			  	    	
			  	    	Client client=this.clientRepository.findOne(clientId);
			  	    	EventOrder eventOrder=this.eventOrderRepository.findOne(Long.valueOf(resourceId));
			  	    	EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
			  	    	
			  	    	String response= AddExternalBeesmartMethod.addVodPackage(client.getOffice().getExternalId().toString(),client.getAccountNo(),
			  	    			eventMaster.getEventName());
			  	   
						ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0), eventOrder.getClientId(),eventOrder.getId(),ProvisioningApiConstants.PROV_BEENIUS,
								ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,'Y','Y');
						List<EventOrderdetials> eventDetails=eventOrder.getEventOrderdetials();
						//EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
						//JSONObject jsonObject=new JSONObject();
						jsonObject.put("officeUid",client.getOffice().getExternalId());
						jsonObject.put("subscriberUid",client.getAccountNo());
						jsonObject.put("vodUid",eventMaster.getEventName());
								
							for(EventOrderdetials details:eventDetails){
								ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(details.getId(),details.getEventDetails().getId(),jsonObject.toString(),
										response,null,eventMaster.getEventStartDate(), eventMaster.getEventEndDate(),new Date(),new Date(),'N',
										ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,null);
								processRequest.add(processRequestDetails);
							}
						this.processRequestRepository.save(processRequest);
					
			  	    	
				      }
			  	    }if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_SEND_PROVISION)){
		        	   
		        	   eventAction=new EventAction(new Date(), "CLOSE", "Client",EventActionConstants.ACTION_SEND_PROVISION.toString(),"/processrequest/"+clientId, 
		        	   Long.parseLong(resourceId),jsonObject.toString(),clientId,clientId);
		        	   this.eventActionRepository.save(eventAction);
			  	}*/
			
		
	   	}
	}
	     return null;
    }catch(Exception exception){
    	exception.printStackTrace();
    	return null;
    }

	}
	
private BillingMessageTemplate getTemplate(String templateName){
		
		if(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION.equalsIgnoreCase(templateName)){
			
			if(null == activationTemplates){
				activationTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION);
			}
			return activationTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION.equalsIgnoreCase(templateName)) {
			
			if(null == disConnectionTemplates){
				disConnectionTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION);
			}
			return disConnectionTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION.equalsIgnoreCase(templateName)) {
			
			if(null == reConnectionTemplates){
				reConnectionTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION);
			}
			return reConnectionTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT.equalsIgnoreCase(templateName)) {
			
			if(null == paymentTemplates){
				paymentTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT);
			}
			return paymentTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_CHANGEPLAN.equalsIgnoreCase(templateName)) {
			
			if(null == changePlanTemplates){
				changePlanTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_CHANGEPLAN);
			}
			return changePlanTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ORDERTERMINATION.equalsIgnoreCase(templateName)) {
			
			if(null == orderTerminationTemplates){
				orderTerminationTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ORDERTERMINATION);
			}
			return orderTerminationTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ACTIVATION.equalsIgnoreCase(templateName)) {
			
			if(null == smsActivationTemplates){
				smsActivationTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ACTIVATION);
			}
			return smsActivationTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_DISCONNECTION.equalsIgnoreCase(templateName)) {
			
			if(null == smsDisconnectionTemplates){
				smsDisconnectionTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_DISCONNECTION);
			}
			return smsDisconnectionTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_RECONNECTION.equalsIgnoreCase(templateName)) {
			
			if(null == smsReConnectionTemplates){
				smsReConnectionTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_RECONNECTION);
			}
			return smsReConnectionTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_PAYMENT.equalsIgnoreCase(templateName)) {
			
			if(null == smsPaymentTemplates){
				smsPaymentTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_PAYMENT);
			}
			return smsPaymentTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_CHANGEPLAN.equalsIgnoreCase(templateName)) {
			
			if(null == smsChangePlanTemplates){
				smsChangePlanTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_CHANGEPLAN);
			}
			return smsChangePlanTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ORDERTERMINATION.equalsIgnoreCase(templateName)) {
			
			if(null == smsOrderTerminationTemplates){
				smsOrderTerminationTemplates = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ORDERTERMINATION);
			}
			return smsOrderTerminationTemplates;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_TECHNICAL_TEAM.equalsIgnoreCase(templateName)) {
			
			if(null == notifyTechicalTeam){
				notifyTechicalTeam = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_TECHNICAL_TEAM);
			}
			return notifyTechicalTeam;
			
		} else {
			throw new BillingMessageTemplateNotFoundException(templateName);
		}
		
	}
}
