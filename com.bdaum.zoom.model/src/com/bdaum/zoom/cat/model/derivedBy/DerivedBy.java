package com.bdaum.zoom.cat.model.derivedBy;

import com.bdaum.zoom.cat.model.DerivedBy_type;

import java.util.*;

import com.bdaum.zoom.cat.model.asset.Asset;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset derivedBy
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface DerivedBy extends DerivedBy_type, IAsset {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property derivative
	 *
	 * @param _value - new element value
	 */
	public void setDerivative(String _value);

	/**
	 * Get value of property derivative
	 *
	 * @return - value of field derivative
	 */
	public String getDerivative();

	/**
	 * Set value of property original
	 *
	 * @param _value - new element value
	 */
	public void setOriginal(String _value);

	/**
	 * Get value of property original
	 *
	 * @return - value of field original
	 */
	public String getOriginal();

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
