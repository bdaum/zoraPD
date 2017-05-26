package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset storyboard
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Storyboard_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property title
	 *
	 * @param _value - new element value
	 */
	public void setTitle(String _value);

	/**
	 * Get value of property title
	 *
	 * @return - value of field title
	 */
	public String getTitle();

	/**
	 * Set value of property sequenceNo
	 *
	 * @param _value - new element value
	 */
	public void setSequenceNo(int _value);

	/**
	 * Get value of property sequenceNo
	 *
	 * @return - value of field sequenceNo
	 */
	public int getSequenceNo();

	/**
	 * Set value of property htmlDescription
	 *
	 * @param _value - new element value
	 */
	public void setHtmlDescription(boolean _value);

	/**
	 * Get value of property htmlDescription
	 *
	 * @return - value of field htmlDescription
	 */
	public boolean getHtmlDescription();

	/**
	 * Set value of property description
	 *
	 * @param _value - new element value
	 */
	public void setDescription(String _value);

	/**
	 * Get value of property description
	 *
	 * @return - value of field description
	 */
	public String getDescription();

	/**
	 * Set value of property imageSize
	 *
	 * @param _value - new element value
	 */
	public void setImageSize(int _value);

	/**
	 * Get value of property imageSize
	 *
	 * @return - value of field imageSize
	 */
	public int getImageSize();

	/**
	 * Set value of property enlargeSmall
	 *
	 * @param _value - new element value
	 */
	public void setEnlargeSmall(boolean _value);

	/**
	 * Get value of property enlargeSmall
	 *
	 * @return - value of field enlargeSmall
	 */
	public boolean getEnlargeSmall();

	/**
	 * Set value of property showCaptions
	 *
	 * @param _value - new element value
	 */
	public void setShowCaptions(boolean _value);

	/**
	 * Get value of property showCaptions
	 *
	 * @return - value of field showCaptions
	 */
	public boolean getShowCaptions();

	/**
	 * Set value of property showDescriptions
	 *
	 * @param _value - new element value
	 */
	public void setShowDescriptions(boolean _value);

	/**
	 * Get value of property showDescriptions
	 *
	 * @return - value of field showDescriptions
	 */
	public boolean getShowDescriptions();

	/**
	 * Set value of property showExif
	 *
	 * @param _value - new element value
	 */
	public void setShowExif(boolean _value);

	/**
	 * Get value of property showExif
	 *
	 * @return - value of field showExif
	 */
	public boolean getShowExif();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
