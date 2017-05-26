package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset ghost
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Ghost_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property name
	 *
	 * @param _value - new element value
	 */
	public void setName(String _value);

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName();

	/**
	 * Set value of property uri
	 *
	 * @param _value - new element value
	 */
	public void setUri(String _value);

	/**
	 * Get value of property uri
	 *
	 * @return - value of field uri
	 */
	public String getUri();

	/**
	 * Set value of property volume
	 *
	 * @param _value - new element value
	 */
	public void setVolume(String _value);

	/**
	 * Get value of property volume
	 *
	 * @return - value of field volume
	 */
	public String getVolume();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
