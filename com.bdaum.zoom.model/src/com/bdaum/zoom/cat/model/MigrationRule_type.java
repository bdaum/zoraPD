package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset migrationRule
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface MigrationRule_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property sourcePattern
	 *
	 * @param _value - new element value
	 */
	public void setSourcePattern(String _value);

	/**
	 * Get value of property sourcePattern
	 *
	 * @return - value of field sourcePattern
	 */
	public String getSourcePattern();

	/**
	 * Set value of property targetPattern
	 *
	 * @param _value - new element value
	 */
	public void setTargetPattern(String _value);

	/**
	 * Get value of property targetPattern
	 *
	 * @return - value of field targetPattern
	 */
	public String getTargetPattern();

	/**
	 * Set value of property targetVolume
	 *
	 * @param _value - new element value
	 */
	public void setTargetVolume(String _value);

	/**
	 * Get value of property targetVolume
	 *
	 * @return - value of field targetVolume
	 */
	public String getTargetVolume();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
