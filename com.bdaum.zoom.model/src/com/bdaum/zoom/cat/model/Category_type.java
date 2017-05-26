package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset category
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Category_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property label
	 *
	 * @param _value - new element value
	 */
	public void setLabel(String _value);

	/**
	 * Get value of property label
	 *
	 * @return - value of field label
	 */
	public String getLabel();

	/**
	 * Set value of property synonyms
	 *
	 * @param _value - new element value
	 */
	public void setSynonyms(String[] _value);

	/**
	 * Set single element of array synonyms
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSynonyms(String _element, int _i);

	/**
	 * Get value of property synonyms
	 *
	 * @return - value of field synonyms
	 */
	public String[] getSynonyms();

	/**
	 * Get single element of array synonyms
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array synonyms
	 */
	public String getSynonyms(int _i);

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
