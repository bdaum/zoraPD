package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset criterion
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Criterion_typeImpl extends AomObject implements Criterion_type {

	static final long serialVersionUID = 542004922L;

	/* ----- Constructors ----- */

	public Criterion_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param field - Property
	 * @param subfield - Property
	 * @param value - Property
	 * @param to - Property
	 * @param relation - Property
	 * @param and - Property
	 */
	public Criterion_typeImpl(String field, String subfield, Object value,
			Object to, int relation, boolean and) {
		super();
		this.field = field;
		this.subfield = subfield;
		this.value = value;
		this.to = to;
		this.relation = relation;
		this.and = and;

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

	/* *** Property value *** */

	private Object value;

	/**
	 * Set value of property value
	 *
	 * @param _value - new field value
	 */
	public void setValue(Object _value) {
		value = _value;
	}

	/**
	 * Get value of property value
	 *
	 * @return - value of field value
	 */
	public Object getValue() {
		return value;
	}

	/* *** Property to *** */

	private Object to;

	/**
	 * Set value of property to
	 *
	 * @param _value - new field value
	 */
	public void setTo(Object _value) {
		to = _value;
	}

	/**
	 * Get value of property to
	 *
	 * @return - value of field to
	 */
	public Object getTo() {
		return to;
	}

	/* *** Property relation *** */

	private int relation;

	/**
	 * Set value of property relation
	 *
	 * @param _value - new field value
	 */
	public void setRelation(int _value) {
		relation = _value;
	}

	/**
	 * Get value of property relation
	 *
	 * @return - value of field relation
	 */
	public int getRelation() {
		return relation;
	}

	/* *** Property and *** */

	private boolean and;

	/**
	 * Set value of property and
	 *
	 * @param _value - new field value
	 */
	public void setAnd(boolean _value) {
		and = _value;
	}

	/**
	 * Get value of property and
	 *
	 * @return - value of field and
	 */
	public boolean getAnd() {
		return and;
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

		if (!(o instanceof Criterion_type) || !super.equals(o))
			return false;
		Criterion_type other = (Criterion_type) o;
		return ((getField() == null && other.getField() == null) || (getField() != null && getField()
				.equals(other.getField())))

				&& ((getSubfield() == null && other.getSubfield() == null) || (getSubfield() != null && getSubfield()
						.equals(other.getSubfield())))

				&& ((getValue() == null && other.getValue() == null) || (getValue() != null && getValue()
						.equals(other.getValue())))

				&& ((getTo() == null && other.getTo() == null) || (getTo() != null && getTo()
						.equals(other.getTo())))

				&& getRelation() == other.getRelation()

				&& getAnd() == other.getAnd()

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

		int hashCode = -1716678008
				+ ((getField() == null) ? 0 : getField().hashCode());

		hashCode = 31 * hashCode
				+ ((getSubfield() == null) ? 0 : getSubfield().hashCode());

		hashCode = 31 * hashCode
				+ ((getValue() == null) ? 0 : getValue().hashCode());

		hashCode = 31 * hashCode + ((getTo() == null) ? 0 : getTo().hashCode());

		hashCode = 31 * hashCode + getRelation();

		hashCode = 31 * hashCode + (getAnd() ? 1231 : 1237);

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
