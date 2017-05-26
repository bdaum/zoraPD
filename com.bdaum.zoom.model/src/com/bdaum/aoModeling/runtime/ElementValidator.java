package com.bdaum.aoModeling.runtime;

import java.io.Serializable;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 * 
 * This interface describes validators to be used for element validation.
 */
public interface ElementValidator extends Serializable {
	
	/**
	 * The validation method. Implementors must only throw
	 * non-declarable exceptions such as IllegalArgumentException.
	 * 
	 * @param o - the object to be validated.
	 */
	public void validate(Object o);

}
