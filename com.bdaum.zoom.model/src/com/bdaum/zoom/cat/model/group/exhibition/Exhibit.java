package com.bdaum.zoom.cat.model.group.exhibition;

import java.util.*;

import com.bdaum.zoom.cat.model.Rgb_type;

import com.bdaum.zoom.cat.model.Exhibit_type;

import com.bdaum.zoom.cat.model.asset.Asset;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset exhibit
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Exhibit extends Exhibit_type, IAsset {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property wall_exhibit_parent
	 *
	 * @param _value - new element value
	 */
	public void setWall_exhibit_parent(String _value);

	/**
	 * Get value of property wall_exhibit_parent
	 *
	 * @return - value of field wall_exhibit_parent
	 */
	public String getWall_exhibit_parent();

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
