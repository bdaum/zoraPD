package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset sortCriterion
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface SortCriterion_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property field
	 *
	 * @param _value - new element value
	 */
	public void setField(String _value);

	/**
	 * Get value of property field
	 *
	 * @return - value of field field
	 */
	public String getField();

	/**
	 * Set value of property subfield
	 *
	 * @param _value - new element value
	 */
	public void setSubfield(String _value);

	/**
	 * Get value of property subfield
	 *
	 * @return - value of field subfield
	 */
	public String getSubfield();

	/**
	 * Set value of property descending
	 *
	 * @param _value - new element value
	 */
	public void setDescending(boolean _value);

	/**
	 * Get value of property descending
	 *
	 * @return - value of field descending
	 */
	public boolean getDescending();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
