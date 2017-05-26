package com.bdaum.zoom.cat.model;

import java.util.*;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset migrationPolicy
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class MigrationPolicyImpl extends MigrationPolicy_typeImpl implements
		MigrationPolicy {

	static final long serialVersionUID = -2523735294L;

	/* ----- Constructors ----- */

	public MigrationPolicyImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param fileSeparatorPolicy - Property
	 * @param targetCatalog - Property
	 */
	public MigrationPolicyImpl(String name, String fileSeparatorPolicy,
			String targetCatalog) {
		super(name, fileSeparatorPolicy, targetCatalog);

	}

	/* ----- Initialisation ----- */

	private static List<Instrumentation> _instrumentation = new ArrayList<Instrumentation>();

	public static void attachInstrumentation(int point, Aspect aspect,
			Object extension) {
		attachInstrumentation(_instrumentation, point, aspect, extension);
	}

	public static void attachInstrumentation(int point, Aspect aspect) {
		attachInstrumentation(_instrumentation, point, aspect);
	}

	public static void attachInstrumentation(Properties properties,
			Aspect aspect) {
		attachInstrumentation(_instrumentation, MigrationPolicyImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Arc rule *** */

	private MigrationRule[] rule = new MigrationRule[0];

	/**
	 * Set value of property rule
	 *
	 * @param _value - new element value
	 */
	public void setRule(MigrationRule[] _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "rule"));
		if (_value.length < 1)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MINOCCURS, "1", "rule"));
		rule = _value;

		for (int _i = 0; _i < _value.length; _i++)
			if (_value[_i] != null)
				_value[_i].setMigrationPolicy_rule_parent(this);

	}

	/**
	 * Set single element of array rule
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setRule(MigrationRule _element, int _i) {
		if (_element != null)
			_element.setMigrationPolicy_rule_parent(this);
		rule[_i] = _element;
	}

	/**
	 * Get value of property rule
	 *
	 * @return - value of field rule
	 */
	public MigrationRule[] getRule() {
		return rule;
	}

	/**
	 * Get single element of array rule
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array rule
	 */
	public MigrationRule getRule(int _i) {
		return rule[_i];
	}

	/* ----- Equality and Identity ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof MigrationPolicy) || !super.equals(o))
			return false;
		MigrationPolicy other = (MigrationPolicy) o;
		return ((getRule() == null && other.getRule() == null) || (getRule() != null && getRule()
				.equals(other.getRule())))

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

		return super.hashCode() * 31
				+ ((getRule() == null) ? 0 : getRule().hashCode());
	}

	/**
	 * Compares the specified object with this object for primary key equality.
	 *
	 * @param o the object to be compared with this object
	 * @return true if the specified object is key-identical to this object
	 * @see com.bdaum.aoModeling.runtime.IAsset#isKeyIdentical
	 */
	public boolean isKeyIdentical(Object o) {
		return this == o;
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	public int keyHashCode() {
		return hashCode();
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

		if (rule == null || rule.length == 0)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "rule"));

		super.validateCompleteness();
	}

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException {
		validateCompleteness();
	}

	@Override
	public String toString() {
		return getName();
	}

}
