package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset textSearchOptions
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface TextSearchOptions_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property queryString
	 *
	 * @param _value - new element value
	 */
	public void setQueryString(String _value);

	/**
	 * Get value of property queryString
	 *
	 * @return - value of field queryString
	 */
	public String getQueryString();

	/**
	 * Set value of property maxResults
	 *
	 * @param _value - new element value
	 */
	public void setMaxResults(int _value);

	/**
	 * Get value of property maxResults
	 *
	 * @return - value of field maxResults
	 */
	public int getMaxResults();

	/**
	 * Set value of property minScore
	 *
	 * @param _value - new element value
	 */
	public void setMinScore(float _value);

	/**
	 * Get value of property minScore
	 *
	 * @return - value of field minScore
	 */
	public float getMinScore();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
