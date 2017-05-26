package com.bdaum.zoom.cat.model.asset;

import java.util.*;

import com.bdaum.zoom.cat.model.MediaExtension_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset mediaExtension
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface MediaExtension extends IAsset, MediaExtension_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property asset_parent
	 *
	 * @param _value - new element value
	 */
	public void setAsset_parent(Asset _value);

	/**
	 * Get value of property asset_parent
	 *
	 * @return - value of field asset_parent
	 */
	public Asset getAsset_parent();

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
