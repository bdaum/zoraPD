package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset font
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Font_type extends AomValueChangedNotifier, IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property family
	 *
	 * @param _value - new element value
	 */
	public void setFamily(String[] _value);

	/**
	 * Set single element of array family
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setFamily(String _element, int _i);

	/**
	 * Get value of property family
	 *
	 * @return - value of field family
	 */
	public String[] getFamily();

	/**
	 * Get single element of array family
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array family
	 */
	public String getFamily(int _i);

	/**
	 * Set value of property size
	 *
	 * @param _value - new element value
	 */
	public void setSize(int _value);

	/**
	 * Get value of property size
	 *
	 * @return - value of field size
	 */
	public int getSize();

	/**
	 * Set value of property style
	 *
	 * @param _value - new element value
	 */
	public void setStyle(int _value);

	/**
	 * Get value of property style
	 *
	 * @return - value of field style
	 */
	public int getStyle();

	/**
	 * Set value of property weight
	 *
	 * @param _value - new element value
	 */
	public void setWeight(int _value);

	/**
	 * Get value of property weight
	 *
	 * @return - value of field weight
	 */
	public int getWeight();

	/**
	 * Set value of property variant
	 *
	 * @param _value - new element value
	 */
	public void setVariant(int _value);

	/**
	 * Get value of property variant
	 *
	 * @return - value of field variant
	 */
	public int getVariant();

	/**
	 * Set value of property color
	 *
	 * @param _value - new element value
	 */
	public void setColor(Rgb_type _value);

	/**
	 * Get value of property color
	 *
	 * @return - value of field color
	 */
	public Rgb_type getColor();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
