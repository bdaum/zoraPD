package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset lastDeviceImport
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface LastDeviceImport_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

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

	/**
	 * Set value of property timestamp
	 *
	 * @param _value - new element value
	 */
	public void setTimestamp(long _value);

	/**
	 * Get value of property timestamp
	 *
	 * @return - value of field timestamp
	 */
	public long getTimestamp();

	/**
	 * Set value of property description
	 *
	 * @param _value - new element value
	 */
	public void setDescription(String _value);

	/**
	 * Get value of property description
	 *
	 * @return - value of field description
	 */
	public String getDescription();

	/**
	 * Set value of property owner
	 *
	 * @param _value - new element value
	 */
	public void setOwner(String _value);

	/**
	 * Get value of property owner
	 *
	 * @return - value of field owner
	 */
	public String getOwner();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
