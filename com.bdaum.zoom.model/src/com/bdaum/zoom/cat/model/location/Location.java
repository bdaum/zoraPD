package com.bdaum.zoom.cat.model.location;

import com.bdaum.zoom.cat.model.locationShown.LocationShown;

import java.util.*;

import com.bdaum.zoom.cat.model.Location_type;

import com.bdaum.zoom.cat.model.locationCreated.LocationCreated;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset location
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Location extends IAsset, Location_type {
	
	// Modified manually ( removed backpointers and other overhead)


	/* ----- Validation ----- */

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException;

}
