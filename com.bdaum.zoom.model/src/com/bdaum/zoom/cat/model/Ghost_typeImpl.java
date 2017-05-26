package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset ghost
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Ghost_typeImpl extends AomObject implements Ghost_type {

	static final long serialVersionUID = -124479860L;

	/* ----- Constructors ----- */

	public Ghost_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param uri - Property
	 * @param volume - Property
	 */
	public Ghost_typeImpl(String name, String uri, String volume) {
		super();
		this.name = name;
		this.uri = uri;
		this.volume = volume;

	}

	/* ----- Fields ----- */

	/* *** Property name *** */

	private String name = AomConstants.INIT_String;

	/**
	 * Set value of property name
	 *
	 * @param _value - new field value
	 */
	public void setName(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));
		name = _value;
	}

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName() {
		return name;
	}

	/* *** Property uri *** */

	private String uri = AomConstants.INIT_String;

	/**
	 * Set value of property uri
	 *
	 * @param _value - new field value
	 */
	public void setUri(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "uri"));
		uri = _value;
	}

	/**
	 * Get value of property uri
	 *
	 * @return - value of field uri
	 */
	public String getUri() {
		return uri;
	}

	/* *** Property volume *** */

	private String volume;

	/**
	 * Set value of property volume
	 *
	 * @param _value - new field value
	 */
	public void setVolume(String _value) {
		volume = _value;
	}

	/**
	 * Get value of property volume
	 *
	 * @return - value of field volume
	 */
	public String getVolume() {
		return volume;
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

		if (!(o instanceof Ghost_type) || !super.equals(o))
			return false;
		Ghost_type other = (Ghost_type) o;
		return ((getName() == null && other.getName() == null) || (getName() != null && getName()
				.equals(other.getName())))

				&& ((getUri() == null && other.getUri() == null) || (getUri() != null && getUri()
						.equals(other.getUri())))

				&& ((getVolume() == null && other.getVolume() == null) || (getVolume() != null && getVolume()
						.equals(other.getVolume())))

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

		int hashCode = -902869770
				+ ((getName() == null) ? 0 : getName().hashCode());

		hashCode = 31 * hashCode
				+ ((getUri() == null) ? 0 : getUri().hashCode());

		hashCode = 31 * hashCode
				+ ((getVolume() == null) ? 0 : getVolume().hashCode());

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

		if (name == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));

		if (uri == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "uri"));

	}

}
