package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset composedTo
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface ComposedTo_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property type
	 *
	 * @param _value - new element value
	 */
	public void setType(String _value);

	/**
	 * Get value of property type
	 *
	 * @return - value of field type
	 */
	public String getType();

	/**
	 * Set value of property recipe
	 *
	 * @param _value - new element value
	 */
	public void setRecipe(String _value);

	/**
	 * Get value of property recipe
	 *
	 * @return - value of field recipe
	 */
	public String getRecipe();

	/**
	 * Set value of property parameterFile
	 *
	 * @param _value - new element value
	 */
	public void setParameterFile(String _value);

	/**
	 * Get value of property parameterFile
	 *
	 * @return - value of field parameterFile
	 */
	public String getParameterFile();

	/**
	 * Set value of property tool
	 *
	 * @param _value - new element value
	 */
	public void setTool(String _value);

	/**
	 * Get value of property tool
	 *
	 * @return - value of field tool
	 */
	public String getTool();

	/**
	 * Set value of property date
	 *
	 * @param _value - new element value
	 */
	public void setDate(Date _value);

	/**
	 * Get value of property date
	 *
	 * @return - value of field date
	 */
	public Date getDate();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
