package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset lastDeviceImport
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class LastDeviceImport_typeImpl extends AomObject implements
		LastDeviceImport_type {

	static final long serialVersionUID = -2782219318L;

	/* ----- Constructors ----- */

	public LastDeviceImport_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param volume - Property
	 * @param timestamp - Property
	 * @param description - Property
	 * @param owner - Property
	 */
	public LastDeviceImport_typeImpl(String volume, long timestamp,
			String description, String owner) {
		super();
		this.volume = volume;
		this.timestamp = timestamp;
		this.description = description;
		this.owner = owner;

	}

	/* ----- Fields ----- */

	/* *** Property volume *** */

	private String volume = AomConstants.INIT_String;

	/**
	 * Set value of property volume
	 *
	 * @param _value - new field value
	 */
	public void setVolume(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "volume"));
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

	/* *** Property timestamp *** */

	private long timestamp;

	/**
	 * Set value of property timestamp
	 *
	 * @param _value - new field value
	 */
	public void setTimestamp(long _value) {
		timestamp = _value;
	}

	/**
	 * Get value of property timestamp
	 *
	 * @return - value of field timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/* *** Property description *** */

	private String description;

	/**
	 * Set value of property description
	 *
	 * @param _value - new field value
	 */
	public void setDescription(String _value) {
		description = _value;
	}

	/**
	 * Get value of property description
	 *
	 * @return - value of field description
	 */
	public String getDescription() {
		return description;
	}

	/* *** Property owner *** */

	private String owner;

	/**
	 * Set value of property owner
	 *
	 * @param _value - new field value
	 */
	public void setOwner(String _value) {
		owner = _value;
	}

	/**
	 * Get value of property owner
	 *
	 * @return - value of field owner
	 */
	public String getOwner() {
		return owner;
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

		if (!(o instanceof LastDeviceImport_type) || !super.equals(o))
			return false;
		LastDeviceImport_type other = (LastDeviceImport_type) o;
		return ((getVolume() == null && other.getVolume() == null) || (getVolume() != null && getVolume()
				.equals(other.getVolume())))

				&& getTimestamp() == other.getTimestamp()

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& ((getOwner() == null && other.getOwner() == null) || (getOwner() != null && getOwner()
						.equals(other.getOwner())))

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

		int hashCode = -1688414344
				+ ((getVolume() == null) ? 0 : getVolume().hashCode());

		hashCode = 31 * hashCode
				+ (int) (getTimestamp() ^ (getTimestamp() >>> 32));

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode
				+ ((getOwner() == null) ? 0 : getOwner().hashCode());

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

		if (volume == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "volume"));

	}

}
