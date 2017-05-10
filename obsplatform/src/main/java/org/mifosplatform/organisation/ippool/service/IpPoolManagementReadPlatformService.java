package org.mifosplatform.organisation.ippool.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.organisation.ippool.data.IpPoolData;
import org.mifosplatform.organisation.ippool.data.IpPoolManagementData;
import org.mifosplatform.organisation.office.service.SearchSqlQuery;

/**
 * 
 * @author ashokreddy
 *
 */
public interface IpPoolManagementReadPlatformService {

	List<IpPoolData> getUnallocatedIpAddressDetailds();

	Long checkIpAddress(String ipaddress);

	Page<IpPoolManagementData> retrieveIpPoolData(SearchSqlQuery searchItemDetails, String type, String[] data);

	List<String> retrieveIpPoolIDArray(String query);

	IpPoolManagementData retrieveIpaddressData(String ipAddress);

	List<IpPoolManagementData> retrieveClientIpPoolDetails(Long clientId);

	IpPoolManagementData retrieveIdByIpAddress(String ip);

	List<IpPoolManagementData> retrieveSingleIpPoolDetails(Long poolId);


}
