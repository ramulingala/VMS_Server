package org.mifosplatform.organisation.address.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.organisation.address.data.AddressData;
import org.mifosplatform.organisation.address.data.AddressLocationDetails;
import org.mifosplatform.organisation.address.data.CityDetailsData;
import org.mifosplatform.organisation.address.data.CountryDetails;
import org.mifosplatform.organisation.office.service.SearchSqlQuery;

public interface AddressReadPlatformService {


	List<AddressData> retrieveSelectedAddressDetails(String selectedname);

	List<AddressData> retrieveAddressDetailsBy(Long clientId, String addressType);

	List<AddressData> retrieveAddressDetails();
	
	List<String> retrieveCountryDetails();

	List<String> retrieveStateDetails();

	List<String> retrieveCityDetails();

	List<AddressData> retrieveCityDetails(String selectedname);

	List<EnumOptionData> addressType();

	AddressData retrieveAdressBy(String cityName);

	List<CountryDetails> retrieveCountries();
	
	List<AddressData> retrieveClientAddressDetails(Long clientId);
	
	Page<AddressLocationDetails> retrieveAllAddressLocations(SearchSqlQuery searchAddresses);

	List<CityDetailsData> retrieveCitywithCodeDetails();

	List<CityDetailsData> retrieveAddressDetailsByCityName(String cityName);
	

}

