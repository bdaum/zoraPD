package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset migrationRule
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class MigrationRule_typeImpl extends AomObject implements
		MigrationRule_type {

	static final long serialVersionUID = -1713369199L;

	/* ----- Constructors ----- */

	public MigrationRule_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param sourcePattern - Property
	 * @param targetPattern - Property
	 * @param targetVolume - Property
	 */
	public MigrationRule_typeImpl(String sourcePattern, String targetPattern,
			String targetVolume) {
		super();
		this.sourcePattern = sourcePattern;
		this.targetPattern = targetPattern;
		this.targetVolume = targetVolume;

	}

	/* ----- Fields ----- */

	/* *** Property sourcePattern *** */

	private String sourcePattern = AomConstants.INIT_String;

	/**
	 * Set value of property sourcePattern
	 *
	 * @param _value - new field value
	 */
	public void setSourcePattern(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "sourcePattern"));
		sourcePattern = _value;
	}

	/**
	 * Get value of property sourcePattern
	 *
	 * @return - value of field sourcePattern
	 */
	public String getSourcePattern() {
		return sourcePattern;
	}

	/* *** Property targetPattern *** */

	private String targetPattern = AomConstants.INIT_String;

	/**
	 * Set value of property targetPattern
	 *
	 * @param _value - new field value
	 */
	public void setTargetPattern(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "targetPattern"));
		targetPattern = _value;
	}

	/**
	 * Get value of property targetPattern
	 *
	 * @return - value of field targetPattern
	 */
	public String getTargetPattern() {
		return targetPattern;
	}

	/* *** Property targetVolume *** */

	private String targetVolume;

	/**
	 * Set value of property targetVolume
	 *
	 * @param _value - new field value
	 */
	public void setTargetVolume(String _value) {
		targetVolume = _value;
	}

	/**
	 * Get value of property targetVolume
	 *
	 * @return - value of field targetVolume
	 */
	public String getTargetVolume() {
		return targetVolume;
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

		if (!(o instanceof MigrationRule_type) || !super.equals(o))
			return false;
		MigrationRule_type other = (MigrationRule_type) o;
		return ((getSourcePattern() == null && other.getSourcePattern() == null) || (getSourcePattern() != null && getSourcePattern()
				.equals(other.getSourcePattern())))

				&& ((getTargetPattern() == null && other.getTargetPattern() == null) || (getTargetPattern() != null && getTargetPattern()
						.equals(other.getTargetPattern())))

				&& ((getTargetVolume() == null && other.getTargetVolume() == null) || (getTargetVolume() != null && getTargetVolume()
						.equals(other.getTargetVolume())))

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

		int hashCode = 1381168273 + ((getSourcePattern() == null) ? 0
				: getSourcePattern().hashCode());

		hashCode = 31
				* hashCode
				+ ((getTargetPattern() == null) ? 0 : getTargetPattern()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getTargetVolume() == null) ? 0 : getTargetVolume()
						.hashCode());

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

		if (sourcePattern == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "sourcePattern"));

		if (targetPattern == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "targetPattern"));

	}

}
