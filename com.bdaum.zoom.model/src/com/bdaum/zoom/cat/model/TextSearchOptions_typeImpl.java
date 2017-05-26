package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset textSearchOptions
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class TextSearchOptions_typeImpl extends AomObject implements
		TextSearchOptions_type {

	static final long serialVersionUID = -449190222L;

	/* ----- Constructors ----- */

	public TextSearchOptions_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param queryString - Property
	 * @param maxResults - Property
	 * @param minScore - Property
	 */
	public TextSearchOptions_typeImpl(String queryString, int maxResults,
			float minScore) {
		super();
		this.queryString = queryString;
		this.maxResults = maxResults;
		this.minScore = minScore;

	}

	/* ----- Fields ----- */

	/* *** Property queryString *** */

	private String queryString = AomConstants.INIT_String;

	/**
	 * Set value of property queryString
	 *
	 * @param _value - new field value
	 */
	public void setQueryString(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "queryString"));
		queryString = _value;
	}

	/**
	 * Get value of property queryString
	 *
	 * @return - value of field queryString
	 */
	public String getQueryString() {
		return queryString;
	}

	/* *** Property maxResults *** */

	private int maxResults;

	/**
	 * Set value of property maxResults
	 *
	 * @param _value - new field value
	 */
	public void setMaxResults(int _value) {
		maxResults = _value;
	}

	/**
	 * Get value of property maxResults
	 *
	 * @return - value of field maxResults
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/* *** Property minScore *** */

	private float minScore;

	/**
	 * Set value of property minScore
	 *
	 * @param _value - new field value
	 */
	public void setMinScore(float _value) {
		minScore = _value;
	}

	/**
	 * Get value of property minScore
	 *
	 * @return - value of field minScore
	 */
	public float getMinScore() {
		return minScore;
	}

	/* ----- Equality ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof TextSearchOptions_type) || !super.equals(o))
			return false;
		TextSearchOptions_type other = (TextSearchOptions_type) o;
		return ((getQueryString() == null && other.getQueryString() == null) || (getQueryString() != null && getQueryString()
				.equals(other.getQueryString())))

		&& getMaxResults() == other.getMaxResults()

		&& getMinScore() == other.getMinScore()

		;
	}

	/**
	 * Returns the hash code for this object.
	 * @return the hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 * @see java.lang.Object#equals(Object)
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {

		int hashCode = 1916010896 + ((getQueryString() == null) ? 0
				: getQueryString().hashCode());

		hashCode = 31 * hashCode + getMaxResults();

		hashCode = 31 * hashCode + computeDoubleHash(getMinScore());

		return hashCode;
	}

	/**
	 * Creates a clone of this object.

	 *   Not supported in this class
	 * @throws CloneNotSupportedException;
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException {

		if (queryString == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "queryString"));

	}

}
