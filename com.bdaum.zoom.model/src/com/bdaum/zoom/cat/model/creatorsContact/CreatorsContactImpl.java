package com.bdaum.zoom.cat.model.creatorsContact;

import java.util.*;
import com.bdaum.zoom.cat.model.CreatorsContact_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset creatorsContact
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class CreatorsContactImpl extends CreatorsContact_typeImpl implements
		CreatorsContact {

	static final long serialVersionUID = 2199868850L;

	/* ----- Constructors ----- */

	public CreatorsContactImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param contact - Arc
	 */
	public CreatorsContactImpl(String contact) {
		super();
		this.contact = contact;

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
		attachInstrumentation(_instrumentation, CreatorsContactImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Arc contact *** */

	private String contact = AomConstants.INIT_String;

	/**
	 * Set value of property contact
	 *
	 * @param _value - new field value
	 */
	public void setContact(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "contact"));
		contact = _value;
	}

	/**
	 * Get value of property contact
	 *
	 * @return - value of field contact
	 */
	public String getContact() {
		return contact;
	}

	/* *** Arc asset *** */

	private AomList<String> asset = new FastArrayList<String>("asset",
			PackageInterface.CreatorsContact_asset, 0, Integer.MAX_VALUE, null,
			null);

	/**
	 * Set value of property asset
	 *
	 * @param _value - new element value
	 */
	public void setAsset(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));
		asset = _value;
	}

	/**
	 * Set value of property asset
	 *
	 * @param _value - new element value
	 */
	public void setAsset(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));
		asset = new FastArrayList<String>(_value, "asset",
				PackageInterface.CreatorsContact_asset, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Set single element of list asset
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setAsset(String _element, int _i) {
		asset.set(_i, _element);
	}

	/**
	 * Add an element to list asset
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addAsset(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Asset._element"));

		return asset.add(_element);
	}

	/**
	 * Remove an element from list asset
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeAsset(String _element) {
		return asset.remove(_element);
	}

	/**
	 * Make asset empty 
	 */
	public void clearAsset() {
		asset.clear();
	}

	/**
	 * Get value of property asset
	 *
	 * @return - value of field asset
	 */
	public AomList<String> getAsset() {
		return asset;
	}

	/**
	 * Get single element of list asset
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list asset
	 */
	public String getAsset(int _i) {
		return asset.get(_i);
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

		if (!(o instanceof CreatorsContact) || !super.equals(o))
			return false;
		CreatorsContact other = (CreatorsContact) o;
		return ((getContact() == null && other.getContact() == null) || (getContact() != null && getContact()
				.equals(other.getContact())))

				&& ((getAsset() == null && other.getAsset() == null) || (getAsset() != null && getAsset()
						.equals(other.getAsset())))

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
				+ ((getContact() == null) ? 0 : getContact().hashCode());

		hashCode = 31 * hashCode
				+ ((getAsset() == null) ? 0 : getAsset().hashCode());

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

		if (contact == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "contact"));

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
