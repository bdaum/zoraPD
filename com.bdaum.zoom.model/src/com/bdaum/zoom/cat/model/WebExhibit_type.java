package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webExhibit
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface WebExhibit_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property caption
	 *
	 * @param _value - new element value
	 */
	public void setCaption(String _value);

	/**
	 * Get value of property caption
	 *
	 * @return - value of field caption
	 */
	public String getCaption();

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
	 * Set value of property altText
	 *
	 * @param _value - new element value
	 */
	public void setAltText(String _value);

	/**
	 * Get value of property altText
	 *
	 * @return - value of field altText
	 */
	public String getAltText();

	/**
	 * Set value of property downloadable
	 *
	 * @param _value - new element value
	 */
	public void setDownloadable(boolean _value);

	/**
	 * Get value of property downloadable
	 *
	 * @return - value of field downloadable
	 */
	public boolean getDownloadable();

	/**
	 * Set value of property includeMetadata
	 *
	 * @param _value - new element value
	 */
	public void setIncludeMetadata(boolean _value);

	/**
	 * Get value of property includeMetadata
	 *
	 * @return - value of field includeMetadata
	 */
	public boolean getIncludeMetadata();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
