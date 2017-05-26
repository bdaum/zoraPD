package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset artworkOrObject
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class ArtworkOrObject_typeImpl extends AomObject implements
		ArtworkOrObject_type {

	static final long serialVersionUID = 588026341L;

	/* ----- Constructors ----- */

	public ArtworkOrObject_typeImpl() {
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
	public ArtworkOrObject_typeImpl(String copyrightNotice, Date dateCreated,
			String source, String sourceInventoryNumber, String title) {
		super();
		this.copyrightNotice = copyrightNotice;
		this.dateCreated = dateCreated;
		this.source = source;
		this.sourceInventoryNumber = sourceInventoryNumber;
		this.title = title;

	}

	/* ----- Fields ----- */

	/* *** Property copyrightNotice *** */

	private String copyrightNotice;

	/**
	 * Set value of property copyrightNotice
	 *
	 * @param _value - new field value
	 */
	public void setCopyrightNotice(String _value) {
		copyrightNotice = _value;
	}

	/**
	 * Get value of property copyrightNotice
	 *
	 * @return - value of field copyrightNotice
	 */
	public String getCopyrightNotice() {
		return copyrightNotice;
	}

	/* *** Property creator *** */

	private String[] creator = new String[0];

	/**
	 * Set value of property creator
	 *
	 * @param _value - new element value
	 */
	public void setCreator(String[] _value) {
		creator = _value;
	}

	/**
	 * Set single element of array creator
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setCreator(String _element, int _i) {
		creator[_i] = _element;
	}

	/**
	 * Get value of property creator
	 *
	 * @return - value of field creator
	 */
	public String[] getCreator() {
		return creator;
	}

	/**
	 * Get single element of array creator
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array creator
	 */
	public String getCreator(int _i) {
		return creator[_i];
	}

	/* *** Property dateCreated *** */

	private Date dateCreated;

	/**
	 * Set value of property dateCreated
	 *
	 * @param _value - new field value
	 */
	public void setDateCreated(Date _value) {
		dateCreated = _value;
	}

	/**
	 * Get value of property dateCreated
	 *
	 * @return - value of field dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/* *** Property source *** */

	private String source;

	/**
	 * Set value of property source
	 *
	 * @param _value - new field value
	 */
	public void setSource(String _value) {
		source = _value;
	}

	/**
	 * Get value of property source
	 *
	 * @return - value of field source
	 */
	public String getSource() {
		return source;
	}

	/* *** Property sourceInventoryNumber *** */

	private String sourceInventoryNumber;

	/**
	 * Set value of property sourceInventoryNumber
	 *
	 * @param _value - new field value
	 */
	public void setSourceInventoryNumber(String _value) {
		sourceInventoryNumber = _value;
	}

	/**
	 * Get value of property sourceInventoryNumber
	 *
	 * @return - value of field sourceInventoryNumber
	 */
	public String getSourceInventoryNumber() {
		return sourceInventoryNumber;
	}

	/* *** Property title *** */

	private String title;

	/**
	 * Set value of property title
	 *
	 * @param _value - new field value
	 */
	public void setTitle(String _value) {
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

		if (!(o instanceof ArtworkOrObject_type) || !super.equals(o))
			return false;
		ArtworkOrObject_type other = (ArtworkOrObject_type) o;
		return ((getCopyrightNotice() == null && other.getCopyrightNotice() == null) || (getCopyrightNotice() != null && getCopyrightNotice()
				.equals(other.getCopyrightNotice())))

				&& ((getCreator() == null && other.getCreator() == null) || (getCreator() != null && getCreator()
						.equals(other.getCreator())))

				&& ((getDateCreated() == null && other.getDateCreated() == null) || (getDateCreated() != null && getDateCreated()
						.equals(other.getDateCreated())))

				&& ((getSource() == null && other.getSource() == null) || (getSource() != null && getSource()
						.equals(other.getSource())))

				&& ((getSourceInventoryNumber() == null && other
						.getSourceInventoryNumber() == null) || (getSourceInventoryNumber() != null && getSourceInventoryNumber()
						.equals(other.getSourceInventoryNumber())))

				&& ((getTitle() == null && other.getTitle() == null) || (getTitle() != null && getTitle()
						.equals(other.getTitle())))

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

		int hashCode = -290014019
				+ ((getCopyrightNotice() == null) ? 0 : getCopyrightNotice()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getCreator() == null) ? 0 : getCreator().hashCode());

		hashCode = 31
				* hashCode
				+ ((getDateCreated() == null) ? 0 : getDateCreated().hashCode());

		hashCode = 31 * hashCode
				+ ((getSource() == null) ? 0 : getSource().hashCode());

		hashCode = 31
				* hashCode
				+ ((getSourceInventoryNumber() == null) ? 0
						: getSourceInventoryNumber().hashCode());

		hashCode = 31 * hashCode
				+ ((getTitle() == null) ? 0 : getTitle().hashCode());

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

	}

}
