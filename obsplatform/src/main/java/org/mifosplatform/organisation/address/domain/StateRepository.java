package org.mifosplatform.organisation.address.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StateRepository extends JpaRepository<State,Long>,JpaSpecificationExecutor<State>{

}
