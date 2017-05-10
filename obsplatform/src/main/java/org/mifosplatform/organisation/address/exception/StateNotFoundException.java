package org.mifosplatform.organisation.address.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class StateNotFoundException extends AbstractPlatformResourceNotFoundException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StateNotFoundException(final String id) {
        super("error.msg.state.not.found", "state with this id"+id+"not exist",id);
        
    }

}
