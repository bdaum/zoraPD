package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset migrationPolicy
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class MigrationPolicy_typeImpl extends AomObject implements
		MigrationPolicy_type {

	static final long serialVersionUID = -851465829L;

	/* ----- Constructors ----- */

	public MigrationPolicy_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param fileSeparatorPolicy - Property
	 * @param targetCatalog - Property
	 */
	public MigrationPolicy_typeImpl(String name, String fileSeparatorPolicy,
			String targetCatalog) {
		super();
		this.name = name;
		this.fileSeparatorPolicy = fileSeparatorPolicy;
		this.targetCatalog = targetCatalog;

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

	/* *** Property fileSeparatorPolicy *** */

	private String fileSeparatorPolicy = AomConstants.INIT_String;

	/**
	 * Set value of property fileSeparatorPolicy
	 *
	 * @param _value - new field value
	 */
	public void setFileSeparatorPolicy(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "fileSeparatorPolicy"));
		String _valueIntern = _value.intern();
		if (!(_valueIntern == fileSeparatorPolicy_nOCHANGE
				|| _valueIntern == fileSeparatorPolicy_tOSLASH || _valueIntern == fileSeparatorPolicy_tOBACKSLASH))
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_VIOLATES_ENUMERATION,
					String.valueOf(_value)));

		fileSeparatorPolicy = _value;
	}

	/**
	 * Get value of property fileSeparatorPolicy
	 *
	 * @return - value of field fileSeparatorPolicy
	 */
	public String getFileSeparatorPolicy() {
		return fileSeparatorPolicy;
	}

	/* *** Property targetCatalog *** */

	private String targetCatalog = AomConstants.INIT_String;

	/**
	 * Set value of property targetCatalog
	 *
	 * @param _value - new field value
	 */
	public void setTargetCatalog(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "targetCatalog"));
		targetCatalog = _value;
	}

	/**
	 * Get value of property targetCatalog
	 *
	 * @return - value of field targetCatalog
	 */
	public String getTargetCatalog() {
		return targetCatalog;
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

		if (!(o instanceof MigrationPolicy_type) || !super.equals(o))
			return false;
		MigrationPolicy_type other = (MigrationPolicy_type) o;
		return ((getName() == null && other.getName() == null) || (getName() != null && getName()
				.equals(other.getName())))

				&& ((getFileSeparatorPolicy() == null && other
						.getFileSeparatorPolicy() == null) || (getFileSeparatorPolicy() != null && getFileSeparatorPolicy()
						.equals(other.getFileSeparatorPolicy())))

				&& ((getTargetCatalog() == null && other.getTargetCatalog() == null) || (getTargetCatalog() != null && getTargetCatalog()
						.equals(other.getTargetCatalog())))

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

		int hashCode = -1964598329
				+ ((getName() == null) ? 0 : getName().hashCode());

		hashCode = 31
				* hashCode
				+ ((getFileSeparatorPolicy() == null) ? 0
						: getFileSeparatorPolicy().hashCode());

		hashCode = 31
				* hashCode
				+ ((getTargetCatalog() == null) ? 0 : getTargetCatalog()
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

		if (name == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));

		if (fileSeparatorPolicy == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "fileSeparatorPolicy"));

		if (targetCatalog == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "targetCatalog"));

	}

}
