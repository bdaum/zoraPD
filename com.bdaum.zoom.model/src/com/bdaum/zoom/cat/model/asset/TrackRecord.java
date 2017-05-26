package com.bdaum.zoom.cat.model.asset;

import java.util.*;

import com.bdaum.zoom.cat.model.TrackRecord_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset trackRecord
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface TrackRecord extends IAsset, TrackRecord_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property asset_track_parent
	 *
	 * @param _value - new element value
	 */
	public void setAsset_track_parent(String _value);

	/**
	 * Get value of property asset_track_parent
	 *
	 * @return - value of field asset_track_parent
	 */
	public String getAsset_track_parent();

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
