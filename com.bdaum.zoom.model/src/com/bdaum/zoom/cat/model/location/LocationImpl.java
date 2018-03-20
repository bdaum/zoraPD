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

	private static final long serialVersionUID = 883390336277425800L;

// Modified manually (removed backpointers and other overhead)

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
	 * @param plusCode - Property
	 */
	public LocationImpl(String city, String details, String provinceOrState,
			String countryName, String countryISOCode, String sublocation,
			String worldRegion, String worldRegionCode, Double longitude,
			Double latitude, Double altitude, String plusCode) {
		super(city, details, provinceOrState, countryName, countryISOCode,
				sublocation, worldRegion, worldRegionCode, longitude, latitude,
				altitude, plusCode);

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
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException {
		// do nothing
	}

}
