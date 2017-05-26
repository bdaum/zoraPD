package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset exhibit
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Exhibit_typeImpl extends AomObject implements Exhibit_type {

	static final long serialVersionUID = -1159408446L;

	/* ----- Constructors ----- */

	public Exhibit_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param title - Property
	 * @param description - Property
	 * @param credits - Property
	 * @param date - Property
	 * @param x - Property
	 * @param y - Property
	 * @param width - Property
	 * @param height - Property
	 * @param matWidth - Property
	 * @param matColor - Property
	 * @param frameWidth - Property
	 * @param frameColor - Property
	 * @param sold - Property
	 * @param hideLabel - Property
	 * @param labelAlignment - Property
	 * @param labelDistance - Property
	 * @param labelIndent - Property
	 */
	public Exhibit_typeImpl(String title, String description, String credits,
			String date, int x, int y, int width, int height, Integer matWidth,
			Rgb_type matColor, Integer frameWidth, Rgb_type frameColor,
			boolean sold, Boolean hideLabel, Integer labelAlignment,
			Integer labelDistance, Integer labelIndent) {
		super();
		this.title = title;
		this.description = description;
		this.credits = credits;
		this.date = date;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.matWidth = matWidth;
		this.matColor = matColor;
		this.frameWidth = frameWidth;
		this.frameColor = frameColor;
		this.sold = sold;
		this.hideLabel = hideLabel;
		this.labelAlignment = labelAlignment;
		this.labelDistance = labelDistance;
		this.labelIndent = labelIndent;

	}

	/* ----- Fields ----- */

	/* *** Property title *** */

	private String title = AomConstants.INIT_String;

	/**
	 * Set value of property title
	 *
	 * @param _value - new field value
	 */
	public void setTitle(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "title"));
		title = _value;
	}

	/**
	 * Get value of property title
	 *
	 * @return - value of field title
	 */
	public String getTitle() {
		return title;
	}

	/* *** Property description *** */

	private String description = AomConstants.INIT_String;

	/**
	 * Set value of property description
	 *
	 * @param _value - new field value
	 */
	public void setDescription(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "description"));
		description = _value;
	}

	/**
	 * Get value of property description
	 *
	 * @return - value of field description
	 */
	public String getDescription() {
		return description;
	}

	/* *** Property credits *** */

	private String credits = AomConstants.INIT_String;

	/**
	 * Set value of property credits
	 *
	 * @param _value - new field value
	 */
	public void setCredits(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "credits"));
		credits = _value;
	}

	/**
	 * Get value of property credits
	 *
	 * @return - value of field credits
	 */
	public String getCredits() {
		return credits;
	}

	/* *** Property date *** */

	private String date = AomConstants.INIT_String;

	/**
	 * Set value of property date
	 *
	 * @param _value - new field value
	 */
	public void setDate(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "date"));
		date = _value;
	}

	/**
	 * Get value of property date
	 *
	 * @return - value of field date
	 */
	public String getDate() {
		return date;
	}

	/* *** Property x(unit=mm) *** */

	public static final String x__unit = "mm";

	private int x;

	/**
	 * Set value of property x
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setX(int _value) {
		x = _value;
	}

	/**
	 * Get value of property x
	 *
	 * @return - value of field x(unit=mm)
	 */
	public int getX() {
		return x;
	}

	/* *** Property y(unit=mm) *** */

	public static final String y__unit = "mm";

	private int y;

	/**
	 * Set value of property y
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setY(int _value) {
		y = _value;
	}

	/**
	 * Get value of property y
	 *
	 * @return - value of field y(unit=mm)
	 */
	public int getY() {
		return y;
	}

	/* *** Property width(unit=mm) *** */

	public static final String width__unit = "mm";

	private int width;

	/**
	 * Set value of property width
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setWidth(int _value) {
		width = _value;
	}

	/**
	 * Get value of property width
	 *
	 * @return - value of field width(unit=mm)
	 */
	public int getWidth() {
		return width;
	}

	/* *** Property height(unit=mm) *** */

	public static final String height__unit = "mm";

	private int height;

	/**
	 * Set value of property height
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setHeight(int _value) {
		height = _value;
	}

	/**
	 * Get value of property height
	 *
	 * @return - value of field height(unit=mm)
	 */
	public int getHeight() {
		return height;
	}

	/* *** Property matWidth(unit=mm) *** */

	public static final String matWidth__unit = "mm";

	private Integer matWidth;

	/**
	 * Set value of property matWidth
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setMatWidth(Integer _value) {
		matWidth = _value;
	}

	/**
	 * Get value of property matWidth
	 *
	 * @return - value of field matWidth(unit=mm)
	 */
	public Integer getMatWidth() {
		return matWidth;
	}

	/* *** Property matColor *** */

	private Rgb_type matColor;

	/**
	 * Set value of property matColor
	 *
	 * @param _value - new field value
	 */
	public void setMatColor(Rgb_type _value) {
		matColor = _value;
	}

	/**
	 * Get value of property matColor
	 *
	 * @return - value of field matColor
	 */
	public Rgb_type getMatColor() {
		return matColor;
	}

	/* *** Property frameWidth(unit=mm) *** */

	public static final String frameWidth__unit = "mm";

	private Integer frameWidth;

	/**
	 * Set value of property frameWidth
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setFrameWidth(Integer _value) {
		frameWidth = _value;
	}

	/**
	 * Get value of property frameWidth
	 *
	 * @return - value of field frameWidth(unit=mm)
	 */
	public Integer getFrameWidth() {
		return frameWidth;
	}

	/* *** Property frameColor *** */

	private Rgb_type frameColor;

	/**
	 * Set value of property frameColor
	 *
	 * @param _value - new field value
	 */
	public void setFrameColor(Rgb_type _value) {
		frameColor = _value;
	}

	/**
	 * Get value of property frameColor
	 *
	 * @return - value of field frameColor
	 */
	public Rgb_type getFrameColor() {
		return frameColor;
	}

	/* *** Property sold *** */

	private boolean sold;

	/**
	 * Set value of property sold
	 *
	 * @param _value - new field value
	 */
	public void setSold(boolean _value) {
		sold = _value;
	}

	/**
	 * Get value of property sold
	 *
	 * @return - value of field sold
	 */
	public boolean getSold() {
		return sold;
	}

	/* *** Property hideLabel *** */

	private Boolean hideLabel;

	/**
	 * Set value of property hideLabel
	 *
	 * @param _value - new field value
	 */
	public void setHideLabel(Boolean _value) {
		hideLabel = _value;
	}

	/**
	 * Get value of property hideLabel
	 *
	 * @return - value of field hideLabel
	 */
	public Boolean getHideLabel() {
		return hideLabel;
	}

	/* *** Property labelAlignment *** */

	private Integer labelAlignment;

	/**
	 * Set value of property labelAlignment
	 *
	 * @param _value - new field value
	 */
	public void setLabelAlignment(Integer _value) {
		labelAlignment = _value;
	}

	/**
	 * Get value of property labelAlignment
	 *
	 * @return - value of field labelAlignment
	 */
	public Integer getLabelAlignment() {
		return labelAlignment;
	}

	/* *** Property labelDistance(unit=mm) *** */

	public static final String labelDistance__unit = "mm";

	private Integer labelDistance;

	/**
	 * Set value of property labelDistance
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setLabelDistance(Integer _value) {
		labelDistance = _value;
	}

	/**
	 * Get value of property labelDistance
	 *
	 * @return - value of field labelDistance(unit=mm)
	 */
	public Integer getLabelDistance() {
		return labelDistance;
	}

	/* *** Property labelIndent(unit=mm) *** */

	public static final String labelIndent__unit = "mm";

	private Integer labelIndent;

	/**
	 * Set value of property labelIndent
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setLabelIndent(Integer _value) {
		labelIndent = _value;
	}

	/**
	 * Get value of property labelIndent
	 *
	 * @return - value of field labelIndent(unit=mm)
	 */
	public Integer getLabelIndent() {
		return labelIndent;
	}

	/* ----- Equality ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof Exhibit_type) || !super.equals(o))
			return false;
		Exhibit_type other = (Exhibit_type) o;
		return ((getTitle() == null && other.getTitle() == null) || (getTitle() != null && getTitle()
				.equals(other.getTitle())))

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& ((getCredits() == null && other.getCredits() == null) || (getCredits() != null && getCredits()
						.equals(other.getCredits())))

				&& ((getDate() == null && other.getDate() == null) || (getDate() != null && getDate()
						.equals(other.getDate())))

				&& getX() == other.getX()

				&& getY() == other.getY()

				&& getWidth() == other.getWidth()

				&& getHeight() == other.getHeight()

				&& ((getMatWidth() == null && other.getMatWidth() == null) || (getMatWidth() != null && getMatWidth()
						.equals(other.getMatWidth())))

				&& ((getMatColor() == null && other.getMatColor() == null) || (getMatColor() != null && getMatColor()
						.equals(other.getMatColor())))

				&& ((getFrameWidth() == null && other.getFrameWidth() == null) || (getFrameWidth() != null && getFrameWidth()
						.equals(other.getFrameWidth())))

				&& ((getFrameColor() == null && other.getFrameColor() == null) || (getFrameColor() != null && getFrameColor()
						.equals(other.getFrameColor())))

				&& getSold() == other.getSold()

				&& ((getHideLabel() == null && other.getHideLabel() == null) || (getHideLabel() != null && getHideLabel()
						.equals(other.getHideLabel())))

				&& ((getLabelAlignment() == null && other.getLabelAlignment() == null) || (getLabelAlignment() != null && getLabelAlignment()
						.equals(other.getLabelAlignment())))

				&& ((getLabelDistance() == null && other.getLabelDistance() == null) || (getLabelDistance() != null && getLabelDistance()
						.equals(other.getLabelDistance())))

				&& ((getLabelIndent() == null && other.getLabelIndent() == null) || (getLabelIndent() != null && getLabelIndent()
						.equals(other.getLabelIndent())))

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

		int hashCode = 1374082432 + ((getTitle() == null) ? 0 : getTitle()
				.hashCode());

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode
				+ ((getCredits() == null) ? 0 : getCredits().hashCode());

		hashCode = 31 * hashCode
				+ ((getDate() == null) ? 0 : getDate().hashCode());

		hashCode = 31 * hashCode + getX();

		hashCode = 31 * hashCode + getY();

		hashCode = 31 * hashCode + getWidth();

		hashCode = 31 * hashCode + getHeight();

		hashCode = 31 * hashCode
				+ ((getMatWidth() == null) ? 0 : getMatWidth().hashCode());

		hashCode = 31 * hashCode
				+ ((getMatColor() == null) ? 0 : getMatColor().hashCode());

		hashCode = 31 * hashCode
				+ ((getFrameWidth() == null) ? 0 : getFrameWidth().hashCode());

		hashCode = 31 * hashCode
				+ ((getFrameColor() == null) ? 0 : getFrameColor().hashCode());

		hashCode = 31 * hashCode + (getSold() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getHideLabel() == null) ? 0 : getHideLabel().hashCode());

		hashCode = 31
				* hashCode
				+ ((getLabelAlignment() == null) ? 0 : getLabelAlignment()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getLabelDistance() == null) ? 0 : getLabelDistance()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getLabelIndent() == null) ? 0 : getLabelIndent().hashCode());

		return hashCode;
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

		if (title == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "title"));

		if (description == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "description"));

		if (credits == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "credits"));

		if (date == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "date"));

	}

}
