package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset slideShow
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface SlideShow_type extends AomValueChangedNotifier,
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
	 * Set value of property fromPreview
	 *
	 * @param _value - new element value
	 */
	public void setFromPreview(boolean _value);

	/**
	 * Get value of property fromPreview
	 *
	 * @return - value of field fromPreview
	 */
	public boolean getFromPreview();

	/**
	 * Set value of property duration
	 *
	 * @param _value - new element value
	 */
	public void setDuration(int _value);

	/**
	 * Get value of property duration
	 *
	 * @return - value of field duration
	 */
	public int getDuration();

	/**
	 * Set value of property effect
	 *
	 * @param _value - new element value
	 */
	public void setEffect(int _value);

	/**
	 * Get value of property effect
	 *
	 * @return - value of field effect
	 */
	public int getEffect();

	/**
	 * Set value of property fading
	 *
	 * @param _value - new element value
	 */
	public void setFading(int _value);

	/**
	 * Get value of property fading
	 *
	 * @return - value of field fading
	 */
	public int getFading();

	/**
	 * Set value of property titleDisplay
	 *
	 * @param _value - new element value
	 */
	public void setTitleDisplay(int _value);

	/**
	 * Get value of property titleDisplay
	 *
	 * @return - value of field titleDisplay
	 */
	public int getTitleDisplay();

	/**
	 * Set value of property titleContent
	 *
	 * @param _value - new element value
	 */
	public void setTitleContent(int _value);

	/**
	 * Get value of property titleContent
	 *
	 * @return - value of field titleContent
	 */
	public int getTitleContent();

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
	 * Set value of property skipDublettes
	 *
	 * @param _value - new element value
	 */
	public void setSkipDublettes(boolean _value);

	/**
	 * Get value of property skipDublettes
	 *
	 * @return - value of field skipDublettes
	 */
	public boolean getSkipDublettes();

	/**
	 * Set value of property voiceNotes
	 *
	 * @param _value - new element value
	 */
	public void setVoiceNotes(boolean _value);

	/**
	 * Get value of property voiceNotes
	 *
	 * @return - value of field voiceNotes
	 */
	public boolean getVoiceNotes();

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

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
