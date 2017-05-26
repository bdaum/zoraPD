package com.bdaum.zoom.cat.model.group.webGallery;

import com.bdaum.zoom.cat.model.WebExhibit_typeImpl;
import java.util.*;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webExhibit
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class WebExhibitImpl extends WebExhibit_typeImpl implements WebExhibit {

	static final long serialVersionUID = 2243076266L;

	/* ----- Constructors ----- */

	public WebExhibitImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param caption - Property
	 * @param sequenceNo - Property
	 * @param description - Property
	 * @param htmlDescription - Property
	 * @param altText - Property
	 * @param downloadable - Property
	 * @param includeMetadata - Property
	 * @param asset - Arc
	 */
	public WebExhibitImpl(String caption, int sequenceNo, String description,
			boolean htmlDescription, String altText, boolean downloadable,
			boolean includeMetadata, String asset) {
		super(caption, sequenceNo, description, htmlDescription, altText,
				downloadable, includeMetadata);
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
		attachInstrumentation(_instrumentation, WebExhibitImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc storyboard_exhibit_parent *** */

	private String storyboard_exhibit_parent;

	/**
	 * Set value of property storyboard_exhibit_parent
	 *
	 * @param _value - new field value
	 */
	public void setStoryboard_exhibit_parent(String _value) {
		storyboard_exhibit_parent = _value;
	}

	/**
	 * Get value of property storyboard_exhibit_parent
	 *
	 * @return - value of field storyboard_exhibit_parent
	 */
	public String getStoryboard_exhibit_parent() {
		return storyboard_exhibit_parent;
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

		if (!(o instanceof WebExhibit) || !super.equals(o))
			return false;
		WebExhibit other = (WebExhibit) o;
		return ((getAsset() == null && other.getAsset() == null) || (getAsset() != null && getAsset()
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

		return super.hashCode() * 31
				+ ((getAsset() == null) ? 0 : getAsset().hashCode());
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

	@Override
	public String toString() {
		return "WebExhibit: " + getCaption();
	}

}
