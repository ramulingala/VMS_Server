package org.mifosplatform.infrastructure.loginhistory.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.loginhistory.data.LoginHistoryData;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class LoginHistoryReadPlatformServiceImpl implements LoginHistoryReadPlatformService
{

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;


	@Autowired
	public LoginHistoryReadPlatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public LoginHistoryData retrieveSessionId(String id) {
		
		try{	
			//context.authenticatedUser();
			LoginMapper mapper = new LoginMapper();

			String sql = "select id from b_login_history where session_id=?";

			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { id });
		}catch(EmptyResultDataAccessException accessException){
			return null;
		}
		
	}
	
	private static final class LoginMapper implements RowMapper<LoginHistoryData> {

		@Override
		public LoginHistoryData mapRow(final ResultSet rs,@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			return new LoginHistoryData(id);

		}
	}

	@Override
	public int retrieveNumberOfUsers(String username) {
		try{
	String sql = "select count(*) from b_login_history where username=? and status='ACTIVE'";
		return this.jdbcTemplate.queryForObject(sql,Integer.class, new Object[]{username});
		}catch(EmptyResultDataAccessException accessException){
			return 0;
		}
	}


}

