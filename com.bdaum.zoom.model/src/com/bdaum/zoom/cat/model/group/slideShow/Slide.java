package com.bdaum.zoom.cat.model.group.slideShow;

import java.util.*;

import com.bdaum.zoom.cat.model.Slide_type;

import com.bdaum.zoom.cat.model.asset.Asset;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset slide
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Slide extends IAsset, Slide_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property slideShow_entry_parent
	 *
	 * @param _value - new element value
	 */
	public void setSlideShow_entry_parent(String _value);

	/**
	 * Get value of property slideShow_entry_parent
	 *
	 * @return - value of field slideShow_entry_parent
	 */
	public String getSlideShow_entry_parent();

	/**
	 * Set value of property asset
	 *
	 * @param _value - new element value
	 */
	public void setAsset(String _value);

	/**
	 * Get value of property asset
	 *
	 * @return - value of field asset
	 */
	public String getAsset();

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
