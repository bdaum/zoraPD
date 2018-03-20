package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset location
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Location_typeImpl extends AomObject implements Location_type {

	static final long serialVersionUID = -2746138650L;

	/* ----- Constructors ----- */

	public Location_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param city - Property
	 * @param details - Property
	 * @param provinceOrState - Property
	 * @param countryName - Property
	 * @param countryISOCode - Property
	 * @param sublocation - Property
	 * @param worldRegion - Property
	 * @param worldRegionCode - Property
	 * @param longitude - Property
	 * @param latitude - Property
	 * @param altitude - Property
	 * @param plusCode - Property
	 */
	public Location_typeImpl(String city, String details,
			String provinceOrState, String countryName, String countryISOCode,
			String sublocation, String worldRegion, String worldRegionCode,
			Double longitude, Double latitude, Double altitude, String plusCode) {
		super();
		this.city = city;
		this.details = details;
		this.provinceOrState = provinceOrState;
		this.countryName = countryName;
		this.countryISOCode = countryISOCode;
		this.sublocation = sublocation;
		this.worldRegion = worldRegion;
		this.worldRegionCode = worldRegionCode;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.plusCode = plusCode;

	}

	/* ----- Fields ----- */

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

	/* *** Property details *** */

	private String details;

	/**
	 * Set value of property details
	 *
	 * @param _value - new field value
	 */
	public void setDetails(String _value) {
		details = _value;
	}

	/**
	 * Get value of property details
	 *
	 * @return - value of field details
	 */
	public String getDetails() {
		return details;
	}

	/* *** Property provinceOrState *** */

	private String provinceOrState;

	/**
	 * Set value of property provinceOrState
	 *
	 * @param _value - new field value
	 */
	public void setProvinceOrState(String _value) {
		provinceOrState = _value;
	}

	/**
	 * Get value of property provinceOrState
	 *
	 * @return - value of field provinceOrState
	 */
	public String getProvinceOrState() {
		return provinceOrState;
	}

	/* *** Property countryName *** */

	private String countryName;

	/**
	 * Set value of property countryName
	 *
	 * @param _value - new field value
	 */
	public void setCountryName(String _value) {
		countryName = _value;
	}

	/**
	 * Get value of property countryName
	 *
	 * @return - value of field countryName
	 */
	public String getCountryName() {
		return countryName;
	}

	/* *** Property countryISOCode *** */

	private String countryISOCode;

	/**
	 * Set value of property countryISOCode
	 *
	 * @param _value - new field value
	 */
	public void setCountryISOCode(String _value) {
		countryISOCode = _value;
	}

	/**
	 * Get value of property countryISOCode
	 *
	 * @return - value of field countryISOCode
	 */
	public String getCountryISOCode() {
		return countryISOCode;
	}

	/* *** Property sublocation *** */

	private String sublocation;

	/**
	 * Set value of property sublocation
	 *
	 * @param _value - new field value
	 */
	public void setSublocation(String _value) {
		sublocation = _value;
	}

	/**
	 * Get value of property sublocation
	 *
	 * @return - value of field sublocation
	 */
	public String getSublocation() {
		return sublocation;
	}

	/* *** Property worldRegion *** */

	private String worldRegion;

	/**
	 * Set value of property worldRegion
	 *
	 * @param _value - new field value
	 */
	public void setWorldRegion(String _value) {
		worldRegion = _value;
	}

	/**
	 * Get value of property worldRegion
	 *
	 * @return - value of field worldRegion
	 */
	public String getWorldRegion() {
		return worldRegion;
	}

	/* *** Property worldRegionCode *** */

	private String worldRegionCode;

	/**
	 * Set value of property worldRegionCode
	 *
	 * @param _value - new field value
	 */
	public void setWorldRegionCode(String _value) {
		worldRegionCode = _value;
	}

	/**
	 * Get value of property worldRegionCode
	 *
	 * @return - value of field worldRegionCode
	 */
	public String getWorldRegionCode() {
		return worldRegionCode;
	}

	/* *** Property longitude *** */

	private Double longitude;

	/**
	 * Set value of property longitude
	 *
	 * @param _value - new field value
	 */
	public void setLongitude(Double _value) {
		longitude = _value;
	}

	/**
	 * Get value of property longitude
	 *
	 * @return - value of field longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/* *** Property latitude *** */

	private Double latitude;

	/**
	 * Set value of property latitude
	 *
	 * @param _value - new field value
	 */
	public void setLatitude(Double _value) {
		latitude = _value;
	}

	/**
	 * Get value of property latitude
	 *
	 * @return - value of field latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/* *** Property altitude *** */

	private Double altitude;

	/**
	 * Set value of property altitude
	 *
	 * @param _value - new field value
	 */
	public void setAltitude(Double _value) {
		altitude = _value;
	}

	/**
	 * Get value of property altitude
	 *
	 * @return - value of field altitude
	 */
	public Double getAltitude() {
		return altitude;
	}

	/* *** Property plusCode *** */

	private String plusCode;

	/**
	 * Set value of property plusCode
	 *
	 * @param _value - new field value
	 */
	public void setPlusCode(String _value) {
		plusCode = _value;
	}

	/**
	 * Get value of property plusCode
	 *
	 * @return - value of field plusCode
	 */
	public String getPlusCode() {
		return plusCode;
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

		if (!(o instanceof Location_type) || !super.equals(o))
			return false;
		Location_type other = (Location_type) o;
		return ((getCity() == null && other.getCity() == null) || (getCity() != null && getCity()
				.equals(other.getCity())))

				&& ((getDetails() == null && other.getDetails() == null) || (getDetails() != null && getDetails()
						.equals(other.getDetails())))

				&& ((getProvinceOrState() == null && other.getProvinceOrState() == null) || (getProvinceOrState() != null && getProvinceOrState()
						.equals(other.getProvinceOrState())))

				&& ((getCountryName() == null && other.getCountryName() == null) || (getCountryName() != null && getCountryName()
						.equals(other.getCountryName())))

				&& ((getCountryISOCode() == null && other.getCountryISOCode() == null) || (getCountryISOCode() != null && getCountryISOCode()
						.equals(other.getCountryISOCode())))

				&& ((getSublocation() == null && other.getSublocation() == null) || (getSublocation() != null && getSublocation()
						.equals(other.getSublocation())))

				&& ((getWorldRegion() == null && other.getWorldRegion() == null) || (getWorldRegion() != null && getWorldRegion()
						.equals(other.getWorldRegion())))

				&& ((getWorldRegionCode() == null && other.getWorldRegionCode() == null) || (getWorldRegionCode() != null && getWorldRegionCode()
						.equals(other.getWorldRegionCode())))

				&& ((getLongitude() == null && other.getLongitude() == null) || (getLongitude() != null && getLongitude()
						.equals(other.getLongitude())))

				&& ((getLatitude() == null && other.getLatitude() == null) || (getLatitude() != null && getLatitude()
						.equals(other.getLatitude())))

				&& ((getAltitude() == null && other.getAltitude() == null) || (getAltitude() != null && getAltitude()
						.equals(other.getAltitude())))

				&& ((getPlusCode() == null && other.getPlusCode() == null) || (getPlusCode() != null && getPlusCode()
						.equals(other.getPlusCode())))

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

		int hashCode = -569913636
				+ ((getCity() == null) ? 0 : getCity().hashCode());

		hashCode = 31 * hashCode
				+ ((getDetails() == null) ? 0 : getDetails().hashCode());

		hashCode = 31
				* hashCode
				+ ((getProvinceOrState() == null) ? 0 : getProvinceOrState()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getCountryName() == null) ? 0 : getCountryName().hashCode());

		hashCode = 31
				* hashCode
				+ ((getCountryISOCode() == null) ? 0 : getCountryISOCode()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getSublocation() == null) ? 0 : getSublocation().hashCode());

		hashCode = 31
				* hashCode
				+ ((getWorldRegion() == null) ? 0 : getWorldRegion().hashCode());

		hashCode = 31
				* hashCode
				+ ((getWorldRegionCode() == null) ? 0 : getWorldRegionCode()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getLongitude() == null) ? 0 : getLongitude().hashCode());

		hashCode = 31 * hashCode
				+ ((getLatitude() == null) ? 0 : getLatitude().hashCode());

		hashCode = 31 * hashCode
				+ ((getAltitude() == null) ? 0 : getAltitude().hashCode());

		hashCode = 31 * hashCode
				+ ((getPlusCode() == null) ? 0 : getPlusCode().hashCode());

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
