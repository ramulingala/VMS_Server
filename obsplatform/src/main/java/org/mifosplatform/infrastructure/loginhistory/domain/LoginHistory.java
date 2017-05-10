package org.mifosplatform.infrastructure.loginhistory.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_login_history")
public class LoginHistory extends AbstractPersistable<Long> {

	
	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name= "username")
	private String userName;

	@Column(name = "session_id")
	private String sessionId;
	
	@Column(name = "login_time")
	private Date loginTime;
	
	@Column(name = "logout_time")
	private Date logoutTime;
	
	@Column(name = "session_lastupdate")
	private Date sessionLastupdate;
	
	@Column(name = "status")
	private String status;


	public LoginHistory() {
		// TODO Auto-generated constructor stub
			
	}
	
	public LoginHistory(String ipAddress, String deviceId,String sessionId,Date loginTime,Date logoutTime,String userName,String status) {
          
		  this.ipAddress = ipAddress;
		  this.deviceId = deviceId;
		  this.sessionId = sessionId;
		  this.loginTime = loginTime;
		  this.logoutTime = logoutTime;
		  this.status = status;
		  this.userName=userName;
	}
	
	public static LoginHistory fromJson(JsonCommand command) {
		 	final String ipAddress = command.stringValueOfParameterNamed("ipAddress");
		    final String deviceId = command.stringValueOfParameterNamed("deviceId");
		    final String sessionId = command.stringValueOfParameterNamed("sessionId");
		    final Date loginTime = command.DateValueOfParameterNamed("loginTime");
		   // final Date logoutTime= command.DateValueOfParameterNamed("logoutTime");
		    final String userName= command.stringValueOfParameterNamed("userName");
		    final String status=command.stringValueOfParameterNamed("status");
		    
		return new LoginHistory(ipAddress,deviceId,sessionId,loginTime,null,userName,status);
	}

	public Map<String, Object> update() {
		 	final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		  
	        final String logoutTimeParamName = "logoutTime";
	        this.logoutTime = DateUtils.getDateOfTenant();
	        Date d = DateUtils.getDateOfTenant();
	        actualChanges.put(logoutTimeParamName, d);
			/*if (command.isChangeInLocalDateParameterNamed(logoutTimeParamName,
					new LocalDate(this.logoutTime))) {
				final LocalDate newValue = command
						.localDateValueOfParameterNamed(logoutTimeParamName);
				actualChanges.put(logoutTimeParamName, newValue);
			}*/


		      final String statusParamName = "status";
		      this.status="INACTIVE";
		      actualChanges.put(statusParamName, "INACTIVE");
	        return actualChanges;

	}

	public void updateActiveTime() {
		 this.sessionLastupdate = DateUtils.getDateOfTenant();
	}
}

