package org.mifosplatform.scheduledjobs.dataupload.service;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.scheduledjobs.dataupload.command.DataUploadCommand;
import org.mifosplatform.scheduledjobs.dataupload.data.MRNErrorData;
import org.mifosplatform.scheduledjobs.dataupload.domain.DataUpload;
import org.mifosplatform.scheduledjobs.dataupload.domain.DataUploadCommandValidator;
import org.mifosplatform.scheduledjobs.dataupload.domain.DataUploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


@Service
public class DataUploadWritePlatformServiceImp implements DataUploadWritePlatformService{

	private final PlatformSecurityContext context;
	private final DataUploadRepository uploadStatusRepository;
	private final DataUploadHelper dataUploadHelper;
	final private static String MEDIAASSETS_RESOURCE_TYPE = "ASSESTS";
	final private static String EPG_RESOURCE_TYPE = "EPGPROGRAMGUIDE";
	final private static String MRN_RESOURCE_TYPE = "MRNDETAILS";
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	 
	
	@Autowired
	public DataUploadWritePlatformServiceImp(final PlatformSecurityContext context,final DataUploadRepository uploadStatusRepository,
			final DataUploadHelper dataUploadHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		
		this.context=context;
		this.dataUploadHelper=dataUploadHelper;
		this.uploadStatusRepository=uploadStatusRepository;
		this.commandsSourceWritePlatformService=commandsSourceWritePlatformService;
	}
	

