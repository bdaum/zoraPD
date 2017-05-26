package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset artworkOrObject
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface ArtworkOrObject_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property copyrightNotice
	 *
	 * @param _value - new element value
	 */
	public void setCopyrightNotice(String _value);

	/**
	 * Get value of property copyrightNotice
	 *
	 * @return - value of field copyrightNotice
	 */
	public String getCopyrightNotice();

	/**
	 * Set value of property creator
	 *
	 * @param _value - new element value
	 */
	public void setCreator(String[] _value);

	/**
	 * Set single element of array creator
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setCreator(String _element, int _i);

	/**
	 * Get value of property creator
	 *
	 * @return - value of field creator
	 */
	public String[] getCreator();

	/**
	 * Get single element of array creator
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array creator
	 */
	public String getCreator(int _i);

	/**
	 * Set value of property dateCreated
	 *
	 * @param _value - new element value
	 */
	public void setDateCreated(Date _value);

	/**
	 * Get value of property dateCreated
	 *
	 * @return - value of field dateCreated
	 */
	public Date getDateCreated();

	/**
	 * Set value of property source
	 *
	 * @param _value - new element value
	 */
	public void setSource(String _value);

	/**
	 * Get value of property source
	 *
	 * @return - value of field source
	 */
	public String getSource();

	/**
	 * Set value of property sourceInventoryNumber
	 *
	 * @param _value - new element value
	 */
	public void setSourceInventoryNumber(String _value);

	/**
	 * Get value of property sourceInventoryNumber
	 *
	 * @return - value of field sourceInventoryNumber
	 */
	public String getSourceInventoryNumber();

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

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
