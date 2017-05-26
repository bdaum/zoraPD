package com.bdaum.zoom.cat.model.locationShown;

import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.LocationShown_typeImpl;
import java.util.*;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset locationShown
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class LocationShownImpl extends LocationShown_typeImpl implements
		LocationShown {

	static final long serialVersionUID = -1341128796L;

	/* ----- Constructors ----- */

	public LocationShownImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param location - Arc
	 * @param asset - Arc
	 */
	public LocationShownImpl(String location, String asset) {
		super();
		this.location = location;
		this.asset = asset;

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
		attachInstrumentation(_instrumentation, LocationShownImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Arc location *** */

	private String location = AomConstants.INIT_String;

	/**
	 * Set value of property location
	 *
	 * @param _value - new field value
	 */
	public void setLocation(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "location"));
		location = _value;
	}

	/**
	 * Get value of property location
	 *
	 * @return - value of field location
	 */
	public String getLocation() {
		return location;
	}

	/* *** Arc asset *** */

	private String asset = AomConstants.INIT_String;

	/**
	 * Set value of property asset
	 *
	 * @param _value - new field value
	 */
	public void setAsset(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));
		asset = _value;
	}

	/**
	 * Get value of property asset
	 *
	 * @return - value of field asset
	 */
	public String getAsset() {
		return asset;
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

		if (!(o instanceof LocationShown) || !super.equals(o))
			return false;
		LocationShown other = (LocationShown) o;
		return ((getLocation() == null && other.getLocation() == null) || (getLocation() != null && getLocation()
				.equals(other.getLocation())))

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
				+ ((getLocation() == null) ? 0 : getLocation().hashCode());

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

		if (location == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "location"));

		if (asset == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));

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
