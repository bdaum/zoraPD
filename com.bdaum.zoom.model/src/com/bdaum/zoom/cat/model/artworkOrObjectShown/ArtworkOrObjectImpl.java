package com.bdaum.zoom.cat.model.artworkOrObjectShown;

import com.bdaum.zoom.cat.model.ArtworkOrObject_typeImpl;
import java.util.*;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset artworkOrObject
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class ArtworkOrObjectImpl extends ArtworkOrObject_typeImpl implements
		ArtworkOrObject {

	static final long serialVersionUID = -1295715775L;

	/* ----- Constructors ----- */

	public ArtworkOrObjectImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param copyrightNotice - Property
	 * @param dateCreated - Property
	 * @param source - Property
	 * @param sourceInventoryNumber - Property
	 * @param title - Property
	 */
	public ArtworkOrObjectImpl(String copyrightNotice, Date dateCreated,
			String source, String sourceInventoryNumber, String title) {
		super(copyrightNotice, dateCreated, source, sourceInventoryNumber,
				title);

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
		attachInstrumentation(_instrumentation, ArtworkOrObjectImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc artworkOrObjectShown_parent *** */

	private AomList<String> artworkOrObjectShown_parent = new FastArrayList<String>(
			"artworkOrObjectShown_parent",
			PackageInterface.ArtworkOrObject_artworkOrObjectShown_parent, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property artworkOrObjectShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setArtworkOrObjectShown_parent(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"artworkOrObjectShown_parent"));
		artworkOrObjectShown_parent = _value;
	}

	/**
	 * Set value of property artworkOrObjectShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setArtworkOrObjectShown_parent(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"artworkOrObjectShown_parent"));
		artworkOrObjectShown_parent = new FastArrayList<String>(_value,
				"artworkOrObjectShown_parent",
				PackageInterface.ArtworkOrObject_artworkOrObjectShown_parent,
				0, Integer.MAX_VALUE, null, null);
	}

	/**
	 * Set single element of list artworkOrObjectShown_parent
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setArtworkOrObjectShown_parent(String _element, int _i) {
		artworkOrObjectShown_parent.set(_i, _element);
	}

	/**
	 * Add an element to list artworkOrObjectShown_parent
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addArtworkOrObjectShown_parent(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"ArtworkOrObjectShown_parent._element"));

		return artworkOrObjectShown_parent.add(_element);
	}

	/**
	 * Remove an element from list artworkOrObjectShown_parent
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeArtworkOrObjectShown_parent(String _element) {
		return artworkOrObjectShown_parent.remove(_element);
	}

	/**
	 * Make artworkOrObjectShown_parent empty 
	 */
	public void clearArtworkOrObjectShown_parent() {
		artworkOrObjectShown_parent.clear();
	}

	/**
	 * Get value of property artworkOrObjectShown_parent
	 *
	 * @return - value of field artworkOrObjectShown_parent
	 */
	public AomList<String> getArtworkOrObjectShown_parent() {
		return artworkOrObjectShown_parent;
	}

	/**
	 * Get single element of list artworkOrObjectShown_parent
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list artworkOrObjectShown_parent
	 */
	public String getArtworkOrObjectShown_parent(int _i) {
		return artworkOrObjectShown_parent.get(_i);
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
