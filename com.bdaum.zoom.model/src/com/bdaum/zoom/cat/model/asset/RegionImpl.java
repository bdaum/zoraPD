package com.bdaum.zoom.cat.model.asset;

import com.bdaum.zoom.cat.model.Region_typeImpl;
import java.util.*;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset region
 *
 * StringID is the hexadecimal representation of 4 16-bit parts (x,y,width,height). Each part is called to the maximum unsigned binary 16-bit number.
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class RegionImpl extends Region_typeImpl implements Region {

	static final long serialVersionUID = -1530231328L;

	/* ----- Constructors ----- */

	public RegionImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param keywordAdded - Property
	 * @param personEmailDigest - Property
	 * @param personLiveCID - Property
	 * @param description - Property
	 * @param type - Property
	 * @param album - Arc
	 */
	public RegionImpl(boolean keywordAdded, String personEmailDigest,
			Long personLiveCID, String description, String type, String album) {
		super(keywordAdded, personEmailDigest, personLiveCID, description, type);
		this.album = album;

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
		attachInstrumentation(_instrumentation, RegionImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc asset_person_parent *** */

	private String asset_person_parent;

	/**
	 * Set value of property asset_person_parent
	 *
	 * @param _value - new field value
	 */
	public void setAsset_person_parent(String _value) {
		asset_person_parent = _value;
	}

	/**
	 * Get value of property asset_person_parent
	 *
	 * @return - value of field asset_person_parent
	 */
	public String getAsset_person_parent() {
		return asset_person_parent;
	}

	/* *** Arc album *** */

	private String album;

	/**
	 * Set value of property album
	 *
	 * @param _value - new field value
	 */
	public void setAlbum(String _value) {
		album = _value;
	}

	/**
	 * Get value of property album
	 *
	 * @return - value of field album
	 */
	public String getAlbum() {
		return album;
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
