package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset smartCollection
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface SmartCollection_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property name
	 *
	 * @param _value - new element value
	 */
	public void setName(String _value);

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName();

	/**
	 * Set value of property system
	 *
	 * @param _value - new element value
	 */
	public void setSystem(boolean _value);

	/**
	 * Get value of property system
	 *
	 * @return - value of field system
	 */
	public boolean getSystem();

	/**
	 * Set value of property album
	 *
	 * @param _value - new element value
	 */
	public void setAlbum(boolean _value);

	/**
	 * Get value of property album
	 *
	 * @return - value of field album
	 */
	public boolean getAlbum();

	/**
	 * Set value of property adhoc
	 *
	 * @param _value - new element value
	 */
	public void setAdhoc(boolean _value);

	/**
	 * Get value of property adhoc
	 *
	 * @return - value of field adhoc
	 */
	public boolean getAdhoc();

	/**
	 * Set value of property network
	 *
	 * @param _value - new element value
	 */
	public void setNetwork(boolean _value);

	/**
	 * Get value of property network
	 *
	 * @return - value of field network
	 */
	public boolean getNetwork();

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
	 * Set value of property colorCode
	 *
	 * @param _value - new element value
	 */
	public void setColorCode(int _value);

	/**
	 * Get value of property colorCode
	 *
	 * @return - value of field colorCode
	 */
	public int getColorCode();

	/**
	 * Set value of property lastAccessDate
	 *
	 * @param _value - new element value
	 */
	public void setLastAccessDate(Date _value);

	/**
	 * Get value of property lastAccessDate
	 *
	 * @return - value of field lastAccessDate
	 */
	public Date getLastAccessDate();

	/**
	 * Set value of property generation
	 *
	 * @param _value - new element value
	 */
	public void setGeneration(int _value);

	/**
	 * Get value of property generation
	 *
	 * @return - value of field generation
	 */
	public int getGeneration();

	/**
	 * Set value of property perspective
	 *
	 * @param _value - new element value
	 */
	public void setPerspective(String _value);

	/**
	 * Get value of property perspective
	 *
	 * @return - value of field perspective
	 */
	public String getPerspective();

	/**
	 * Set value of property showLabel
	 *
	 * @param _value - new element value
	 */
	public void setShowLabel(int _value);

	/**
	 * Get value of property showLabel
	 *
	 * @return - value of field showLabel
	 */
	public int getShowLabel();

	/**
	 * Set value of property labelTemplate
	 *
	 * @param _value - new element value
	 */
	public void setLabelTemplate(String _value);

	/**
	 * Get value of property labelTemplate
	 *
	 * @return - value of field labelTemplate
	 */
	public String getLabelTemplate();

	/**
	 * Set value of property fontSize
	 *
	 * @param _value - new element value
	 */
	public void setFontSize(int _value);

	/**
	 * Get value of property fontSize
	 *
	 * @return - value of field fontSize
	 */
	public int getFontSize();

	/**
	 * Set value of property alignment
	 *
	 * @param _value - new element value
	 */
	public void setAlignment(int _value);

	/**
	 * Get value of property alignment
	 *
	 * @return - value of field alignment
	 */
	public int getAlignment();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
