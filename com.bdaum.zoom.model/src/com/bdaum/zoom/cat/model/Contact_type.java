package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset Contact
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Contact_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property address
	 *
	 * @param _value - new element value
	 */
	public void setAddress(String[] _value);

	/**
	 * Set single element of array address
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setAddress(String _element, int _i);

	/**
	 * Get value of property address
	 *
	 * @return - value of field address
	 */
	public String[] getAddress();

	/**
	 * Get single element of array address
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array address
	 */
	public String getAddress(int _i);

	/**
	 * Set value of property city
	 *
	 * @param _value - new element value
	 */
	public void setCity(String _value);

	/**
	 * Get value of property city
	 *
	 * @return - value of field city
	 */
	public String getCity();

	/**
	 * Set value of property country
	 *
	 * @param _value - new element value
	 */
	public void setCountry(String _value);

	/**
	 * Get value of property country
	 *
	 * @return - value of field country
	 */
	public String getCountry();

	/**
	 * Set value of property email
	 *
	 * @param _value - new element value
	 */
	public void setEmail(String[] _value);

	/**
	 * Set single element of array email
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setEmail(String _element, int _i);

	/**
	 * Get value of property email
	 *
	 * @return - value of field email
	 */
	public String[] getEmail();

	/**
	 * Get single element of array email
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array email
	 */
	public String getEmail(int _i);

	/**
	 * Set value of property phone
	 *
	 * @param _value - new element value
	 */
	public void setPhone(String[] _value);

	/**
	 * Set single element of array phone
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPhone(String _element, int _i);

	/**
	 * Get value of property phone
	 *
	 * @return - value of field phone
	 */
	public String[] getPhone();

	/**
	 * Get single element of array phone
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array phone
	 */
	public String getPhone(int _i);

	/**
	 * Set value of property postalCode
	 *
	 * @param _value - new element value
	 */
	public void setPostalCode(String _value);

	/**
	 * Get value of property postalCode
	 *
	 * @return - value of field postalCode
	 */
	public String getPostalCode();

	/**
	 * Set value of property state
	 *
	 * @param _value - new element value
	 */
	public void setState(String _value);

	/**
	 * Get value of property state
	 *
	 * @return - value of field state
	 */
	public String getState();

	/**
	 * Set value of property webUrl
	 *
	 * @param _value - new element value
	 */
	public void setWebUrl(String[] _value);

	/**
	 * Set single element of array webUrl
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setWebUrl(String _element, int _i);

	/**
	 * Get value of property webUrl
	 *
	 * @return - value of field webUrl
	 */
	public String[] getWebUrl();

	/**
	 * Get single element of array webUrl
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array webUrl
	 */
	public String getWebUrl(int _i);

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
