package com.bdaum.zoom.cat.model.composedTo;

import java.util.*;
import com.bdaum.zoom.cat.model.ComposedTo_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset composedTo
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class ComposedToImpl extends ComposedTo_typeImpl implements ComposedTo {

	static final long serialVersionUID = -1492157390L;

	/* ----- Constructors ----- */

	public ComposedToImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param type - Property
	 * @param recipe - Property
	 * @param parameterFile - Property
	 * @param tool - Property
	 * @param date - Property
	 * @param composite - Arc
	 */
	public ComposedToImpl(String type, String recipe, String parameterFile,
			String tool, Date date, String composite) {
		super(type, recipe, parameterFile, tool, date);
		this.composite = composite;

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
		attachInstrumentation(_instrumentation, ComposedToImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Arc component *** */

	private AomList<String> component = new FastArrayList<String>("component",
			PackageInterface.ComposedTo_component, 0, Integer.MAX_VALUE, null,
			null);

	/**
	 * Set value of property component
	 *
	 * @param _value - new element value
	 */
	public void setComponent(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "component"));
		component = _value;
	}

	/**
	 * Set value of property component
	 *
	 * @param _value - new element value
	 */
	public void setComponent(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "component"));
		component = new FastArrayList<String>(_value, "component",
				PackageInterface.ComposedTo_component, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Set single element of list component
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setComponent(String _element, int _i) {
		component.set(_i, _element);
	}

	/**
	 * Add an element to list component
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addComponent(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Component._element"));

		return component.add(_element);
	}

	/**
	 * Remove an element from list component
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeComponent(String _element) {
		return component.remove(_element);
	}

	/**
	 * Make component empty 
	 */
	public void clearComponent() {
		component.clear();
	}

	/**
	 * Get value of property component
	 *
	 * @return - value of field component
	 */
	public AomList<String> getComponent() {
		return component;
	}

	/**
	 * Get single element of list component
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list component
	 */
	public String getComponent(int _i) {
		return component.get(_i);
	}

	/* *** Arc composite *** */

	private String composite = AomConstants.INIT_String;

	/**
	 * Set value of property composite
	 *
	 * @param _value - new field value
	 */
	public void setComposite(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "composite"));
		composite = _value;
	}

	/**
	 * Get value of property composite
	 *
	 * @return - value of field composite
	 */
	public String getComposite() {
		return composite;
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

		if (!(o instanceof ComposedTo) || !super.equals(o))
			return false;
		ComposedTo other = (ComposedTo) o;
		return ((getComponent() == null && other.getComponent() == null) || (getComponent() != null && getComponent()
				.equals(other.getComponent())))

				&& ((getComposite() == null && other.getComposite() == null) || (getComposite() != null && getComposite()
						.equals(other.getComposite())))

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
				+ ((getComponent() == null) ? 0 : getComponent().hashCode());

		hashCode = 31 * hashCode
				+ ((getComposite() == null) ? 0 : getComposite().hashCode());

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

		if (composite == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "composite"));

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
