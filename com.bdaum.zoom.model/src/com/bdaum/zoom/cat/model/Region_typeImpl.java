package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset region
 *
 * StringID is the hexadecimal representation of 4 16-bit parts (x,y,width,height). Each part is called to the maximum unsigned binary 16-bit number.
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Region_typeImpl extends AomObject implements Region_type {

	static final long serialVersionUID = 249685895L;

	/* ----- Constructors ----- */

	public Region_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param keywordAdded - Property
	 * @param personEmailDigest - Property
	 * @param personLiveCID - Property
	 * @param description - Property
	 * @param type - Property
	 */
	public Region_typeImpl(boolean keywordAdded, String personEmailDigest,
			Long personLiveCID, String description, String type) {
		super();
		this.keywordAdded = keywordAdded;
		this.personEmailDigest = personEmailDigest;
		this.personLiveCID = personLiveCID;
		this.description = description;
		this.type = type;

	}

	/* ----- Fields ----- */

	/* *** Property keywordAdded *** */

	private boolean keywordAdded;

	/**
	 * Set value of property keywordAdded
	 *
	 * @param _value - new field value
	 */
	public void setKeywordAdded(boolean _value) {
		keywordAdded = _value;
	}

	/**
	 * Get value of property keywordAdded
	 *
	 * @return - value of field keywordAdded
	 */
	public boolean getKeywordAdded() {
		return keywordAdded;
	}

	/* *** Property personEmailDigest *** */

	private String personEmailDigest;

	/**
	 * Set value of property personEmailDigest
	 *
	 * @param _value - new field value
	 */
	public void setPersonEmailDigest(String _value) {
		personEmailDigest = _value;
	}

	/**
	 * Get value of property personEmailDigest
	 *
	 * @return - value of field personEmailDigest
	 */
	public String getPersonEmailDigest() {
		return personEmailDigest;
	}

	/* *** Property personLiveCID *** */

	private Long personLiveCID;

	/**
	 * Set value of property personLiveCID
	 *
	 * @param _value - new field value
	 */
	public void setPersonLiveCID(Long _value) {
		personLiveCID = _value;
	}

	/**
	 * Get value of property personLiveCID
	 *
	 * @return - value of field personLiveCID
	 */
	public Long getPersonLiveCID() {
		return personLiveCID;
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

	/* *** Property type *** */

	private String type;

	/**
	 * Set value of property type
	 *
	 * @param _value - new field value
	 */
	public void setType(String _value) {
		type = _value;
	}

	/**
	 * Get value of property type
	 *
	 * @return - value of field type
	 */
	public String getType() {
		return type;
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

		if (!(o instanceof Region_type) || !super.equals(o))
			return false;
		Region_type other = (Region_type) o;
		return getKeywordAdded() == other.getKeywordAdded()

				&& ((getPersonEmailDigest() == null && other
						.getPersonEmailDigest() == null) || (getPersonEmailDigest() != null && getPersonEmailDigest()
						.equals(other.getPersonEmailDigest())))

				&& ((getPersonLiveCID() == null && other.getPersonLiveCID() == null) || (getPersonLiveCID() != null && getPersonLiveCID()
						.equals(other.getPersonLiveCID())))

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& ((getType() == null && other.getType() == null) || (getType() != null && getType()
						.equals(other.getType())))

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

		int hashCode = 2106334043 + (getKeywordAdded() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getPersonEmailDigest() == null) ? 0
						: getPersonEmailDigest().hashCode());

		hashCode = 31
				* hashCode
				+ ((getPersonLiveCID() == null) ? 0 : getPersonLiveCID()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode
				+ ((getType() == null) ? 0 : getType().hashCode());

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

	}

}
