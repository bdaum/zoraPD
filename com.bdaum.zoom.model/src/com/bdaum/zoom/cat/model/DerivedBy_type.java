package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset derivedBy
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface DerivedBy_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

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
	 * Set value of property archivedRecipe
	 *
	 * @param _value - new element value
	 */
	public void setArchivedRecipe(byte[] _value);

	/**
	 * Set single element of array archivedRecipe
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setArchivedRecipe(byte _element, int _i);

	/**
	 * Get value of property archivedRecipe
	 *
	 * @return - value of field archivedRecipe
	 */
	public byte[] getArchivedRecipe();

	/**
	 * Get single element of array archivedRecipe
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array archivedRecipe
	 */
	public byte getArchivedRecipe(int _i);

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
