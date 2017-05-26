package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset bookmark
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Bookmark_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property label
	 *
	 * @param _value - new element value
	 */
	public void setLabel(String _value);

	/**
	 * Get value of property label
	 *
	 * @return - value of field label
	 */
	public String getLabel();

	/**
	 * Set value of property assetId
	 *
	 * @param _value - new element value
	 */
	public void setAssetId(String _value);

	/**
	 * Get value of property assetId
	 *
	 * @return - value of field assetId
	 */
	public String getAssetId();

	/**
	 * Set value of property collectionId
	 *
	 * @param _value - new element value
	 */
	public void setCollectionId(String _value);

	/**
	 * Get value of property collectionId
	 *
	 * @return - value of field collectionId
	 */
	public String getCollectionId();

	/**
	 * Set value of property jpegImage
	 *
	 * @param _value - new element value
	 */
	public void setJpegImage(byte[] _value);

	/**
	 * Set single element of array jpegImage
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setJpegImage(byte _element, int _i);

	/**
	 * Get value of property jpegImage
	 *
	 * @return - value of field jpegImage
	 */
	public byte[] getJpegImage();

	/**
	 * Get single element of array jpegImage
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array jpegImage
	 */
	public byte getJpegImage(int _i);

	/**
	 * Set value of property createdAt
	 *
	 * @param _value - new element value
	 */
	public void setCreatedAt(Date _value);

	/**
	 * Get value of property createdAt
	 *
	 * @return - value of field createdAt
	 */
	public Date getCreatedAt();

	/**
	 * Set value of property peer
	 *
	 * @param _value - new element value
	 */
	public void setPeer(String _value);

	/**
	 * Get value of property peer
	 *
	 * @return - value of field peer
	 */
	public String getPeer();

	/**
	 * Set value of property catFile
	 *
	 * @param _value - new element value
	 */
	public void setCatFile(String _value);

	/**
	 * Get value of property catFile
	 *
	 * @return - value of field catFile
	 */
	public String getCatFile();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
