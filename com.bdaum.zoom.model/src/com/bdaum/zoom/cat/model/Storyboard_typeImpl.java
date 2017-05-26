package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset storyboard
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Storyboard_typeImpl extends AomObject implements Storyboard_type {

	static final long serialVersionUID = -1325094902L;

	/* ----- Constructors ----- */

	public Storyboard_typeImpl() {
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
	public Storyboard_typeImpl(String title, int sequenceNo,
			boolean htmlDescription, String description, int imageSize,
			boolean enlargeSmall, boolean showCaptions,
			boolean showDescriptions, boolean showExif) {
		super();
		this.title = title;
		this.sequenceNo = sequenceNo;
		this.htmlDescription = htmlDescription;
		this.description = description;
		this.imageSize = imageSize;
		this.enlargeSmall = enlargeSmall;
		this.showCaptions = showCaptions;
		this.showDescriptions = showDescriptions;
		this.showExif = showExif;

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

	/* *** Property sequenceNo *** */

	private int sequenceNo;

	/**
	 * Set value of property sequenceNo
	 *
	 * @param _value - new field value
	 */
	public void setSequenceNo(int _value) {
		sequenceNo = _value;
	}

	/**
	 * Get value of property sequenceNo
	 *
	 * @return - value of field sequenceNo
	 */
	public int getSequenceNo() {
		return sequenceNo;
	}

	/* *** Property htmlDescription *** */

	private boolean htmlDescription;

	/**
	 * Set value of property htmlDescription
	 *
	 * @param _value - new field value
	 */
	public void setHtmlDescription(boolean _value) {
		htmlDescription = _value;
	}

	/**
	 * Get value of property htmlDescription
	 *
	 * @return - value of field htmlDescription
	 */
	public boolean getHtmlDescription() {
		return htmlDescription;
	}

	/* *** Property description *** */

	private String description;

	/**
	 * Set value of property description
	 *
	 * @param _value - new field value
	 */
	public void setDescription(String _value) {
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

	/* *** Property imageSize *** */

	private int imageSize;

	/**
	 * Set value of property imageSize
	 *
	 * @param _value - new field value
	 */
	public void setImageSize(int _value) {
		imageSize = _value;
	}

	/**
	 * Get value of property imageSize
	 *
	 * @return - value of field imageSize
	 */
	public int getImageSize() {
		return imageSize;
	}

	/* *** Property enlargeSmall *** */

	private boolean enlargeSmall;

	/**
	 * Set value of property enlargeSmall
	 *
	 * @param _value - new field value
	 */
	public void setEnlargeSmall(boolean _value) {
		enlargeSmall = _value;
	}

	/**
	 * Get value of property enlargeSmall
	 *
	 * @return - value of field enlargeSmall
	 */
	public boolean getEnlargeSmall() {
		return enlargeSmall;
	}

	/* *** Property showCaptions *** */

	private boolean showCaptions;

	/**
	 * Set value of property showCaptions
	 *
	 * @param _value - new field value
	 */
	public void setShowCaptions(boolean _value) {
		showCaptions = _value;
	}

	/**
	 * Get value of property showCaptions
	 *
	 * @return - value of field showCaptions
	 */
	public boolean getShowCaptions() {
		return showCaptions;
	}

	/* *** Property showDescriptions *** */

	private boolean showDescriptions;

	/**
	 * Set value of property showDescriptions
	 *
	 * @param _value - new field value
	 */
	public void setShowDescriptions(boolean _value) {
		showDescriptions = _value;
	}

	/**
	 * Get value of property showDescriptions
	 *
	 * @return - value of field showDescriptions
	 */
	public boolean getShowDescriptions() {
		return showDescriptions;
	}

	/* *** Property showExif *** */

	private boolean showExif;

	/**
	 * Set value of property showExif
	 *
	 * @param _value - new field value
	 */
	public void setShowExif(boolean _value) {
		showExif = _value;
	}

	/**
	 * Get value of property showExif
	 *
	 * @return - value of field showExif
	 */
	public boolean getShowExif() {
		return showExif;
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

		if (!(o instanceof Storyboard_type) || !super.equals(o))
			return false;
		Storyboard_type other = (Storyboard_type) o;
		return ((getTitle() == null && other.getTitle() == null) || (getTitle() != null && getTitle()
				.equals(other.getTitle())))

				&& getSequenceNo() == other.getSequenceNo()

				&& getHtmlDescription() == other.getHtmlDescription()

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& getImageSize() == other.getImageSize()

				&& getEnlargeSmall() == other.getEnlargeSmall()

				&& getShowCaptions() == other.getShowCaptions()

				&& getShowDescriptions() == other.getShowDescriptions()

				&& getShowExif() == other.getShowExif()

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

		int hashCode = 532769592 + ((getTitle() == null) ? 0 : getTitle()
				.hashCode());

		hashCode = 31 * hashCode + getSequenceNo();

		hashCode = 31 * hashCode + (getHtmlDescription() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode + getImageSize();

		hashCode = 31 * hashCode + (getEnlargeSmall() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getShowCaptions() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getShowDescriptions() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getShowExif() ? 1231 : 1237);

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

	}

}
