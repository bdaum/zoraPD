package com.bdaum.zoom.cat.model;

import java.util.*;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset migrationPolicy
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface MigrationPolicy extends IAsset, MigrationPolicy_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property rule
	 *
	 * @param _value - new element value
	 */
	public void setRule(MigrationRule[] _value);

	/**
	 * Set single element of array rule
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setRule(MigrationRule _element, int _i);

	/**
	 * Get value of property rule
	 *
	 * @return - value of field rule
	 */
	public MigrationRule[] getRule();

	/**
	 * Get single element of array rule
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array rule
	 */
	public MigrationRule getRule(int _i);

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
