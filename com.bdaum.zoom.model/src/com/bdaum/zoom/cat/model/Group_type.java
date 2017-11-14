package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset group
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Group_type extends AomValueChangedNotifier,
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
	 * Set value of property system
	 *
	 * @param _value - new element value
	 */
	public void setSystem(boolean _value);

	/**
	 * Get value of property system
	 *
	 * @return - value of field system
	 */
	public boolean getSystem();

	/**
	 * Set value of property showLabel
	 *
	 * @param _value - new element value
	 */
	public void setShowLabel(int _value);

	/**
	 * Get value of property showLabel
	 *
	 * @return - value of field showLabel
	 */
	public int getShowLabel();

	/**
	 * Set value of property labelTemplate
	 *
	 * @param _value - new element value
	 */
	public void setLabelTemplate(String _value);

	/**
	 * Get value of property labelTemplate
	 *
	 * @return - value of field labelTemplate
	 */
	public String getLabelTemplate();

	/**
	 * Set value of property fontSize
	 *
	 * @param _value - new element value
	 */
	public void setFontSize(int _value);

	/**
	 * Get value of property fontSize
	 *
	 * @return - value of field fontSize
	 */
	public int getFontSize();

	/**
	 * Set value of property annotations
	 *
	 * @param _value - new element value
	 */
	public void setAnnotations(String _value);

	/**
	 * Get value of property annotations
	 *
	 * @return - value of field annotations
	 */
	public String getAnnotations();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
