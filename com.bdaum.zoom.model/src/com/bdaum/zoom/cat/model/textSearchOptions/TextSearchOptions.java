package com.bdaum.zoom.cat.model.textSearchOptions;

import com.bdaum.zoom.cat.model.TextSearchOptions_type;

import java.util.*;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset textSearchOptions
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface TextSearchOptions extends IAsset, TextSearchOptions_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

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
