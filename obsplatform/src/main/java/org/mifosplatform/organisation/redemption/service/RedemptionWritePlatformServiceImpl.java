package org.mifosplatform.organisation.redemption.service;



import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.redemption.serialization.RedemptionCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.contract.domain.ContractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedemptionWritePlatformServiceImpl implements
		RedemptionWritePlatformService {

	private final static Logger LOGGER = LoggerFactory.getLogger(RedemptionWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;	
	private final FromJsonHelper fromJsonHelper;
	//private final VoucherDetailsRepository voucherDetailsRepository;
	private final ClientRepository clientRepository;
	//private final OrderWritePlatformService orderWritePlatformService;
	private final RedemptionReadPlatformService redemptionReadPlatformService;
	private final RedemptionCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	//private final OrderRepository orderRepository;
	//private final ChargeCodeRepository chargeCodeRepository;
	//private final JournalvoucherRepository journalvoucherRepository;
	//private final BillingOrderWritePlatformService billingOrderWritePlatformService;
	private final static String VALUE_PINTYPE = "VALUE";
	private final static String PRODUCE_PINTYPE = "PRODUCT";
	private final static int RECONNECT_ORDER_STATUS = 3;
	private final static int RENEWAL_ORDER_STATUS = 1;
	private final static String USED = "USED";
	//private final PriceRepository priceRepository;
	private final ContractRepository contractRepository;
	
	@Autowired
	public RedemptionWritePlatformServiceImpl(final PlatformSecurityContext context,/*final VoucherDetailsRepository voucherDetailsRepository,*/
		final ClientRepository clientRepository,final FromJsonHelper fromJsonHelper,
		/*final OrderWritePlatformService orderWritePlatformService,*/final RedemptionReadPlatformService redemptionReadPlatformService,/*final OrderRepository orderRepository,*/
		final RedemptionCommandFromApiJsonDeserializer apiJsonDeserializer,/*final JournalvoucherRepository journalvoucherRepository,*/
		final ContractRepository contractRepository) {
		
		this.context = context;
		this.fromJsonHelper = fromJsonHelper;
		//this.orderRepository=orderRepository;
		this.clientRepository = clientRepository;
		this.fromApiJsonDeserializer= apiJsonDeserializer;
		//this.orderWritePlatformService = orderWritePlatformService;
		//this.chargeCodeRepository=chargeCodeRepository;
		this.redemptionReadPlatformService=redemptionReadPlatformService;
		//this.billingOrderWritePlatformService=billingOrderWritePlatformService;
		//this.voucherDetailsRepository = voucherDetailsRepository;
		//this.journalvoucherRepository=journalvoucherRepository;
		//this.priceRepository = priceRepository;
		this.contractRepository = contractRepository;
		
	}
	
	/**
	 * Implementing createRedemption method
	 */
	@Transactional
	@Override
	public CommandProcessingResult createRedemption(final JsonCommand command) {
	
		try {
			
			 final String simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy").format(DateUtils.getDateOfTenant());
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final Long clientId = command.longValueOfParameterNamed("clientId");
			final String pinNum = command.stringValueOfParameterNamed("pinNumber");
			this.clientObjectRetrieveById(clientId);
            Long resourceId=Long.valueOf(0);
            CommandProcessingResult result =null;

			//final VoucherDetails voucherDetails = retrieveRandomDetailsByPinNo(pinNum);
			//final Voucher voucher = voucherDetails.getVoucher();
			//final String pinType = voucher.getPinType();
			//final String pinTypeValue =  voucher.getPinValue();
			//final Long priceId  =  voucher.getPriceId();
			BigDecimal pinValue = BigDecimal.ZERO;
			
			/*if(pinType.equalsIgnoreCase(VALUE_PINTYPE) && pinTypeValue != null){
				
				  pinValue = new BigDecimal(pinTypeValue);
				  this.billingOrderWritePlatformService.updateClientBalance(pinValue, clientId, true);
				  resourceId = clientId;
			}*/
			 
			/*if(pinType.equalsIgnoreCase(PRODUCE_PINTYPE) && pinTypeValue != null){
				 
				final Long planId = Long.parseLong(pinTypeValue);
				final List<Long> orderIds=this.redemptionReadPlatformService.retrieveOrdersData(clientId,planId);
				final Price price  =  this.priceRepository.findOne(priceId);
				Long contractId = (long) 0;
				
				if(price != null){
					final String contractPeriod = price.getContractPeriod();
					Contract contract = this.contractRepository.findOneByContractId(contractPeriod);
					contractId = contract.getId();
					pinValue =price.getPrice();
				}
				final JsonObject json = new JsonObject();
				
				if(orderIds.isEmpty() && (price != null)){
					
					ChargeCodeMaster  chargeCode = this.chargeCodeRepository.findOneByChargeCode(price.getChargeCode());
					json.addProperty("billAlign", false);json.addProperty("planCode", planId);
					json.addProperty("contractPeriod", contractId);
					json.addProperty("isNewplan", true);
					json.addProperty("paytermCode",chargeCode.getBillFrequencyCode());
					json.addProperty("locale", "en");
					json.addProperty("dateFormat","dd MMMM yyyy"); 
					json.addProperty("start_date", simpleDateFormat);
					final JsonCommand commd = new JsonCommand(null, json.toString(), json, fromJsonHelper, null,clientId, null, null, null, null, null, null, null, null, null,null);
					//result=this.orderWritePlatformService.createOrder(clientId, commd);
				    resourceId=result.resourceId();
				}else {
					 
					final Long orderId = orderIds.get(0);*/
					 
					//final Order order=this.orderRepository.findOne(orderId);
					 
						/*if(order.getStatus() == RECONNECT_ORDER_STATUS){					
							this.orderWritePlatformService.reconnectOrder(orderId);
						
						} else if(order.getStatus() == RENEWAL_ORDER_STATUS){
							json.addProperty("priceId", priceId);
							json.addProperty("renewalPeriod", contractId);
							json.addProperty("priceId",priceId);
							json.addProperty("description", "Order Renewal By Redemption");
							final JsonCommand commd = new JsonCommand(null, json.toString(), json, fromJsonHelper, null, clientId, null, null, clientId, null, null, null, null, null, null,null);
							result=this.orderWritePlatformService.renewalClientOrder(commd, orderId);
							resourceId=result.resourceId();
						}*/
				/*}*/

				//this.billingOrderWritePlatformService.updateClientVoucherBalance(pinValue.negate(), clientId, false);

		/*	}*/
			
			  /*JournalVoucher journalVoucher=new JournalVoucher(resourceId,new Date(),"Redemption",null,pinValue.doubleValue(),Long.valueOf(0));
				this.journalvoucherRepository.save(journalVoucher);
*/				
			/*JournalVoucher journalVoucher=new JournalVoucher(resourceId,DateUtils.getDateOfTenant(),"Redemption",pinValue.doubleValue(),null,clientId);
					this.journalvoucherRepository.save(journalVoucher);*/
			  
			/*voucherDetails.setClientId(clientId);
			voucherDetails.setStatus(USED);
			voucherDetails.setSaleDate(DateUtils.getDateOfTenant());
			
			this.voucherDetailsRepository.save(voucherDetails);*/
			 
			return new CommandProcessingResult(clientId);
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(dve);
	    	return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
	

	/*private VoucherDetails retrieveRandomDetailsByPinNo(String pinNumber) {

		final VoucherDetails voucherDetails = this.voucherDetailsRepository.findOneByPinNumber(pinNumber);
		
		if (voucherDetails == null) {
			throw new PinNumberNotFoundException(pinNumber);
			
		}else if (voucherDetails.getClientId()!=null || voucherDetails.getStatus().equalsIgnoreCase("USED")) {
			throw new PinNumberAlreadyUsedException(pinNumber);
		}
		return voucherDetails;
	}*/

	private Client clientObjectRetrieveById(final Long clientId) {
		
		final Client client = this.clientRepository.findOne(clientId);
		if (client== null) { throw new ClientNotFoundException(clientId); }
		return client;
	}

	private void handleCodeDataIntegrityIssues(final DataIntegrityViolationException dve) {
		 final Throwable realCause = dve.getMostSpecificCause();

	        LOGGER.error(dve.getMessage(), dve);
	        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource: " + realCause.getMessage());
		
	}
	
}
