package com.bdaum.zoom.cat.model.location;

import com.bdaum.zoom.cat.model.Location_typeImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShown;
import java.util.*;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreated;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset location
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class LocationImpl extends Location_typeImpl implements Location {

	static final long serialVersionUID = 1044450658L;

	/* ----- Constructors ----- */

	public LocationImpl() {
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
	 */
	public LocationImpl(String city, String details, String provinceOrState,
			String countryName, String countryISOCode, String sublocation,
			String worldRegion, String worldRegionCode, Double longitude,
			Double latitude, Double altitude) {
		super(city, details, provinceOrState, countryName, countryISOCode,
				sublocation, worldRegion, worldRegionCode, longitude, latitude,
				altitude);

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
		attachInstrumentation(_instrumentation, LocationImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc locationCreated_parent *** */

	private String locationCreated_parent;

	/**
	 * Set value of property locationCreated_parent
	 *
	 * @param _value - new field value
	 */
	public void setLocationCreated_parent(String _value) {
		locationCreated_parent = _value;
	}

	/**
	 * Get value of property locationCreated_parent
	 *
	 * @return - value of field locationCreated_parent
	 */
	public String getLocationCreated_parent() {
		return locationCreated_parent;
	}

	/* *** Incoming Arc locationShown_parent *** */

	private AomList<String> locationShown_parent = new FastArrayList<String>(
			"locationShown_parent",
			PackageInterface.Location_locationShown_parent, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property locationShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setLocationShown_parent(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "locationShown_parent"));
		locationShown_parent = _value;
	}

	/**
	 * Set value of property locationShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setLocationShown_parent(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "locationShown_parent"));
		locationShown_parent = new FastArrayList<String>(_value,
				"locationShown_parent",
				PackageInterface.Location_locationShown_parent, 0,
				Integer.MAX_VALUE, null, null);
	}

	/**
	 * Set single element of list locationShown_parent
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setLocationShown_parent(String _element, int _i) {
		locationShown_parent.set(_i, _element);
	}

	/**
	 * Add an element to list locationShown_parent
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addLocationShown_parent(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"LocationShown_parent._element"));

		return locationShown_parent.add(_element);
	}

	/**
	 * Remove an element from list locationShown_parent
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeLocationShown_parent(String _element) {
		return locationShown_parent.remove(_element);
	}

	/**
	 * Make locationShown_parent empty 
	 */
	public void clearLocationShown_parent() {
		locationShown_parent.clear();
	}

	/**
	 * Get value of property locationShown_parent
	 *
	 * @return - value of field locationShown_parent
	 */
	public AomList<String> getLocationShown_parent() {
		return locationShown_parent;
	}

	/**
	 * Get single element of list locationShown_parent
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list locationShown_parent
	 */
	public String getLocationShown_parent(int _i) {
		return locationShown_parent.get(_i);
	}

	/* ----- Equality and Identity ----- */

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
