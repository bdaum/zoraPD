package com.bdaum.zoom.cat.model;

import java.util.*;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset rgb
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Rgb_type extends AomValueChangedNotifier, IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property r
	 *
	 * @param _value - new element value
	 */
	public void setR(int _value);

	/**
	 * Get value of property r
	 *
	 * @return - value of field r
	 */
	public int getR();

	/**
	 * Set value of property g
	 *
	 * @param _value - new element value
	 */
	public void setG(int _value);

	/**
	 * Get value of property g
	 *
	 * @return - value of field g
	 */
	public int getG();

	/**
	 * Set value of property b
	 *
	 * @param _value - new element value
	 */
	public void setB(int _value);

	/**
	 * Get value of property b
	 *
	 * @return - value of field b
	 */
	public int getB();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
