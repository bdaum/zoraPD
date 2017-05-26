package com.bdaum.zoom.cat.model.group;

import java.util.*;

import com.bdaum.zoom.cat.model.PostProcessor_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset PostProcessor
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface PostProcessor extends IAsset, PostProcessor_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property smartCollection_parent
	 *
	 * @param _value - new element value
	 */
	public void setSmartCollection_parent(SmartCollection _value);

	/**
	 * Get value of property smartCollection_parent
	 *
	 * @return - value of field smartCollection_parent
	 */
	public SmartCollection getSmartCollection_parent();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException;

}
