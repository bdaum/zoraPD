package com.bdaum.zoom.cat.model.asset;

import java.util.*;
import com.bdaum.zoom.cat.model.TrackRecord_typeImpl;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset trackRecord
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class TrackRecordImpl extends TrackRecord_typeImpl implements
		TrackRecord {

	static final long serialVersionUID = -319906776L;

	/* ----- Constructors ----- */

	public TrackRecordImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param type - Property
	 * @param serviceId - Property
	 * @param serviceName - Property
	 * @param target - Property
	 * @param derivative - Property
	 * @param exportDate - Property
	 * @param replaced - Property
	 * @param visit - Property
	 */
	public TrackRecordImpl(String type, String serviceId, String serviceName,
			String target, String derivative, Date exportDate,
			boolean replaced, String visit) {
		super(type, serviceId, serviceName, target, derivative, exportDate,
				replaced, visit);

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
		attachInstrumentation(_instrumentation, TrackRecordImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc asset_track_parent *** */

	private String asset_track_parent;

	/**
	 * Set value of property asset_track_parent
	 *
	 * @param _value - new field value
	 */
	public void setAsset_track_parent(String _value) {
		asset_track_parent = _value;
	}

	/**
	 * Get value of property asset_track_parent
	 *
	 * @return - value of field asset_track_parent
	 */
	public String getAsset_track_parent() {
		return asset_track_parent;
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

	@Override
	public String toString() {
		return getStringId();
	}

}
