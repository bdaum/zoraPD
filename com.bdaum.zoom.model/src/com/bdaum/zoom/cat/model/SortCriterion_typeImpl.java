package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset sortCriterion
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class SortCriterion_typeImpl extends AomObject implements
		SortCriterion_type {

	static final long serialVersionUID = -1958605576L;

	/* ----- Constructors ----- */

	public SortCriterion_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param field - Property
	 * @param subfield - Property
	 * @param descending - Property
	 */
	public SortCriterion_typeImpl(String field, String subfield,
			boolean descending) {
		super();
		this.field = field;
		this.subfield = subfield;
		this.descending = descending;

	}

	/* ----- Fields ----- */

	/* *** Property field *** */

	private String field = AomConstants.INIT_String;

	/**
	 * Set value of property field
	 *
	 * @param _value - new field value
	 */
	public void setField(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "field"));
		field = _value;
	}

	/**
	 * Get value of property field
	 *
	 * @return - value of field field
	 */
	public String getField() {
		return field;
	}

	/* *** Property subfield *** */

	private String subfield;

	/**
	 * Set value of property subfield
	 *
	 * @param _value - new field value
	 */
	public void setSubfield(String _value) {
		subfield = _value;
	}

	/**
	 * Get value of property subfield
	 *
	 * @return - value of field subfield
	 */
	public String getSubfield() {
		return subfield;
	}

	/* *** Property descending *** */

	private boolean descending;

	/**
	 * Set value of property descending
	 *
	 * @param _value - new field value
	 */
	public void setDescending(boolean _value) {
		descending = _value;
	}

	/**
	 * Get value of property descending
	 *
	 * @return - value of field descending
	 */
	public boolean getDescending() {
		return descending;
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

		if (!(o instanceof SortCriterion_type) || !super.equals(o))
			return false;
		SortCriterion_type other = (SortCriterion_type) o;
		return ((getField() == null && other.getField() == null) || (getField() != null && getField()
				.equals(other.getField())))

				&& ((getSubfield() == null && other.getSubfield() == null) || (getSubfield() != null && getSubfield()
						.equals(other.getSubfield())))

				&& getDescending() == other.getDescending()

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

		int hashCode = -1926192118
				+ ((getField() == null) ? 0 : getField().hashCode());

		hashCode = 31 * hashCode
				+ ((getSubfield() == null) ? 0 : getSubfield().hashCode());

		hashCode = 31 * hashCode + (getDescending() ? 1231 : 1237);

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

		if (field == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "field"));

	}

}