	@Override
	public CommandProcessingResult processDatauploadFile(final Long fileId) {
		
		this.context.authenticatedUser();
		BufferedReader csvFileBufferedReader = null;
		String line = null;
		try{
			final DataUpload uploadStatus = this.uploadStatusRepository.findOne(fileId);
			final String uploadProcess=uploadStatus.getUploadProcess();
			String jsonString=null;
			final String fileLocation = uploadStatus.getUploadFilePath();	
			ArrayList<MRNErrorData> errorData = new ArrayList<MRNErrorData>();
			final String splitLineRegX = ",";
			int i=1;
			Long processRecordCount=0L;
			Long totalRecordCount=0L;
			uploadStatus.setProcessStatus("Running...");
			this.uploadStatusRepository.save(uploadStatus);
			csvFileBufferedReader = new BufferedReader(new FileReader(fileLocation));
			line = csvFileBufferedReader.readLine();
	
	if(uploadProcess.equalsIgnoreCase("Hardware Items") && new File(fileLocation).getName().contains(".csv")){
		
		try{
			
		while((line = csvFileBufferedReader.readLine()) != null){
			try{
				final String[] currentLineData = line.split(splitLineRegX);
				if(currentLineData!=null && currentLineData[0].contains("EOF")){
					return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
				}
				jsonString=this.dataUploadHelper.buildJsonForHardwareItems(currentLineData,errorData,i);
				
				/*if(jsonString != null){ 
					final CommandWrapper commandRequest = new CommandWrapperBuilder().createInventoryItem(null).withJson(jsonString).build();
					final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
					if(result!=null){
						processRecordCount++;
							errorData.add(new MRNErrorData((long)i, "Success."));
					}
				}*/
			
			}catch (Exception e) {
				//handleDataIntegrityIssues(i,errorData,e);
			}
			totalRecordCount++;
			i++;
			
		}
		}catch(Exception exception){
			exception.printStackTrace();
		}
		finally{
			
			if(csvFileBufferedReader!=null){
				try{
					csvFileBufferedReader.close();
				}catch(Exception e){
					e.printStackTrace();
					
				}
			}
		}
	//	writeToFile(fileLocation,errorData);

   	}else if(uploadProcess.equalsIgnoreCase("Mrn") && new File(fileLocation).getName().contains(".csv")){
   		
   		while((line = csvFileBufferedReader.readLine()) != null){
   			try{
   				String[] currentLineData = line.split(splitLineRegX);
   				if(currentLineData!=null && currentLineData[0].equalsIgnoreCase("EOF")){
   					return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
   				}
							
   				jsonString=this.dataUploadHelper.buildJsonForMrn(currentLineData,errorData,i);
   				if(jsonString != null){
   					context.authenticatedUser().validateHasReadPermission(MRN_RESOURCE_TYPE);
   					/*final CommandWrapper commandRequest = new CommandWrapperBuilder().moveMRN().withJson(jsonString).build();
   					final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
   					if(result!=null){
   						processRecordCount++; 
   						errorData.add(new MRNErrorData((long)i, "Success."));
   					}*/
   				}
   				
   			}catch (Exception e) {
   				//errorData.add(new MRNErrorData((long)i, "Error: "+e.getMessage()));
   				//handleDataIntegrityIssues(i, errorData, e);
   			}
   			totalRecordCount++;
   			i++;
		}
		//writeToFile(fileLocation,errorData);

   	}else if(uploadProcess.equalsIgnoreCase("Move Itemsale") && new File(fileLocation).getName().contains(".csv")){
   		while((line = csvFileBufferedReader.readLine()) != null){
   			try{
   				final String[] currentLineData = line.split(splitLineRegX);
   				if(currentLineData!=null && currentLineData[0].equalsIgnoreCase("EOF")){
   					return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
   				}
   				jsonString=this.dataUploadHelper.buildJsonForMoveItems(currentLineData, errorData, i);
   				if(jsonString != null){
   					context.authenticatedUser().validateHasReadPermission(MRN_RESOURCE_TYPE);
   					/*final CommandWrapper commandRequest = new CommandWrapperBuilder().moveItemSale().withJson(jsonString).build();
   					final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
   					if(result!=null){
   						processRecordCount++; 
   						errorData.add(new MRNErrorData((long)i, "Success."));
   					}*/
   				}
   				
   			}catch (Exception e) {
   				//handleDataIntegrityIssues(i, errorData, e);
   			}
   			totalRecordCount++;
   			i++;
   		}
		//writeToFile(fileLocation,errorData);

   	}else if(uploadProcess.equalsIgnoreCase("Epg") && new File(fileLocation).getName().contains(".csv")){
   		while((line = csvFileBufferedReader.readLine()) != null){
   			try{
   				final String[] currentLineData = line.split(splitLineRegX);
   				if(currentLineData!=null && currentLineData[0].equalsIgnoreCase("EOF")){
   					return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
   				}
   				jsonString=this.dataUploadHelper.buildJsonForEpg(currentLineData, errorData, i);
   				if(jsonString !=null){
   					context.authenticatedUser().validateHasReadPermission(EPG_RESOURCE_TYPE);
   					final CommandWrapper commandRequest = new CommandWrapperBuilder().createEpgXsls(0L).withJson(jsonString).build();
					final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
					if(result!=null){
						processRecordCount++;
						errorData.add(new MRNErrorData((long)i, "Success."));
					}
   				}	
   				
			}catch (Exception e) {
				   //handleDataIntegrityIssues(i, errorData,e);
		   }
   			totalRecordCount++;
   			i++;
   		}

   	}else if(uploadProcess.equalsIgnoreCase("Adjustments") && new File(fileLocation).getName().contains(".csv")){
   		while((line = csvFileBufferedReader.readLine()) != null){
   			try{
   				line=line.replace(";"," ");
   				String[] currentLineData = line.split(splitLineRegX);
   				if(currentLineData!=null && currentLineData[0].equalsIgnoreCase("EOF")){
   					return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
   				}
   				jsonString=this.dataUploadHelper.buildJsonForAdjustment(currentLineData, errorData, i);
   				
   				if(jsonString != null){
   					/*final CommandWrapper commandRequest = new CommandWrapperBuilder().createAdjustment(Long.valueOf(currentLineData[0])).withJson(jsonString).build();
   					final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
   					if(result!=null){
   						processRecordCount++;
   						errorData.add(new MRNErrorData((long)i, "Success."));
   					}*/
   				}
   			
   			}catch (Exception e) {
   				//handleDataIntegrityIssues(i, errorData,e);
   			}
   			totalRecordCount++;
   			i++;
		   }
   		//writeToFile(fileLocation,errorData);
				
   	}else if(uploadProcess.equalsIgnoreCase("Payments") && new File(fileLocation).getName().contains(".csv")){
   		while((line = csvFileBufferedReader.readLine()) != null){
   			try{
   				line=line.replace(";"," ");
   				final String[] currentLineData = line.split(splitLineRegX);
   				if(currentLineData!=null && currentLineData[0].equalsIgnoreCase("EOF")){
   					return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
				  }	
   				jsonString=this.dataUploadHelper.buildjsonForPayments(currentLineData, errorData, i);
   				if(jsonString !=null){
   					//final CommandWrapper commandRequest = new CommandWrapperBuilder().createPayment(Long.valueOf(currentLineData[0])).withJson(jsonString).build();
				  //final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
				  /*if(result!=null){
					  processRecordCount++;
					  errorData.add(new MRNErrorData((long)i, "Success."));
				  }*/
   				}
   				
   			}catch (Exception e) {
   				//handleDataIntegrityIssues(i, errorData,e);
			}
   			totalRecordCount++;
   			i++;
   		}
	   	//writeToFile(fileLocation,errorData);
	   	
   }/*else if(uploadProcess.equalsIgnoreCase("MediaAssets")){
	   DataUpload uploadStatusForMediaAsset = this.uploadStatusRepository.findOne(fileId);
	   Workbook wb = null;
	   processRecordCount=0L;
	   totalRecordCount=0L;
	   try {
		   wb = WorkbookFactory.create(new File(fileLocation));
		   final Sheet mediaSheet = wb.getSheetAt(0);
		   Sheet mediaAttributeSheet = wb.getSheetAt(1);
		   Sheet mediaLocationSheet = wb.getSheetAt(2);
		   final int msNumberOfRows = mediaSheet.getPhysicalNumberOfRows();
		   System.out.println("Number of rows : "+msNumberOfRows);
		   for ( i = 1; i < msNumberOfRows; i++) {
			   Row mediaRow = mediaSheet.getRow(i);
			  final Row mediaAttributeRow = mediaAttributeSheet.getRow(i);
			   Row mediaLocationRow = mediaLocationSheet.getRow(i);
			   try {
				   if(mediaRow.getCell(0).getStringCellValue().equalsIgnoreCase("EOF")){
					   this.dataUploadHelper.updateFile(uploadStatusForMediaAsset, totalRecordCount, processRecordCount, errorData);
				   }
				   jsonString=this.dataUploadHelper.buildForMediaAsset(mediaRow,mediaAttributeRow,mediaLocationRow);
				  
				   context.authenticatedUser().validateHasReadPermission(MEDIAASSETS_RESOURCE_TYPE );
				   final CommandWrapper commandRequest = new CommandWrapperBuilder().createMediaAsset().withJson(jsonString).build();
				   final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
				   
				   if(result!=null){
					   //Long rsId = result.resourceId();
					   errorData.add(new MRNErrorData((long)i, "Success"));
					   processRecordCount++;
				   }
						}catch (Exception e) {
							handleDataIntegrityIssues(i, errorData,e);
					}	
				}	
		   totalRecordCount++;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidFormatException e) {
				e.getStackTrace();
			}
		}*/else if(uploadProcess.equalsIgnoreCase("ItemSale") && new File(fileLocation).getName().contains(".csv")){
			while((line = csvFileBufferedReader.readLine()) != null){
				try{
					line=line.replace(";"," ");
					String[] currentLineData = line.split(splitLineRegX);
					if(currentLineData!=null && currentLineData[0].equalsIgnoreCase("EOF")){
						return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
					}
					
					jsonString=this.dataUploadHelper.buildforitemSale(currentLineData, errorData, i);
					/*if(jsonString != null){
						final CommandWrapper commandRequest = new CommandWrapperBuilder().createItemSale().withJson(jsonString).build();
						final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
						if(result!=null){
							processRecordCount++;
							errorData.add(new MRNErrorData((long)i, "Success."));
						}
					}*/
					
				}
				catch(Exception e){
					//handleDataIntegrityIssues(i, errorData,e);
						}
				i++;	
				totalRecordCount++;
			}
			//writeToFile(fileLocation,errorData);
		}else if(uploadProcess.equalsIgnoreCase("Property Data")  && new File(fileLocation).getName().contains(".csv") ){
	   		while((line = csvFileBufferedReader.readLine()) != null){
	   			try{
	   				line=line.replace(";"," ");
	   				final String[] currentLineData = line.split(splitLineRegX);
	   				if(currentLineData!=null && currentLineData[0].equalsIgnoreCase("EOF")){
	   					return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
					  }	
	   				jsonString=this.dataUploadHelper.buildjsonForPropertyDefinition(currentLineData, errorData, i);
	   				
	   				if(jsonString !=null){
	   					
	   				  final CommandWrapper commandRequest = new CommandWrapperBuilder().createProperty().withJson(jsonString).build();
	   		          final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
					
					  if(result!=null){
						  processRecordCount++;
						  errorData.add(new MRNErrorData((long)i, "Success."));
					  }
	   				}
	   				
	   			}catch (Exception e) {
	   				//handleDataIntegrityIssues(i, errorData,e);
				}
	   			totalRecordCount++;
	   			i++;
	   		}
		   	//writeToFile(fileLocation,errorData);
		   	
	   }else if(uploadProcess.equalsIgnoreCase("Property Master")  && new File(fileLocation).getName().contains(".csv") ){
	   		while((line = csvFileBufferedReader.readLine()) != null){
	   			try{
	   				line=line.replace(";"," ");
	   				final String[] currentLineData = line.split(splitLineRegX);
	   				if(currentLineData!=null && currentLineData[0].equalsIgnoreCase("EOF")){
	   					return  this.dataUploadHelper.updateFile(uploadStatus,totalRecordCount,processRecordCount,errorData);
					  }	
	   				jsonString=this.dataUploadHelper.buildjsonForPropertyCodeMaster(currentLineData, errorData, i);
	   				
	   				if(jsonString !=null){
	   					
	   				  final CommandWrapper commandRequest = new CommandWrapperBuilder().createPropertyMaster().withJson(jsonString).build();
	   		          final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
					
					  if(result!=null){
						  processRecordCount++;
						  errorData.add(new MRNErrorData((long)i, "Success."));
					  }
	   				}
	   				
	   			}catch (Exception e) {
	   				//handleDataIntegrityIssues(i, errorData,e);
				}
	   			totalRecordCount++;
	   			i++;
	   		}
		   	//writeToFile(fileLocation,errorData);
		   	
	   }
	
		return new CommandProcessingResult(Long.valueOf(1));
	}catch (FileNotFoundException e) {
		throw new PlatformDataIntegrityException("file.not.found", "file.not.found", "file.not.found", "file.not.found");					
	}catch (Exception e) {
		//errorData.add(new MRNErrorData((long)i, "Error: "+e.getCause().getLocalizedMessage()));
		//return new CommandProcessingResult(Long.valueOf(-1));
	}
		finally{
		
		if(csvFileBufferedReader!=null){
			try{
				csvFileBufferedReader.close();
			}catch(Exception e){
				e.printStackTrace();
				
			}
		}
		
	}
		
		return new CommandProcessingResult(Long.valueOf(1));
	}	
	/*private void handleDataIntegrityIssues(final int i,final ArrayList<MRNErrorData> errorData,final Exception dve) {
		
		if(dve instanceof OrderQuantityExceedsException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((AbstractPlatformDomainRuleException) dve).getDefaultUserMessage()));
			
		}else if(dve instanceof NoGrnIdFoundException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((AbstractPlatformDomainRuleException) dve).getDefaultUserMessage()));
			
		}else if(dve instanceof ItemNotFoundException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((AbstractPlatformDomainRuleException) dve).getDefaultUserMessage()));
			
		}else if(dve instanceof PlatformApiDataValidationException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((PlatformApiDataValidationException) dve).getErrors().get(0).getParameterName()+" : "+((PlatformApiDataValidationException) dve).getErrors().get(0).getDefaultUserMessage()));
			
		}else if(dve instanceof NullPointerException){
			errorData.add(new MRNErrorData((long)i, "Error: value cannot be null"));
			
		}else if(dve instanceof IllegalStateException){
			errorData.add(new MRNErrorData((long)i,((PlatformApiDataValidationException) dve).getErrors().get(0).getDefaultUserMessage()));
			
		}else if(dve instanceof AdjustmentCodeNotFoundException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((AbstractPlatformDomainRuleException) dve).getDefaultUserMessage()));

		}else if(dve instanceof PlatformDataIntegrityException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((PlatformDataIntegrityException) dve).getParameterName()+" : "+((PlatformDataIntegrityException) dve).getDefaultUserMessage()));
			
		}else if(dve instanceof SerialNumberNotFoundException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((AbstractPlatformDomainRuleException) dve).getDefaultUserMessage()));
			
		}else if(dve instanceof SerialNumberAlreadyExistException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((AbstractPlatformDomainRuleException) dve).getDefaultUserMessage()));
			
		}else if(dve instanceof DataIntegrityViolationException){
			
			errorData.add(new MRNErrorData((long)i, "Error: "+((DataIntegrityViolationException) dve).getMessage()));
		
		}else if(dve instanceof ClientNotFoundException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((AbstractPlatformDomainRuleException) dve).getDefaultUserMessage()));
		
		}else if(dve instanceof PaymentCodeNotFoundException){
			errorData.add(new MRNErrorData((long)i, "Error: "+((AbstractPlatformDomainRuleException) dve).getDefaultUserMessage()));
		
		}else if(dve instanceof EOFException){
			errorData.add(new MRNErrorData((long)i, "Completed: End Of Record"));
			
		}else if(dve instanceof ItemNotFoundException){
			errorData.add(new MRNErrorData((long)i,"Invalid Item id"));
		
		}else if(dve instanceof InvalidMrnIdException){
			errorData.add(new MRNErrorData((long)i,"Invalid Mrn id"));
		
		}else if(dve instanceof UnsupportedParameterException){
			    errorData.add(new MRNErrorData((long)i,"Row Contains Improper data "));
		}else {
			errorData.add(new MRNErrorData((long)i,"Data insertion is failed"));
	   }
	}*/

	@Override
	public CommandProcessingResult addItem(DataUploadCommand command) {
		DataUpload uploadStatus;
	
		 try{
			 
			this.context.authenticatedUser();
			DataUploadCommandValidator validator = new DataUploadCommandValidator(command);
		    validator.validateForCreate();
        	String fileLocation=null;
			fileLocation = FileUtils.saveToFileSystem(command.getInputStream(), command.getFileUploadLocation(),command.getFileName());
			
			uploadStatus = DataUpload.create(command.getUploadProcess(), fileLocation, command.getProcessDate(),command.getProcessStatus(),
					command.getProcessRecords(), command.getErrorMessage(),command.getDescription(),command.getFileName());
			
			 this.uploadStatusRepository.save(uploadStatus);
			 return new CommandProcessingResult(uploadStatus.getId());
			 
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command,dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}catch (IOException e) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
		
		
	}

	private void handleCodeDataIntegrityIssues(final DataUploadCommand command, final DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("file_name_key")) {
            final String name = command.getFileName();
       
            throw new PlatformDataIntegrityException("error.msg.file.duplicate.name", "A file with name'"
                    + name + "'already exists", "displayName", name);
        }

//        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
	

	
}


