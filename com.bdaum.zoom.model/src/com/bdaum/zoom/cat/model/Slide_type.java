package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset slide
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Slide_type extends AomValueChangedNotifier,
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
	 * Set value of property layout
	 *
	 * @param _value - new element value
	 */
	public void setLayout(int _value);

	/**
	 * Get value of property layout
	 *
	 * @return - value of field layout
	 */
	public int getLayout();

	/**
	 * Set value of property delay
	 *
	 * @param _value - new element value
	 */
	public void setDelay(int _value);

	/**
	 * Get value of property delay
	 *
	 * @return - value of field delay
	 */
	public int getDelay();

	/**
	 * Set value of property fadeIn
	 *
	 * @param _value - new element value
	 */
	public void setFadeIn(int _value);

	/**
	 * Get value of property fadeIn
	 *
	 * @return - value of field fadeIn
	 */
	public int getFadeIn();

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
	 * Set value of property fadeOut
	 *
	 * @param _value - new element value
	 */
	public void setFadeOut(int _value);

	/**
	 * Get value of property fadeOut
	 *
	 * @return - value of field fadeOut
	 */
	public int getFadeOut();

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
	 * Set value of property noVoice
	 *
	 * @param _value - new element value
	 */
	public void setNoVoice(boolean _value);

	/**
	 * Get value of property noVoice
	 *
	 * @return - value of field noVoice
	 */
	public boolean getNoVoice();

	/**
	 * Set value of property safety
	 *
	 * @param _value - new element value
	 */
	public void setSafety(int _value);

	/**
	 * Get value of property safety
	 *
	 * @return - value of field safety
	 */
	public int getSafety();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
