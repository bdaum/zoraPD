package com.bdaum.zoom.cat.model.group.slideShow;

import com.bdaum.zoom.cat.model.group.Group;
import java.util.*;
import com.bdaum.aoModeling.runtime.*;
import com.bdaum.zoom.cat.model.SlideShow_typeImpl;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset slideShow
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class SlideShowImpl extends SlideShow_typeImpl implements SlideShow {

	static final long serialVersionUID = -1772064699L;

	/* ----- Constructors ----- */

	public SlideShowImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param description - Property
	 * @param fromPreview - Property
	 * @param duration - Property
	 * @param effect - Property
	 * @param fading - Property
	 * @param titleDisplay - Property
	 * @param titleContent - Property
	 * @param adhoc - Property
	 * @param skipDublettes - Property
	 * @param voiceNotes - Property
	 * @param lastAccessDate - Property
	 * @param perspective - Property
	 */
	public SlideShowImpl(String name, String description, boolean fromPreview,
			int duration, int effect, int fading, int titleDisplay,
			int titleContent, boolean adhoc, boolean skipDublettes,
			boolean voiceNotes, Date lastAccessDate, String perspective) {
		super(name, description, fromPreview, duration, effect, fading,
				titleDisplay, titleContent, adhoc, skipDublettes, voiceNotes,
				lastAccessDate, perspective);

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
		attachInstrumentation(_instrumentation, SlideShowImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc group_slideshow_parent *** */

	private String group_slideshow_parent;

	/**
	 * Set value of property group_slideshow_parent
	 *
	 * @param _value - new field value
	 */
	public void setGroup_slideshow_parent(String _value) {
		group_slideshow_parent = _value;
	}

	/**
	 * Get value of property group_slideshow_parent
	 *
	 * @return - value of field group_slideshow_parent
	 */
	public String getGroup_slideshow_parent() {
		return group_slideshow_parent;
	}

	/* *** Arc entry *** */

	private AomList<String> entry = new FastArrayList<String>("entry",
			PackageInterface.SlideShow_entry, 0, Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property entry
	 *
	 * @param _value - new element value
	 */
	public void setEntry(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "entry"));
		entry = _value;
	}

	/**
	 * Set value of property entry
	 *
	 * @param _value - new element value
	 */
	public void setEntry(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "entry"));
		entry = new FastArrayList<String>(_value, "entry",
				PackageInterface.SlideShow_entry, 0, Integer.MAX_VALUE, null,
				null);
	}

	/**
	 * Set single element of list entry
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setEntry(String _element, int _i) {
		entry.set(_i, _element);
	}

	/**
	 * Add an element to list entry
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addEntry(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Entry._element"));

		return entry.add(_element);
	}

	/**
	 * Remove an element from list entry
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeEntry(String _element) {
		return entry.remove(_element);
	}

	/**
	 * Make entry empty 
	 */
	public void clearEntry() {
		entry.clear();
	}

	/**
	 * Get value of property entry
	 *
	 * @return - value of field entry
	 */
	public AomList<String> getEntry() {
		return entry;
	}

	/**
	 * Get single element of list entry
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list entry
	 */
	public String getEntry(int _i) {
		return entry.get(_i);
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

		if (!(o instanceof SlideShow) || !super.equals(o))
			return false;
		SlideShow other = (SlideShow) o;
		return ((getEntry() == null && other.getEntry() == null) || (getEntry() != null && getEntry()
				.equals(other.getEntry())))

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

		return super.hashCode() * 31
				+ ((getEntry() == null) ? 0 : getEntry().hashCode());
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
