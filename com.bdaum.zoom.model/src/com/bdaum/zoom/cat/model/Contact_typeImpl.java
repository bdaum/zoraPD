package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset Contact
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Contact_typeImpl extends AomObject implements Contact_type {

	static final long serialVersionUID = -724531173L;

	/* ----- Constructors ----- */

	public Contact_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param city - Property
	 * @param country - Property
	 * @param postalCode - Property
	 * @param state - Property
	 */
	public Contact_typeImpl(String city, String country, String postalCode,
			String state) {
		super();
		this.city = city;
		this.country = country;
		this.postalCode = postalCode;
		this.state = state;

	}

	/* ----- Fields ----- */

	/* *** Property address *** */

	private String[] address = new String[0];

	/**
	 * Set value of property address
	 *
	 * @param _value - new element value
	 */
	public void setAddress(String[] _value) {
		address = _value;
	}

	/**
	 * Set single element of array address
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setAddress(String _element, int _i) {
		address[_i] = _element;
	}

	/**
	 * Get value of property address
	 *
	 * @return - value of field address
	 */
	public String[] getAddress() {
		return address;
	}

	/**
	 * Get single element of array address
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array address
	 */
	public String getAddress(int _i) {
		return address[_i];
	}

	/* *** Property city *** */

	private String city;

	/**
	 * Set value of property city
	 *
	 * @param _value - new field value
	 */
	public void setCity(String _value) {
		city = _value;
	}

	/**
	 * Get value of property city
	 *
	 * @return - value of field city
	 */
	public String getCity() {
		return city;
	}

	/* *** Property country *** */

	private String country;

	/**
	 * Set value of property country
	 *
	 * @param _value - new field value
	 */
	public void setCountry(String _value) {
		country = _value;
	}

	/**
	 * Get value of property country
	 *
	 * @return - value of field country
	 */
	public String getCountry() {
		return country;
	}

	/* *** Property email *** */

	private String[] email = new String[0];

	/**
	 * Set value of property email
	 *
	 * @param _value - new element value
	 */
	public void setEmail(String[] _value) {
		email = _value;
	}

	/**
	 * Set single element of array email
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setEmail(String _element, int _i) {
		email[_i] = _element;
	}

	/**
	 * Get value of property email
	 *
	 * @return - value of field email
	 */
	public String[] getEmail() {
		return email;
	}

	/**
	 * Get single element of array email
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array email
	 */
	public String getEmail(int _i) {
		return email[_i];
	}

	/* *** Property phone *** */

	private String[] phone = new String[0];

	/**
	 * Set value of property phone
	 *
	 * @param _value - new element value
	 */
	public void setPhone(String[] _value) {
		phone = _value;
	}

	/**
	 * Set single element of array phone
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPhone(String _element, int _i) {
		phone[_i] = _element;
	}

	/**
	 * Get value of property phone
	 *
	 * @return - value of field phone
	 */
	public String[] getPhone() {
		return phone;
	}

	/**
	 * Get single element of array phone
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array phone
	 */
	public String getPhone(int _i) {
		return phone[_i];
	}

	/* *** Property postalCode *** */

	private String postalCode;

	/**
	 * Set value of property postalCode
	 *
	 * @param _value - new field value
	 */
	public void setPostalCode(String _value) {
		postalCode = _value;
	}

	/**
	 * Get value of property postalCode
	 *
	 * @return - value of field postalCode
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/* *** Property state *** */

	private String state;

	/**
	 * Set value of property state
	 *
	 * @param _value - new field value
	 */
	public void setState(String _value) {
		state = _value;
	}

	/**
	 * Get value of property state
	 *
	 * @return - value of field state
	 */
	public String getState() {
		return state;
	}

	/* *** Property webUrl *** */

	private String[] webUrl = new String[0];

	/**
	 * Set value of property webUrl
	 *
	 * @param _value - new element value
	 */
	public void setWebUrl(String[] _value) {
		webUrl = _value;
	}

	/**
	 * Set single element of array webUrl
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setWebUrl(String _element, int _i) {
		webUrl[_i] = _element;
	}

	/**
	 * Get value of property webUrl
	 *
	 * @return - value of field webUrl
	 */
	public String[] getWebUrl() {
		return webUrl;
	}

	/**
	 * Get single element of array webUrl
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array webUrl
	 */
	public String getWebUrl(int _i) {
		return webUrl[_i];
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

		if (!(o instanceof Contact_type) || !super.equals(o))
			return false;
		Contact_type other = (Contact_type) o;
		return ((getAddress() == null && other.getAddress() == null) || (getAddress() != null && getAddress()
				.equals(other.getAddress())))

				&& ((getCity() == null && other.getCity() == null) || (getCity() != null && getCity()
						.equals(other.getCity())))

				&& ((getCountry() == null && other.getCountry() == null) || (getCountry() != null && getCountry()
						.equals(other.getCountry())))

				&& ((getEmail() == null && other.getEmail() == null) || (getEmail() != null && getEmail()
						.equals(other.getEmail())))

				&& ((getPhone() == null && other.getPhone() == null) || (getPhone() != null && getPhone()
						.equals(other.getPhone())))

				&& ((getPostalCode() == null && other.getPostalCode() == null) || (getPostalCode() != null && getPostalCode()
						.equals(other.getPostalCode())))

				&& ((getState() == null && other.getState() == null) || (getState() != null && getState()
						.equals(other.getState())))

				&& ((getWebUrl() == null && other.getWebUrl() == null) || (getWebUrl() != null && getWebUrl()
						.equals(other.getWebUrl())))

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

		int hashCode = 1970376007 + ((getAddress() == null) ? 0 : getAddress()
				.hashCode());

		hashCode = 31 * hashCode
				+ ((getCity() == null) ? 0 : getCity().hashCode());

		hashCode = 31 * hashCode
				+ ((getCountry() == null) ? 0 : getCountry().hashCode());

		hashCode = 31 * hashCode
				+ ((getEmail() == null) ? 0 : getEmail().hashCode());

		hashCode = 31 * hashCode
				+ ((getPhone() == null) ? 0 : getPhone().hashCode());

		hashCode = 31 * hashCode
				+ ((getPostalCode() == null) ? 0 : getPostalCode().hashCode());

		hashCode = 31 * hashCode
				+ ((getState() == null) ? 0 : getState().hashCode());

		hashCode = 31 * hashCode
				+ ((getWebUrl() == null) ? 0 : getWebUrl().hashCode());

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
