package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset location
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Location_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

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
	 * Set value of property details
	 *
	 * @param _value - new element value
	 */
	public void setDetails(String _value);

	/**
	 * Get value of property details
	 *
	 * @return - value of field details
	 */
	public String getDetails();

	/**
	 * Set value of property provinceOrState
	 *
	 * @param _value - new element value
	 */
	public void setProvinceOrState(String _value);

	/**
	 * Get value of property provinceOrState
	 *
	 * @return - value of field provinceOrState
	 */
	public String getProvinceOrState();

	/**
	 * Set value of property countryName
	 *
	 * @param _value - new element value
	 */
	public void setCountryName(String _value);

	/**
	 * Get value of property countryName
	 *
	 * @return - value of field countryName
	 */
	public String getCountryName();

	/**
	 * Set value of property countryISOCode
	 *
	 * @param _value - new element value
	 */
	public void setCountryISOCode(String _value);

	/**
	 * Get value of property countryISOCode
	 *
	 * @return - value of field countryISOCode
	 */
	public String getCountryISOCode();

	/**
	 * Set value of property sublocation
	 *
	 * @param _value - new element value
	 */
	public void setSublocation(String _value);

	/**
	 * Get value of property sublocation
	 *
	 * @return - value of field sublocation
	 */
	public String getSublocation();

	/**
	 * Set value of property worldRegion
	 *
	 * @param _value - new element value
	 */
	public void setWorldRegion(String _value);

	/**
	 * Get value of property worldRegion
	 *
	 * @return - value of field worldRegion
	 */
	public String getWorldRegion();

	/**
	 * Set value of property worldRegionCode
	 *
	 * @param _value - new element value
	 */
	public void setWorldRegionCode(String _value);

	/**
	 * Get value of property worldRegionCode
	 *
	 * @return - value of field worldRegionCode
	 */
	public String getWorldRegionCode();

	/**
	 * Set value of property longitude
	 *
	 * @param _value - new element value
	 */
	public void setLongitude(Double _value);

	/**
	 * Get value of property longitude
	 *
	 * @return - value of field longitude
	 */
	public Double getLongitude();

	/**
	 * Set value of property latitude
	 *
	 * @param _value - new element value
	 */
	public void setLatitude(Double _value);

	/**
	 * Get value of property latitude
	 *
	 * @return - value of field latitude
	 */
	public Double getLatitude();

	/**
	 * Set value of property altitude
	 *
	 * @param _value - new element value
	 */
	public void setAltitude(Double _value);

	/**
	 * Get value of property altitude
	 *
	 * @return - value of field altitude
	 */
	public Double getAltitude();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
