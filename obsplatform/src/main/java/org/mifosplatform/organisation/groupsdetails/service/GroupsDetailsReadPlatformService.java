package org.mifosplatform.organisation.groupsdetails.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.organisation.groupsdetails.data.GroupsDetailsData;
import org.mifosplatform.organisation.office.service.SearchSqlQuery;

public interface GroupsDetailsReadPlatformService {

	Page<GroupsDetailsData> retrieveAllGroupsData(SearchSqlQuery searchGroupsDetails);

	List<Long> retrieveclientIdsByGroupId(Long groupId);

}
