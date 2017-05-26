package com.bdaum.zoom.cat.model.derivedBy;

import java.util.*;
import com.bdaum.zoom.cat.model.DerivedBy_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset derivedBy
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class DerivedByImpl extends DerivedBy_typeImpl implements DerivedBy {

	static final long serialVersionUID = 949677684L;

	/* ----- Constructors ----- */

	public DerivedByImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param recipe - Property
	 * @param parameterFile - Property
	 * @param tool - Property
	 * @param date - Property
	 * @param derivative - Arc
	 * @param original - Arc
	 */
	public DerivedByImpl(String recipe, String parameterFile, String tool,
			Date date, String derivative, String original) {
		super(recipe, parameterFile, tool, date);
		this.derivative = derivative;
		this.original = original;

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
		attachInstrumentation(_instrumentation, DerivedByImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Arc derivative *** */

	private String derivative = AomConstants.INIT_String;

	/**
	 * Set value of property derivative
	 *
	 * @param _value - new field value
	 */
	public void setDerivative(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "derivative"));
		derivative = _value;
	}

	/**
	 * Get value of property derivative
	 *
	 * @return - value of field derivative
	 */
	public String getDerivative() {
		return derivative;
	}

	/* *** Arc original *** */

	private String original = AomConstants.INIT_String;

	/**
	 * Set value of property original
	 *
	 * @param _value - new field value
	 */
	public void setOriginal(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "original"));
		original = _value;
	}

	/**
	 * Get value of property original
	 *
	 * @return - value of field original
	 */
	public String getOriginal() {
		return original;
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

		if (!(o instanceof DerivedBy) || !super.equals(o))
			return false;
		DerivedBy other = (DerivedBy) o;
		return ((getDerivative() == null && other.getDerivative() == null) || (getDerivative() != null && getDerivative()
				.equals(other.getDerivative())))

				&& ((getOriginal() == null && other.getOriginal() == null) || (getOriginal() != null && getOriginal()
						.equals(other.getOriginal())))

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

		int hashCode = super.hashCode() * 31
				+ ((getDerivative() == null) ? 0 : getDerivative().hashCode());

		hashCode = 31 * hashCode
				+ ((getOriginal() == null) ? 0 : getOriginal().hashCode());

		return hashCode;
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

		if (derivative == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "derivative"));

		if (original == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "original"));

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

}
