package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset PageLayout
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class PageLayout_typeImpl extends AomObject implements PageLayout_type {

	static final long serialVersionUID = -903510654L;

	/* ----- Constructors ----- */

	public PageLayout_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param type - Property
	 * @param title - Property
	 * @param subtitle - Property
	 * @param footer - Property
	 * @param size - Property
	 * @param columns - Property
	 * @param leftMargin - Property
	 * @param rightMargin - Property
	 * @param horizontalGap - Property
	 * @param topMargin - Property
	 * @param bottomMargin - Property
	 * @param verticalGap - Property
	 * @param caption1 - Property
	 * @param caption2 - Property
	 * @param alt - Property
	 * @param keyLine - Property
	 * @param landscape - Property
	 * @param facingPages - Property
	 * @param format - Property
	 * @param applySharpening - Property
	 * @param radius - Property
	 * @param amount - Property
	 * @param threshold - Property
	 * @param jpegQuality - Property
	 */
	public PageLayout_typeImpl(String name, int type, String title,
			String subtitle, String footer, int size, int columns,
			int leftMargin, int rightMargin, int horizontalGap, int topMargin,
			int bottomMargin, int verticalGap, String caption1,
			String caption2, String alt, int keyLine, boolean landscape,
			boolean facingPages, String format, boolean applySharpening,
			float radius, float amount, int threshold, int jpegQuality) {
		super();
		this.name = name;
		this.type = type;
		this.title = title;
		this.subtitle = subtitle;
		this.footer = footer;
		this.size = size;
		this.columns = columns;
		this.leftMargin = leftMargin;
		this.rightMargin = rightMargin;
		this.horizontalGap = horizontalGap;
		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;
		this.verticalGap = verticalGap;
		this.caption1 = caption1;
		this.caption2 = caption2;
		this.alt = alt;
		this.keyLine = keyLine;
		this.landscape = landscape;
		this.facingPages = facingPages;
		this.format = format;
		this.applySharpening = applySharpening;
		this.radius = radius;
		this.amount = amount;
		this.threshold = threshold;
		this.jpegQuality = jpegQuality;

	}

	/* ----- Fields ----- */

	/* *** Property name *** */

	private String name;

	/**
	 * Set value of property name
	 *
	 * @param _value - new field value
	 */
	public void setName(String _value) {
		name = _value;
	}

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName() {
		return name;
	}

	/* *** Property type *** */

	private int type;

	/**
	 * Set value of property type
	 *
	 * @param _value - new field value
	 */
	public void setType(int _value) {
		type = _value;
	}

	/**
	 * Get value of property type
	 *
	 * @return - value of field type
	 */
	public int getType() {
		return type;
	}

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

	/* *** Property subtitle *** */

	private String subtitle = AomConstants.INIT_String;

	/**
	 * Set value of property subtitle
	 *
	 * @param _value - new field value
	 */
	public void setSubtitle(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subtitle"));
		subtitle = _value;
	}

	/**
	 * Get value of property subtitle
	 *
	 * @return - value of field subtitle
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/* *** Property footer *** */

	private String footer = AomConstants.INIT_String;

	/**
	 * Set value of property footer
	 *
	 * @param _value - new field value
	 */
	public void setFooter(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "footer"));
		footer = _value;
	}

	/**
	 * Get value of property footer
	 *
	 * @return - value of field footer
	 */
	public String getFooter() {
		return footer;
	}

	/* *** Property size *** */

	private int size;

	/**
	 * Set value of property size
	 *
	 * @param _value - new field value
	 */
	public void setSize(int _value) {
		size = _value;
	}

	/**
	 * Get value of property size
	 *
	 * @return - value of field size
	 */
	public int getSize() {
		return size;
	}

	/* *** Property columns *** */

	private int columns;

	/**
	 * Set value of property columns
	 *
	 * @param _value - new field value
	 */
	public void setColumns(int _value) {
		columns = _value;
	}

	/**
	 * Get value of property columns
	 *
	 * @return - value of field columns
	 */
	public int getColumns() {
		return columns;
	}

	/* *** Property leftMargin(unit=mm) *** */

	public static final String leftMargin__unit = "mm";

	private int leftMargin;

	/**
	 * Set value of property leftMargin
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setLeftMargin(int _value) {
		leftMargin = _value;
	}

	/**
	 * Get value of property leftMargin
	 *
	 * @return - value of field leftMargin(unit=mm)
	 */
	public int getLeftMargin() {
		return leftMargin;
	}

	/* *** Property rightMargin(unit=mm) *** */

	public static final String rightMargin__unit = "mm";

	private int rightMargin;

	/**
	 * Set value of property rightMargin
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setRightMargin(int _value) {
		rightMargin = _value;
	}

	/**
	 * Get value of property rightMargin
	 *
	 * @return - value of field rightMargin(unit=mm)
	 */
	public int getRightMargin() {
		return rightMargin;
	}

	/* *** Property horizontalGap(unit=mm) *** */

	public static final String horizontalGap__unit = "mm";

	private int horizontalGap;

	/**
	 * Set value of property horizontalGap
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setHorizontalGap(int _value) {
		horizontalGap = _value;
	}

	/**
	 * Get value of property horizontalGap
	 *
	 * @return - value of field horizontalGap(unit=mm)
	 */
	public int getHorizontalGap() {
		return horizontalGap;
	}

	/* *** Property topMargin(unit=mm) *** */

	public static final String topMargin__unit = "mm";

	private int topMargin;

	/**
	 * Set value of property topMargin
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setTopMargin(int _value) {
		topMargin = _value;
	}

	/**
	 * Get value of property topMargin
	 *
	 * @return - value of field topMargin(unit=mm)
	 */
	public int getTopMargin() {
		return topMargin;
	}

	/* *** Property bottomMargin(unit=mm) *** */

	public static final String bottomMargin__unit = "mm";

	private int bottomMargin;

	/**
	 * Set value of property bottomMargin
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setBottomMargin(int _value) {
		bottomMargin = _value;
	}

	/**
	 * Get value of property bottomMargin
	 *
	 * @return - value of field bottomMargin(unit=mm)
	 */
	public int getBottomMargin() {
		return bottomMargin;
	}

	/* *** Property verticalGap(unit=mm) *** */

	public static final String verticalGap__unit = "mm";

	private int verticalGap;

	/**
	 * Set value of property verticalGap
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setVerticalGap(int _value) {
		verticalGap = _value;
	}

	/**
	 * Get value of property verticalGap
	 *
	 * @return - value of field verticalGap(unit=mm)
	 */
	public int getVerticalGap() {
		return verticalGap;
	}

	/* *** Property caption1 *** */

	private String caption1 = AomConstants.INIT_String;

	/**
	 * Set value of property caption1
	 *
	 * @param _value - new field value
	 */
	public void setCaption1(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "caption1"));
		caption1 = _value;
	}

	/**
	 * Get value of property caption1
	 *
	 * @return - value of field caption1
	 */
	public String getCaption1() {
		return caption1;
	}

	/* *** Property caption2 *** */

	private String caption2 = AomConstants.INIT_String;

	/**
	 * Set value of property caption2
	 *
	 * @param _value - new field value
	 */
	public void setCaption2(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "caption2"));
		caption2 = _value;
	}

	/**
	 * Get value of property caption2
	 *
	 * @return - value of field caption2
	 */
	public String getCaption2() {
		return caption2;
	}

	/* *** Property alt *** */

	private String alt = AomConstants.INIT_String;

	/**
	 * Set value of property alt
	 *
	 * @param _value - new field value
	 */
	public void setAlt(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "alt"));
		alt = _value;
	}

	/**
	 * Get value of property alt
	 *
	 * @return - value of field alt
	 */
	public String getAlt() {
		return alt;
	}

	/* *** Property keyLine *** */

	private int keyLine;

	/**
	 * Set value of property keyLine
	 *
	 * @param _value - new field value
	 */
	public void setKeyLine(int _value) {
		keyLine = _value;
	}

	/**
	 * Get value of property keyLine
	 *
	 * @return - value of field keyLine
	 */
	public int getKeyLine() {
		return keyLine;
	}

	/* *** Property landscape *** */

	private boolean landscape;

	/**
	 * Set value of property landscape
	 *
	 * @param _value - new field value
	 */
	public void setLandscape(boolean _value) {
		landscape = _value;
	}

	/**
	 * Get value of property landscape
	 *
	 * @return - value of field landscape
	 */
	public boolean getLandscape() {
		return landscape;
	}

	/* *** Property facingPages *** */

	private boolean facingPages;

	/**
	 * Set value of property facingPages
	 *
	 * @param _value - new field value
	 */
	public void setFacingPages(boolean _value) {
		facingPages = _value;
	}

	/**
	 * Get value of property facingPages
	 *
	 * @return - value of field facingPages
	 */
	public boolean getFacingPages() {
		return facingPages;
	}

	/* *** Property format *** */

	private String format = AomConstants.INIT_String;

	/**
	 * Set value of property format
	 *
	 * @param _value - new field value
	 */
	public void setFormat(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "format"));
		String _valueIntern = _value.intern();
		if (!(_valueIntern == format_a0 || _valueIntern == format_a1
				|| _valueIntern == format_a2 || _valueIntern == format_a3
				|| _valueIntern == format_a4 || _valueIntern == format_a5
				|| _valueIntern == format_a6 || _valueIntern == format_b0
				|| _valueIntern == format_b1 || _valueIntern == format_b2
				|| _valueIntern == format_b3 || _valueIntern == format_b4
				|| _valueIntern == format_b5 || _valueIntern == format_b6
				|| _valueIntern == format_b7 || _valueIntern == format_letter
				|| _valueIntern == format_halfletter
				|| _valueIntern == format_legal || _valueIntern == format_11X17))
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_VIOLATES_ENUMERATION,
					String.valueOf(_value)));

		format = _value;
	}

	/**
	 * Get value of property format
	 *
	 * @return - value of field format
	 */
	public String getFormat() {
		return format;
	}

	/* *** Property applySharpening *** */

	private boolean applySharpening;

	/**
	 * Set value of property applySharpening
	 *
	 * @param _value - new field value
	 */
	public void setApplySharpening(boolean _value) {
		applySharpening = _value;
	}

	/**
	 * Get value of property applySharpening
	 *
	 * @return - value of field applySharpening
	 */
	public boolean getApplySharpening() {
		return applySharpening;
	}

	/* *** Property radius *** */

	private float radius;

	/**
	 * Set value of property radius
	 *
	 * @param _value - new field value
	 */
	public void setRadius(float _value) {
		radius = _value;
	}

	/**
	 * Get value of property radius
	 *
	 * @return - value of field radius
	 */
	public float getRadius() {
		return radius;
	}

	/* *** Property amount *** */

	private float amount;

	/**
	 * Set value of property amount
	 *
	 * @param _value - new field value
	 */
	public void setAmount(float _value) {
		amount = _value;
	}

	/**
	 * Get value of property amount
	 *
	 * @return - value of field amount
	 */
	public float getAmount() {
		return amount;
	}

	/* *** Property threshold *** */

	private int threshold;

	/**
	 * Set value of property threshold
	 *
	 * @param _value - new field value
	 */
	public void setThreshold(int _value) {
		threshold = _value;
	}

	/**
	 * Get value of property threshold
	 *
	 * @return - value of field threshold
	 */
	public int getThreshold() {
		return threshold;
	}

	/* *** Property jpegQuality *** */

	private int jpegQuality;

	/**
	 * Set value of property jpegQuality
	 *
	 * @param _value - new field value
	 */
	public void setJpegQuality(int _value) {
		jpegQuality = _value;
	}

	/**
	 * Get value of property jpegQuality
	 *
	 * @return - value of field jpegQuality
	 */
	public int getJpegQuality() {
		return jpegQuality;
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

		if (!(o instanceof PageLayout_type) || !super.equals(o))
			return false;
		PageLayout_type other = (PageLayout_type) o;
		return ((getName() == null && other.getName() == null) || (getName() != null && getName()
				.equals(other.getName())))

				&& getType() == other.getType()

				&& ((getTitle() == null && other.getTitle() == null) || (getTitle() != null && getTitle()
						.equals(other.getTitle())))

				&& ((getSubtitle() == null && other.getSubtitle() == null) || (getSubtitle() != null && getSubtitle()
						.equals(other.getSubtitle())))

				&& ((getFooter() == null && other.getFooter() == null) || (getFooter() != null && getFooter()
						.equals(other.getFooter())))

				&& getSize() == other.getSize()

				&& getColumns() == other.getColumns()

				&& getLeftMargin() == other.getLeftMargin()

				&& getRightMargin() == other.getRightMargin()

				&& getHorizontalGap() == other.getHorizontalGap()

				&& getTopMargin() == other.getTopMargin()

				&& getBottomMargin() == other.getBottomMargin()

				&& getVerticalGap() == other.getVerticalGap()

				&& ((getCaption1() == null && other.getCaption1() == null) || (getCaption1() != null && getCaption1()
						.equals(other.getCaption1())))

				&& ((getCaption2() == null && other.getCaption2() == null) || (getCaption2() != null && getCaption2()
						.equals(other.getCaption2())))

				&& ((getAlt() == null && other.getAlt() == null) || (getAlt() != null && getAlt()
						.equals(other.getAlt())))

				&& getKeyLine() == other.getKeyLine()

				&& getLandscape() == other.getLandscape()

				&& getFacingPages() == other.getFacingPages()

				&& ((getFormat() == null && other.getFormat() == null) || (getFormat() != null && getFormat()
						.equals(other.getFormat())))

				&& getApplySharpening() == other.getApplySharpening()

				&& getRadius() == other.getRadius()

				&& getAmount() == other.getAmount()

				&& getThreshold() == other.getThreshold()

				&& getJpegQuality() == other.getJpegQuality()

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

		int hashCode = 716979392 + ((getName() == null) ? 0 : getName()
				.hashCode());

		hashCode = 31 * hashCode + getType();

		hashCode = 31 * hashCode
				+ ((getTitle() == null) ? 0 : getTitle().hashCode());

		hashCode = 31 * hashCode
				+ ((getSubtitle() == null) ? 0 : getSubtitle().hashCode());

		hashCode = 31 * hashCode
				+ ((getFooter() == null) ? 0 : getFooter().hashCode());

		hashCode = 31 * hashCode + getSize();

		hashCode = 31 * hashCode + getColumns();

		hashCode = 31 * hashCode + getLeftMargin();

		hashCode = 31 * hashCode + getRightMargin();

		hashCode = 31 * hashCode + getHorizontalGap();

		hashCode = 31 * hashCode + getTopMargin();

		hashCode = 31 * hashCode + getBottomMargin();

		hashCode = 31 * hashCode + getVerticalGap();

		hashCode = 31 * hashCode
				+ ((getCaption1() == null) ? 0 : getCaption1().hashCode());

		hashCode = 31 * hashCode
				+ ((getCaption2() == null) ? 0 : getCaption2().hashCode());

		hashCode = 31 * hashCode
				+ ((getAlt() == null) ? 0 : getAlt().hashCode());

		hashCode = 31 * hashCode + getKeyLine();

		hashCode = 31 * hashCode + (getLandscape() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getFacingPages() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getFormat() == null) ? 0 : getFormat().hashCode());

		hashCode = 31 * hashCode + (getApplySharpening() ? 1231 : 1237);

		hashCode = 31 * hashCode + computeDoubleHash(getRadius());

		hashCode = 31 * hashCode + computeDoubleHash(getAmount());

		hashCode = 31 * hashCode + getThreshold();

		hashCode = 31 * hashCode + getJpegQuality();

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

		if (subtitle == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subtitle"));

		if (footer == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "footer"));

		if (caption1 == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "caption1"));

		if (caption2 == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "caption2"));

		if (alt == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "alt"));

		if (format == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "format"));

	}

}
