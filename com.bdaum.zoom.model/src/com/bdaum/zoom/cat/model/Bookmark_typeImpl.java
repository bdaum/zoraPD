package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset bookmark
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Bookmark_typeImpl extends AomObject implements Bookmark_type {

	static final long serialVersionUID = -2729387131L;

	/* ----- Constructors ----- */

	public Bookmark_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param label - Property
	 * @param assetId - Property
	 * @param collectionId - Property
	 * @param createdAt - Property
	 * @param peer - Property
	 * @param catFile - Property
	 */
	public Bookmark_typeImpl(String label, String assetId, String collectionId,
			Date createdAt, String peer, String catFile) {
		super();
		this.label = label;
		this.assetId = assetId;
		this.collectionId = collectionId;
		this.createdAt = createdAt;
		this.peer = peer;
		this.catFile = catFile;

	}

	/* ----- Fields ----- */

	/* *** Property label *** */

	private String label = AomConstants.INIT_String;

	/**
	 * Set value of property label
	 *
	 * @param _value - new field value
	 */
	public void setLabel(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "label"));
		label = _value;
	}

	/**
	 * Get value of property label
	 *
	 * @return - value of field label
	 */
	public String getLabel() {
		return label;
	}

	/* *** Property assetId *** */

	private String assetId = AomConstants.INIT_String;

	/**
	 * Set value of property assetId
	 *
	 * @param _value - new field value
	 */
	public void setAssetId(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "assetId"));
		assetId = _value;
	}

	/**
	 * Get value of property assetId
	 *
	 * @return - value of field assetId
	 */
	public String getAssetId() {
		return assetId;
	}

	/* *** Property collectionId *** */

	private String collectionId = AomConstants.INIT_String;

	/**
	 * Set value of property collectionId
	 *
	 * @param _value - new field value
	 */
	public void setCollectionId(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "collectionId"));
		collectionId = _value;
	}

	/**
	 * Get value of property collectionId
	 *
	 * @return - value of field collectionId
	 */
	public String getCollectionId() {
		return collectionId;
	}

	/* *** Property jpegImage *** */

	private byte[] jpegImage = new byte[0];

	/**
	 * Set value of property jpegImage
	 *
	 * @param _value - new element value
	 */
	public void setJpegImage(byte[] _value) {
		jpegImage = _value;
	}

	/**
	 * Set single element of array jpegImage
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setJpegImage(byte _element, int _i) {
		jpegImage[_i] = _element;
	}

	/**
	 * Get value of property jpegImage
	 *
	 * @return - value of field jpegImage
	 */
	public byte[] getJpegImage() {
		return jpegImage;
	}

	/**
	 * Get single element of array jpegImage
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array jpegImage
	 */
	public byte getJpegImage(int _i) {
		return jpegImage[_i];
	}

	/* *** Property createdAt *** */

	private Date createdAt = new Date();

	/**
	 * Set value of property createdAt
	 *
	 * @param _value - new field value
	 */
	public void setCreatedAt(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "createdAt"));
		createdAt = _value;
	}

	/**
	 * Get value of property createdAt
	 *
	 * @return - value of field createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/* *** Property peer *** */

	private String peer = AomConstants.INIT_String;

	/**
	 * Set value of property peer
	 *
	 * @param _value - new field value
	 */
	public void setPeer(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "peer"));
		peer = _value;
	}

	/**
	 * Get value of property peer
	 *
	 * @return - value of field peer
	 */
	public String getPeer() {
		return peer;
	}

	/* *** Property catFile *** */

	private String catFile = AomConstants.INIT_String;

	/**
	 * Set value of property catFile
	 *
	 * @param _value - new field value
	 */
	public void setCatFile(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "catFile"));
		catFile = _value;
	}

	/**
	 * Get value of property catFile
	 *
	 * @return - value of field catFile
	 */
	public String getCatFile() {
		return catFile;
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

		if (!(o instanceof Bookmark_type) || !super.equals(o))
			return false;
		Bookmark_type other = (Bookmark_type) o;
		return ((getLabel() == null && other.getLabel() == null) || (getLabel() != null && getLabel()
				.equals(other.getLabel())))

				&& ((getAssetId() == null && other.getAssetId() == null) || (getAssetId() != null && getAssetId()
						.equals(other.getAssetId())))

				&& ((getCollectionId() == null && other.getCollectionId() == null) || (getCollectionId() != null && getCollectionId()
						.equals(other.getCollectionId())))

				&& ((getJpegImage() == null && other.getJpegImage() == null) || (getJpegImage() != null && getJpegImage()
						.equals(other.getJpegImage())))

				&& ((getCreatedAt() == null && other.getCreatedAt() == null) || (getCreatedAt() != null && getCreatedAt()
						.equals(other.getCreatedAt())))

				&& ((getPeer() == null && other.getPeer() == null) || (getPeer() != null && getPeer()
						.equals(other.getPeer())))

				&& ((getCatFile() == null && other.getCatFile() == null) || (getCatFile() != null && getCatFile()
						.equals(other.getCatFile())))

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

		int hashCode = -50616547
				+ ((getLabel() == null) ? 0 : getLabel().hashCode());

		hashCode = 31 * hashCode
				+ ((getAssetId() == null) ? 0 : getAssetId().hashCode());

		hashCode = 31
				* hashCode
				+ ((getCollectionId() == null) ? 0 : getCollectionId()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getJpegImage() == null) ? 0 : getJpegImage().hashCode());

		hashCode = 31 * hashCode
				+ ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());

		hashCode = 31 * hashCode
				+ ((getPeer() == null) ? 0 : getPeer().hashCode());

		hashCode = 31 * hashCode
				+ ((getCatFile() == null) ? 0 : getCatFile().hashCode());

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

		if (label == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "label"));

		if (assetId == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "assetId"));

		if (collectionId == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "collectionId"));

		if (createdAt == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "createdAt"));

		if (peer == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "peer"));

		if (catFile == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "catFile"));

	}

}
