package org.mifosplatform.organisation.partneragreement.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.partneragreement.data.AgreementData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PartnersAgreementReadPlatformServiceImp<AgreementDataMapper> implements PartnersAgreementReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public PartnersAgreementReadPlatformServiceImp(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	
	@Override
	public Long checkAgreement(Long officeId) {
		
		try {
			context.authenticatedUser();
			final String sql = "select id from m_office_agreement where office_id=? ";
			return jdbcTemplate.queryForLong(sql, new Object[] { officeId});
		} catch (final EmptyResultDataAccessException e) {
			return null;
		}

	}
	
	
	@Override
	public List<AgreementData> retrieveAgreementDetails(final Long agreementId) {
		try {
			context.authenticatedUser();
			final AgreementMapper mapper = new AgreementMapper();
			final String sql = "select " + mapper.schema() + " and a.id = ? " ;
			return this.jdbcTemplate.query(sql, mapper,new Object[] { agreementId });
		} catch (final EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class AgreementMapper implements RowMapper<AgreementData> {

		public String schema() {
			return " a.id as Id,a.agreement_status as agreementStatus,a.office_id as officeId, a.start_date as startDate,a.end_date as endDate,ad.id as detailId,ad.share_type as shareType, "
					+ "ad.share_amount as shareAmount,c.code_value as source from m_office_agreement a join m_office_agreement_detail ad ON a.id = ad.agreement_id left join "
					+ " m_code_value c ON c.id = ad.source where a.is_deleted='N' and ad.is_deleted='N' ";
		}

		@Override
		public AgreementData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("Id");
			final String agreementStatus = rs.getString("agreementStatus");
			final Long officeId = rs.getLong("officeId");
			final LocalDate startDate = JdbcSupport.getLocalDate(rs,"startDate");
			final LocalDate endDate = JdbcSupport.getLocalDate(rs,"endDate");
			final String shareType = rs.getString("shareType");
			final BigDecimal shareAmount =rs.getBigDecimal("shareAmount");
			final String source = rs.getString("source");
			final Long detailId = rs.getLong("detailId");
			//final EnumOptionData enumstatus=OrderStatusEnumaration.OrderStatusType(status.intValue());
			
			return new AgreementData(id,agreementStatus,officeId,startDate,endDate,shareType,shareAmount,source,detailId);

		}

	}

	@Override
	public AgreementData retrieveAgreementData(final Long partnerId) {
		try {
			context.authenticatedUser();
			final DataMapper mapper=new DataMapper();
			final String sql = "select " + mapper.schema();
			return this.jdbcTemplate.queryForObject(sql,mapper,new Object[] { partnerId });
		} catch (final EmptyResultDataAccessException accessException) {
			return null;	
	 }
   }
	
	private static final class DataMapper implements RowMapper<AgreementData> {

		public String schema() {
			return " a.id as Id,a.agreement_status as agreementStatus,a.office_id as officeId, a.start_date as startDate,a.end_date as endDate "
					+" from m_office_agreement a where a.is_deleted='N' and a.office_id= ?";
		}

		@Override
		public AgreementData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("Id");
			final String agreementStatus = rs.getString("agreementStatus");
			final Long officeId = rs.getLong("officeId");
			final LocalDate startDate = JdbcSupport.getLocalDate(rs,"startDate");
			final LocalDate endDate = JdbcSupport.getLocalDate(rs,"endDate");
			//final EnumOptionData enumstatus=OrderStatusEnumaration.OrderStatusType(status.intValue());
			
			return new AgreementData(id,agreementStatus,officeId,startDate,endDate);

		}

	}
	
}

