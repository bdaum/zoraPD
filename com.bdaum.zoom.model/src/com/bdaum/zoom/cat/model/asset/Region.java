package com.bdaum.zoom.cat.model.asset;

import java.util.*;

import com.bdaum.zoom.cat.model.Region_type;

import com.bdaum.zoom.cat.model.group.SmartCollection;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset region
 *
 * StringID is the hexadecimal representation of 4 16-bit parts (x,y,width,height). Each part is called to the maximum unsigned binary 16-bit number.
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Region extends IAsset, Region_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property asset_person_parent
	 *
	 * @param _value - new element value
	 */
	public void setAsset_person_parent(String _value);

	/**
	 * Get value of property asset_person_parent
	 *
	 * @return - value of field asset_person_parent
	 */
	public String getAsset_person_parent();

	/**
	 * Set value of property album
	 *
	 * @param _value - new element value
	 */
	public void setAlbum(String _value);

	/**
	 * Get value of property album
	 *
	 * @return - value of field album
	 */
	public String getAlbum();

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
