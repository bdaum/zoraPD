package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset category
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Category_typeImpl extends AomObject implements Category_type {

	static final long serialVersionUID = -20970307L;

	/* ----- Constructors ----- */

	public Category_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param label - Property
	 */
	public Category_typeImpl(String label) {
		super();
		this.label = label;

	}

	/* ----- Fields ----- */

	/* *** Property label *** */

	private String label = AomConstants.INIT_String;

	/**
	 * Set value of property label
	 *
	 * @param _value - new field value
	 */
	public void setLabel(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "label"));
		label = _value;
	}

	/**
	 * Get value of property label
	 *
	 * @return - value of field label
	 */
	public String getLabel() {
		return label;
	}

	/* *** Property synonyms *** */

	private String[] synonyms = new String[0];

	/**
	 * Set value of property synonyms
	 *
	 * @param _value - new element value
	 */
	public void setSynonyms(String[] _value) {
		synonyms = _value;
	}

	/**
	 * Set single element of array synonyms
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSynonyms(String _element, int _i) {
		synonyms[_i] = _element;
	}

	/**
	 * Get value of property synonyms
	 *
	 * @return - value of field synonyms
	 */
	public String[] getSynonyms() {
		return synonyms;
	}

	/**
	 * Get single element of array synonyms
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array synonyms
	 */
	public String getSynonyms(int _i) {
		return synonyms[_i];
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

		return (o instanceof Category_typeImpl)
				&& getStringId().equals(((Category_typeImpl) o).getStringId());
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

		return getStringId().hashCode();
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

		if (label == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "label"));

	}

}
