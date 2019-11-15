package com.bdaum.zoom.cat.model.group.exhibition;

import com.bdaum.zoom.cat.model.group.Group;
import java.util.*;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Exhibition_typeImpl;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset exhibition
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class ExhibitionImpl extends Exhibition_typeImpl implements Exhibition {

	static final long serialVersionUID = -502455255L;

	/* ----- Constructors ----- */

	public ExhibitionImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param description - Property
	 * @param info - Property
	 * @param defaultViewingHeight - Property
	 * @param variance - Property
	 * @param gridSize - Property
	 * @param showGrid - Property
	 * @param snapToGrid - Property
	 * @param defaultDescription - Property
	 * @param labelFontFamily - Property
	 * @param labelFontSize - Property
	 * @param labelSequence - Property
	 * @param hideLabel - Property
	 * @param labelAlignment - Property
	 * @param labelDistance - Property
	 * @param labelIndent - Property
	 * @param startX - Property
	 * @param startY - Property
	 * @param matWidth - Property
	 * @param matColor - Property
	 * @param frameWidth - Property
	 * @param frameColor - Property
	 * @param groundColor - Property
	 * @param horizonColor - Property
	 * @param ceilingColor - Property
	 * @param audio - Property
	 * @param outputFolder - Property
	 * @param ftpDir - Property
	 * @param isFtp - Property
	 * @param pageName - Property
	 * @param applySharpening - Property
	 * @param radius - Property
	 * @param amount - Property
	 * @param threshold - Property
	 * @param addWatermark - Property
	 * @param contactName - Property
	 * @param email - Property
	 * @param webUrl - Property
	 * @param copyright - Property
	 * @param logo - Property
	 * @param infoPlatePosition - Property
	 * @param hideCredits - Property
	 * @param jpegQuality - Property
	 * @param scalingMethod - Property
	 * @param lastAccessDate - Property
	 * @param perspective - Property
	 * @param safety - Property
	 */
	public ExhibitionImpl(String name, String description, String info,
			int defaultViewingHeight, int variance, int gridSize,
			boolean showGrid, boolean snapToGrid, String defaultDescription,
			String labelFontFamily, int labelFontSize, int labelSequence,
			boolean hideLabel, Integer labelAlignment, Integer labelDistance,
			Integer labelIndent, int startX, int startY, int matWidth,
			Rgb_type matColor, int frameWidth, Rgb_type frameColor,
			Rgb_type groundColor, Rgb_type horizonColor, Rgb_type ceilingColor,
			String audio, String outputFolder, String ftpDir, boolean isFtp,
			String pageName, Boolean applySharpening, float radius,
			float amount, int threshold, boolean addWatermark,
			String contactName, String email, String webUrl, String copyright,
			String logo, int infoPlatePosition, boolean hideCredits,
			int jpegQuality, int scalingMethod, Date lastAccessDate,
			String perspective, int safety) {
		super(name, description, info, defaultViewingHeight, variance,
				gridSize, showGrid, snapToGrid, defaultDescription,
				labelFontFamily, labelFontSize, labelSequence, hideLabel,
				labelAlignment, labelDistance, labelIndent, startX, startY,
				matWidth, matColor, frameWidth, frameColor, groundColor,
				horizonColor, ceilingColor, audio, outputFolder, ftpDir, isFtp,
				pageName, applySharpening, radius, amount, threshold,
				addWatermark, contactName, email, webUrl, copyright, logo,
				infoPlatePosition, hideCredits, jpegQuality, scalingMethod,
				lastAccessDate, perspective, safety);

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
		attachInstrumentation(_instrumentation, ExhibitionImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc group_exhibition_parent *** */

	private String group_exhibition_parent;

	/**
	 * Set value of property group_exhibition_parent
	 *
	 * @param _value - new field value
	 */
	public void setGroup_exhibition_parent(String _value) {
		group_exhibition_parent = _value;
	}

	/**
	 * Get value of property group_exhibition_parent
	 *
	 * @return - value of field group_exhibition_parent
	 */
	public String getGroup_exhibition_parent() {
		return group_exhibition_parent;
	}

	/* *** Arc wall *** */

	private AomList<Wall> wall = new FastArrayList<Wall>("wall",
			PackageInterface.Exhibition_wall, 0, Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property wall
	 *
	 * @param _value - new element value
	 */
	public void setWall(AomList<Wall> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "wall"));
		wall = _value;
		for (Wall _element : _value) {
			if (_element != null)
				_element.setExhibition_wall_parent(this);

		}
	}

	/**
	 * Set value of property wall
	 *
	 * @param _value - new element value
	 */
	public void setWall(Collection<Wall> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "wall"));
		wall = new FastArrayList<Wall>(_value, "wall",
				PackageInterface.Exhibition_wall, 0, Integer.MAX_VALUE, null,
				null);

		for (Wall _element : _value) {
			if (_element != null)
				_element.setExhibition_wall_parent(this);
		}
	}

	/**
	 * Set single element of list wall
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setWall(Wall _element, int _i) {
		if (_element != null)
			_element.setExhibition_wall_parent(this);
		wall.set(_i, _element);
	}

	/**
	 * Add an element to list wall
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addWall(Wall _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Wall._element"));
		_element.setExhibition_wall_parent(this);

		return wall.add(_element);
	}

	/**
	 * Remove an element from list wall
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeWall(Wall _element) {
		return wall.remove(_element);
	}

	/**
	 * Make wall empty 
	 */
	public void clearWall() {
		wall.clear();
	}

	/**
	 * Get value of property wall
	 *
	 * @return - value of field wall
	 */
	public AomList<Wall> getWall() {
		return wall;
	}

	/**
	 * Get single element of list wall
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list wall
	 */
	public Wall getWall(int _i) {
		return wall.get(_i);
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

		if (!(o instanceof Exhibition) || !super.equals(o))
			return false;
		Exhibition other = (Exhibition) o;
		return ((getWall() == null && other.getWall() == null) || (getWall() != null && getWall()
				.equals(other.getWall())))

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
				+ ((getWall() == null) ? 0 : getWall().hashCode());
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
