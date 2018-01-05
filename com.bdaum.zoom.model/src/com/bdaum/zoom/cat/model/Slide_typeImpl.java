package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset slide
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Slide_typeImpl extends AomObject implements Slide_type {

	static final long serialVersionUID = 287844778L;

	/* ----- Constructors ----- */

	public Slide_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param caption - Property
	 * @param sequenceNo - Property
	 * @param description - Property
	 * @param layout - Property
	 * @param delay - Property
	 * @param fadeIn - Property
	 * @param duration - Property
	 * @param fadeOut - Property
	 * @param effect - Property
	 * @param zoom - Property
	 * @param zoomX - Property
	 * @param zoomY - Property
	 * @param noVoice - Property
	 * @param safety - Property
	 */
	public Slide_typeImpl(String caption, int sequenceNo, String description,
			int layout, int delay, int fadeIn, int duration, int fadeOut,
			int effect, int zoom, int zoomX, int zoomY, boolean noVoice,
			int safety) {
		super();
		this.caption = caption;
		this.sequenceNo = sequenceNo;
		this.description = description;
		this.layout = layout;
		this.delay = delay;
		this.fadeIn = fadeIn;
		this.duration = duration;
		this.fadeOut = fadeOut;
		this.effect = effect;
		this.zoom = zoom;
		this.zoomX = zoomX;
		this.zoomY = zoomY;
		this.noVoice = noVoice;
		this.safety = safety;

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

	/* *** Property layout *** */

	private int layout;

	/**
	 * Set value of property layout
	 *
	 * @param _value - new field value
	 */
	public void setLayout(int _value) {
		layout = _value;
	}

	/**
	 * Get value of property layout
	 *
	 * @return - value of field layout
	 */
	public int getLayout() {
		return layout;
	}

	/* *** Property delay *** */

	private int delay;

	/**
	 * Set value of property delay
	 *
	 * @param _value - new field value
	 */
	public void setDelay(int _value) {
		delay = _value;
	}

	/**
	 * Get value of property delay
	 *
	 * @return - value of field delay
	 */
	public int getDelay() {
		return delay;
	}

	/* *** Property fadeIn *** */

	private int fadeIn;

	/**
	 * Set value of property fadeIn
	 *
	 * @param _value - new field value
	 */
	public void setFadeIn(int _value) {
		fadeIn = _value;
	}

	/**
	 * Get value of property fadeIn
	 *
	 * @return - value of field fadeIn
	 */
	public int getFadeIn() {
		return fadeIn;
	}

	/* *** Property duration *** */

	private int duration;

	/**
	 * Set value of property duration
	 *
	 * @param _value - new field value
	 */
	public void setDuration(int _value) {
		duration = _value;
	}

	/**
	 * Get value of property duration
	 *
	 * @return - value of field duration
	 */
	public int getDuration() {
		return duration;
	}

	/* *** Property fadeOut *** */

	private int fadeOut;

	/**
	 * Set value of property fadeOut
	 *
	 * @param _value - new field value
	 */
	public void setFadeOut(int _value) {
		fadeOut = _value;
	}

	/**
	 * Get value of property fadeOut
	 *
	 * @return - value of field fadeOut
	 */
	public int getFadeOut() {
		return fadeOut;
	}

	/* *** Property effect *** */

	private int effect;

	/**
	 * Set value of property effect
	 *
	 * @param _value - new field value
	 */
	public void setEffect(int _value) {
		effect = _value;
	}

	/**
	 * Get value of property effect
	 *
	 * @return - value of field effect
	 */
	public int getEffect() {
		return effect;
	}

	/* *** Property zoom *** */

	private int zoom;

	/**
	 * Set value of property zoom
	 *
	 * @param _value - new field value
	 */
	public void setZoom(int _value) {
		zoom = _value;
	}

	/**
	 * Get value of property zoom
	 *
	 * @return - value of field zoom
	 */
	public int getZoom() {
		return zoom;
	}

	/* *** Property zoomX *** */

	private int zoomX;

	/**
	 * Set value of property zoomX
	 *
	 * @param _value - new field value
	 */
	public void setZoomX(int _value) {
		zoomX = _value;
	}

	/**
	 * Get value of property zoomX
	 *
	 * @return - value of field zoomX
	 */
	public int getZoomX() {
		return zoomX;
	}

	/* *** Property zoomY *** */

	private int zoomY;

	/**
	 * Set value of property zoomY
	 *
	 * @param _value - new field value
	 */
	public void setZoomY(int _value) {
		zoomY = _value;
	}

	/**
	 * Get value of property zoomY
	 *
	 * @return - value of field zoomY
	 */
	public int getZoomY() {
		return zoomY;
	}

	/* *** Property noVoice *** */

	private boolean noVoice;

	/**
	 * Set value of property noVoice
	 *
	 * @param _value - new field value
	 */
	public void setNoVoice(boolean _value) {
		noVoice = _value;
	}

	/**
	 * Get value of property noVoice
	 *
	 * @return - value of field noVoice
	 */
	public boolean getNoVoice() {
		return noVoice;
	}

	/* *** Property safety *** */

	private int safety;

	/**
	 * Set value of property safety
	 *
	 * @param _value - new field value
	 */
	public void setSafety(int _value) {
		safety = _value;
	}

	/**
	 * Get value of property safety
	 *
	 * @return - value of field safety
	 */
	public int getSafety() {
		return safety;
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

		return (o instanceof Slide_typeImpl)
				&& getStringId().equals(((Slide_typeImpl) o).getStringId());
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

		return getStringId().hashCode();
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
