package com.bdaum.zoom.cat.model.group.webGallery;

import java.util.*;
import com.bdaum.aoModeling.runtime.*;
import com.bdaum.zoom.cat.model.Storyboard_typeImpl;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset storyboard
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class StoryboardImpl extends Storyboard_typeImpl implements Storyboard {

	static final long serialVersionUID = 1269529622L;

	/* ----- Constructors ----- */

	public StoryboardImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param title - Property
	 * @param sequenceNo - Property
	 * @param htmlDescription - Property
	 * @param description - Property
	 * @param imageSize - Property
	 * @param enlargeSmall - Property
	 * @param showCaptions - Property
	 * @param showDescriptions - Property
	 * @param showExif - Property
	 */
	public StoryboardImpl(String title, int sequenceNo,
			boolean htmlDescription, String description, int imageSize,
			boolean enlargeSmall, boolean showCaptions,
			boolean showDescriptions, boolean showExif) {
		super(title, sequenceNo, htmlDescription, description, imageSize,
				enlargeSmall, showCaptions, showDescriptions, showExif);

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
		attachInstrumentation(_instrumentation, StoryboardImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc webGallery_storyboard_parent *** */

	private WebGallery webGallery_storyboard_parent;

	/**
	 * Set value of property webGallery_storyboard_parent
	 *
	 * @param _value - new field value
	 */
	public void setWebGallery_storyboard_parent(WebGallery _value) {
		webGallery_storyboard_parent = _value;
	}

	/**
	 * Get value of property webGallery_storyboard_parent
	 *
	 * @return - value of field webGallery_storyboard_parent
	 */
	public WebGallery getWebGallery_storyboard_parent() {
		return webGallery_storyboard_parent;
	}

	/* *** Arc exhibit *** */

	private AomList<String> exhibit = new FastArrayList<String>("exhibit",
			PackageInterface.Storyboard_exhibit, 0, Integer.MAX_VALUE, null,
			null);

	/**
	 * Set value of property exhibit
	 *
	 * @param _value - new element value
	 */
	public void setExhibit(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "exhibit"));
		exhibit = _value;
	}

	/**
	 * Set value of property exhibit
	 *
	 * @param _value - new element value
	 */
	public void setExhibit(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "exhibit"));
		exhibit = new FastArrayList<String>(_value, "exhibit",
				PackageInterface.Storyboard_exhibit, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Set single element of list exhibit
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setExhibit(String _element, int _i) {
		exhibit.set(_i, _element);
	}

	/**
	 * Add an element to list exhibit
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addExhibit(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Exhibit._element"));

		return exhibit.add(_element);
	}

	/**
	 * Remove an element from list exhibit
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeExhibit(String _element) {
		return exhibit.remove(_element);
	}

	/**
	 * Make exhibit empty 
	 */
	public void clearExhibit() {
		exhibit.clear();
	}

	/**
	 * Get value of property exhibit
	 *
	 * @return - value of field exhibit
	 */
	public AomList<String> getExhibit() {
		return exhibit;
	}

	/**
	 * Get single element of list exhibit
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list exhibit
	 */
	public String getExhibit(int _i) {
		return exhibit.get(_i);
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

		if (!(o instanceof Storyboard) || !super.equals(o))
			return false;
		Storyboard other = (Storyboard) o;
		return ((getExhibit() == null && other.getExhibit() == null) || (getExhibit() != null && getExhibit()
				.equals(other.getExhibit())))

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
				+ ((getExhibit() == null) ? 0 : getExhibit().hashCode());
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
