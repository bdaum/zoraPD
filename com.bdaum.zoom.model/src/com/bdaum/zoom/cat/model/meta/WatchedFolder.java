package com.bdaum.zoom.cat.model.meta;

import com.bdaum.zoom.cat.model.WatchedFolder_type;

import java.util.*;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset watchedFolder
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface WatchedFolder extends IAsset, WatchedFolder_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property meta_parent
	 *
	 * @param _value - new element value
	 */
	public void setMeta_parent(String _value);

	/**
	 * Get value of property meta_parent
	 *
	 * @return - value of field meta_parent
	 */
	public String getMeta_parent();

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
