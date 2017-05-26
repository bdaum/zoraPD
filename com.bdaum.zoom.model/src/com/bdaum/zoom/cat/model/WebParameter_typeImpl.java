package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webParameter
 *
 * <b>id</b> is composed of namespace.localID
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class WebParameter_typeImpl extends AomObject implements
		WebParameter_type {

	static final long serialVersionUID = -1334490906L;

	/* ----- Constructors ----- */

	public WebParameter_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param id - Property
	 * @param value - Property
	 * @param encodeHtml - Property
	 * @param linkTo - Property
	 */
	public WebParameter_typeImpl(String id, Object value, boolean encodeHtml,
			String linkTo) {
		super();
		this.id = id;
		this.value = value;
		this.encodeHtml = encodeHtml;
		this.linkTo = linkTo;

	}

	/* ----- Fields ----- */

	/* *** Property id *** */

	private String id = AomConstants.INIT_String;

	/**
	 * Set value of property id
	 *
	 * @param _value - new field value
	 */
	public void setId(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "id"));
		id = _value;
	}

	/**
	 * Get value of property id
	 *
	 * @return - value of field id
	 */
	public String getId() {
		return id;
	}

	/* *** Property value *** */

	private Object value;

	/**
	 * Set value of property value
	 *
	 * @param _value - new field value
	 */
	public void setValue(Object _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "value"));
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

	/* *** Property encodeHtml *** */

	private boolean encodeHtml;

	/**
	 * Set value of property encodeHtml
	 *
	 * @param _value - new field value
	 */
	public void setEncodeHtml(boolean _value) {
		encodeHtml = _value;
	}

	/**
	 * Get value of property encodeHtml
	 *
	 * @return - value of field encodeHtml
	 */
	public boolean getEncodeHtml() {
		return encodeHtml;
	}

	/* *** Property linkTo *** */

	private String linkTo;

	/**
	 * Set value of property linkTo
	 *
	 * @param _value - new field value
	 */
	public void setLinkTo(String _value) {
		linkTo = _value;
	}

	/**
	 * Get value of property linkTo
	 *
	 * @return - value of field linkTo
	 */
	public String getLinkTo() {
		return linkTo;
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

		if (!(o instanceof WebParameter_type) || !super.equals(o))
			return false;
		WebParameter_type other = (WebParameter_type) o;
		return ((getId() == null && other.getId() == null) || (getId() != null && getId()
				.equals(other.getId())))

				&& ((getValue() == null && other.getValue() == null) || (getValue() != null && getValue()
						.equals(other.getValue())))

				&& getEncodeHtml() == other.getEncodeHtml()

				&& ((getLinkTo() == null && other.getLinkTo() == null) || (getLinkTo() != null && getLinkTo()
						.equals(other.getLinkTo())))

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

		int hashCode = 241493468 + ((getId() == null) ? 0 : getId().hashCode());

		hashCode = 31 * hashCode
				+ ((getValue() == null) ? 0 : getValue().hashCode());

		hashCode = 31 * hashCode + (getEncodeHtml() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getLinkTo() == null) ? 0 : getLinkTo().hashCode());

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

		if (id == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "id"));

		if (value == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "value"));

	}

}
