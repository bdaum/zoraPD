package com.bdaum.zoom.cat.model.group.webGallery;

import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.WebGallery_typeImpl;
import java.util.*;
import com.bdaum.zoom.cat.model.Font_type;
import java.lang.String;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webGallery
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class WebGalleryImpl extends WebGallery_typeImpl implements WebGallery {

	static final long serialVersionUID = -202578973L;

	/* ----- Constructors ----- */

	public WebGalleryImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param template - Property
	 * @param name - Property
	 * @param logo - Property
	 * @param htmlDescription - Property
	 * @param description - Property
	 * @param hideHeader - Property
	 * @param opacity - Property
	 * @param padding - Property
	 * @param thumbSize - Property
	 * @param downloadText - Property
	 * @param hideDownload - Property
	 * @param copyright - Property
	 * @param addWatermark - Property
	 * @param showMeta - Property
	 * @param contactName - Property
	 * @param email - Property
	 * @param webUrl - Property
	 * @param hideFooter - Property
	 * @param bgImage - Property
	 * @param bgRepeat - Property
	 * @param bgColor - Property
	 * @param shadeColor - Property
	 * @param borderColor - Property
	 * @param linkColor - Property
	 * @param titleFont - Property
	 * @param sectionFont - Property
	 * @param captionFont - Property
	 * @param descriptionFont - Property
	 * @param footerFont - Property
	 * @param controlsFont - Property
	 * @param selectedEngine - Property
	 * @param outputFolder - Property
	 * @param ftpDir - Property
	 * @param isFtp - Property
	 * @param pageName - Property
	 * @param poweredByText - Property
	 * @param applySharpening - Property
	 * @param radius - Property
	 * @param amount - Property
	 * @param threshold - Property
	 * @param headHtml - Property
	 * @param topHtml - Property
	 * @param footerHtml - Property
	 * @param jpegQuality - Property
	 * @param scalingMethod - Property
	 * @param lastAccessDate - Property
	 * @param perspective - Property
	 */
	public WebGalleryImpl(boolean template, String name, String logo,
			boolean htmlDescription, String description, boolean hideHeader,
			int opacity, int padding, int thumbSize, String downloadText,
			boolean hideDownload, String copyright, boolean addWatermark,
			boolean showMeta, String contactName, String email, String webUrl,
			boolean hideFooter, String bgImage, boolean bgRepeat,
			Rgb_type bgColor, Rgb_type shadeColor, Rgb_type borderColor,
			Rgb_type linkColor, Font_type titleFont, Font_type sectionFont,
			Font_type captionFont, Font_type descriptionFont,
			Font_type footerFont, Font_type controlsFont,
			String selectedEngine, String outputFolder, String ftpDir,
			boolean isFtp, String pageName, String poweredByText,
			boolean applySharpening, float radius, float amount, int threshold,
			String headHtml, String topHtml, String footerHtml,
			int jpegQuality, int scalingMethod, Date lastAccessDate,
			String perspective) {
		super(template, name, logo, htmlDescription, description, hideHeader,
				opacity, padding, thumbSize, downloadText, hideDownload,
				copyright, addWatermark, showMeta, contactName, email, webUrl,
				hideFooter, bgImage, bgRepeat, bgColor, shadeColor,
				borderColor, linkColor, titleFont, sectionFont, captionFont,
				descriptionFont, footerFont, controlsFont, selectedEngine,
				outputFolder, ftpDir, isFtp, pageName, poweredByText,
				applySharpening, radius, amount, threshold, headHtml, topHtml,
				footerHtml, jpegQuality, scalingMethod, lastAccessDate,
				perspective);

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
		attachInstrumentation(_instrumentation, WebGalleryImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc group_webGallery_parent *** */

	private String group_webGallery_parent;

	/**
	 * Set value of property group_webGallery_parent
	 *
	 * @param _value - new field value
	 */
	public void setGroup_webGallery_parent(String _value) {
		group_webGallery_parent = _value;
	}

	/**
	 * Get value of property group_webGallery_parent
	 *
	 * @return - value of field group_webGallery_parent
	 */
	public String getGroup_webGallery_parent() {
		return group_webGallery_parent;
	}

	/* *** Arc storyboard *** */

	private AomList<Storyboard> storyboard = new FastArrayList<Storyboard>(
			"storyboard", PackageInterface.WebGallery_storyboard, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property storyboard
	 *
	 * @param _value - new element value
	 */
	public void setStoryboard(AomList<Storyboard> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "storyboard"));
		storyboard = _value;
		for (Storyboard _element : _value) {
			if (_element != null)
				_element.setWebGallery_storyboard_parent(this);

		}
	}

	/**
	 * Set value of property storyboard
	 *
	 * @param _value - new element value
	 */
	public void setStoryboard(Collection<Storyboard> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "storyboard"));
		storyboard = new FastArrayList<Storyboard>(_value, "storyboard",
				PackageInterface.WebGallery_storyboard, 0, Integer.MAX_VALUE,
				null, null);

		for (Storyboard _element : _value) {
			if (_element != null)
				_element.setWebGallery_storyboard_parent(this);
		}
	}

	/**
	 * Set single element of list storyboard
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setStoryboard(Storyboard _element, int _i) {
		if (_element != null)
			_element.setWebGallery_storyboard_parent(this);
		storyboard.set(_i, _element);
	}

	/**
	 * Add an element to list storyboard
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addStoryboard(Storyboard _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Storyboard._element"));
		_element.setWebGallery_storyboard_parent(this);

		return storyboard.add(_element);
	}

	/**
	 * Remove an element from list storyboard
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeStoryboard(Storyboard _element) {
		return storyboard.remove(_element);
	}

	/**
	 * Make storyboard empty 
	 */
	public void clearStoryboard() {
		storyboard.clear();
	}

	/**
	 * Get value of property storyboard
	 *
	 * @return - value of field storyboard
	 */
	public AomList<Storyboard> getStoryboard() {
		return storyboard;
	}

	/**
	 * Get single element of list storyboard
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list storyboard
	 */
	public Storyboard getStoryboard(int _i) {
		return storyboard.get(_i);
	}

	/* *** Arc parameter *** */

	private AomMap<String, WebParameter> parameter = new FastHashMap<String, WebParameter>(
			"parameter", PackageInterface.WebGallery_parameter, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property parameter
	 *
	 * @param _value - new element value
	 */
	public void setParameter(AomMap<String, WebParameter> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "parameter"));
		parameter = _value;
		for (WebParameter _element : _value.values()) {
			if (_element != null)
				_element.setWebGallery_parameter_parent(this);

		}
	}

	/**
	 * Set value of property parameter
	 *
	 * @param _value - new element value
	 */
	public void setParameter(Map<String, WebParameter> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "parameter"));
		parameter = new FastHashMap<String, WebParameter>(_value, "parameter",
				PackageInterface.WebGallery_parameter, 0, Integer.MAX_VALUE,
				null, null);

		for (WebParameter _element : _value.values()) {
			if (_element != null)
				_element.setWebGallery_parameter_parent(this);
		}
	}

	/**
	 * Add an element to map parameter under key _element.getId()
	 *
	 * @param _element - the element to add
	 * @return - the previous element stored under that key
	 */
	public WebParameter putParameter(WebParameter _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Parameter._element"));
		_element.setWebGallery_parameter_parent(this);

		return (WebParameter) parameter.put(_element.getId(), _element);
	}

	/**
	 * Remove an element from map parameter
	 *
	 * @param _key - the key of the element to remove
	 * @return - the previous element stored under that key
	 */
	public WebParameter removeParameter(String _key) {
		return (WebParameter) parameter.remove(_key);
	}

	/**
	 * Make parameter empty 
	 */
	public void clearParameter() {
		parameter.clear();
	}

	/**
	 * Get value of property parameter
	 *
	 * @return - value of field parameter
	 */
	public AomMap<String, WebParameter> getParameter() {
		return parameter;
	}

	/**
	 * Get single element of map parameter
	 *
	 * @param _key - the key of the element
	 * @return - the element belonging to the specified key
	 */
	public WebParameter getParameter(String _key) {
		return parameter.get(_key);
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

		if (!(o instanceof WebGallery) || !super.equals(o))
			return false;
		WebGallery other = (WebGallery) o;
		return ((getStoryboard() == null && other.getStoryboard() == null) || (getStoryboard() != null && getStoryboard()
				.equals(other.getStoryboard())))

				&& ((getParameter() == null && other.getParameter() == null) || (getParameter() != null && getParameter()
						.equals(other.getParameter())))

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
				+ ((getStoryboard() == null) ? 0 : getStoryboard().hashCode());

		hashCode = 31 * hashCode
				+ ((getParameter() == null) ? 0 : getParameter().hashCode());

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
