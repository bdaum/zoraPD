package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webExhibit
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class WebExhibit_typeImpl extends AomObject implements WebExhibit_type {

	static final long serialVersionUID = 241254390L;

	/* ----- Constructors ----- */

	public WebExhibit_typeImpl() {
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
	 */
	public WebExhibit_typeImpl(String caption, int sequenceNo,
			String description, boolean htmlDescription, String altText,
			boolean downloadable, boolean includeMetadata) {
		super();
		this.caption = caption;
		this.sequenceNo = sequenceNo;
		this.description = description;
		this.htmlDescription = htmlDescription;
		this.altText = altText;
		this.downloadable = downloadable;
		this.includeMetadata = includeMetadata;

	}

	/* ----- Fields ----- */

	/* *** Property caption *** */

	private String caption = AomConstants.INIT_String;

	/**
	 * Set value of property caption
	 *
	 * @param _value - new field value
	 */
	public void setCaption(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "caption"));
		caption = _value;
	}

	/**
	 * Get value of property caption
	 *
	 * @return - value of field caption
	 */
	public String getCaption() {
		return caption;
	}

	/* *** Property sequenceNo *** */

	private transient int sequenceNo;

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

	/* *** Property altText *** */

	private String altText;

	/**
	 * Set value of property altText
	 *
	 * @param _value - new field value
	 */
	public void setAltText(String _value) {
		altText = _value;
	}

	/**
	 * Get value of property altText
	 *
	 * @return - value of field altText
	 */
	public String getAltText() {
		return altText;
	}

	/* *** Property downloadable *** */

	private boolean downloadable;

	/**
	 * Set value of property downloadable
	 *
	 * @param _value - new field value
	 */
	public void setDownloadable(boolean _value) {
		downloadable = _value;
	}

	/**
	 * Get value of property downloadable
	 *
	 * @return - value of field downloadable
	 */
	public boolean getDownloadable() {
		return downloadable;
	}

	/* *** Property includeMetadata *** */

	private boolean includeMetadata;

	/**
	 * Set value of property includeMetadata
	 *
	 * @param _value - new field value
	 */
	public void setIncludeMetadata(boolean _value) {
		includeMetadata = _value;
	}

	/**
	 * Get value of property includeMetadata
	 *
	 * @return - value of field includeMetadata
	 */
	public boolean getIncludeMetadata() {
		return includeMetadata;
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

		if (!(o instanceof WebExhibit_type) || !super.equals(o))
			return false;
		WebExhibit_type other = (WebExhibit_type) o;
		return ((getCaption() == null && other.getCaption() == null) || (getCaption() != null && getCaption()
				.equals(other.getCaption())))

				&& getSequenceNo() == other.getSequenceNo()

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& getHtmlDescription() == other.getHtmlDescription()

				&& ((getAltText() == null && other.getAltText() == null) || (getAltText() != null && getAltText()
						.equals(other.getAltText())))

				&& getDownloadable() == other.getDownloadable()

				&& getIncludeMetadata() == other.getIncludeMetadata()

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

		int hashCode = 1844957388 + ((getCaption() == null) ? 0 : getCaption()
				.hashCode());

		hashCode = 31 * hashCode + getSequenceNo();

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode + (getHtmlDescription() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getAltText() == null) ? 0 : getAltText().hashCode());

		hashCode = 31 * hashCode + (getDownloadable() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getIncludeMetadata() ? 1231 : 1237);

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

		if (caption == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "caption"));

	}

}
