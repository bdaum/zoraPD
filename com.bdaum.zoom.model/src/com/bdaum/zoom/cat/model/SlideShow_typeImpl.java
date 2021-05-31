package com.bdaum.zoom.cat.model;

import java.util.Date;

import com.bdaum.aoModeling.runtime.AomConstants;
import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.aoModeling.runtime.ConstraintException;
import com.bdaum.aoModeling.runtime.ErrorMessages;
import com.bdaum.aoModeling.runtime.ModelMessages;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset slideShow
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class SlideShow_typeImpl extends AomObject implements SlideShow_type {

	static final long serialVersionUID = -1799345235L;

	/* ----- Constructors ----- */

	public SlideShow_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param description - Property
	 * @param fromPreview - Property
	 * @param duration - Property
	 * @param effect - Property
	 * @param fading - Property
	 * @param zoom - Property
	 * @param titleDisplay - Property
	 * @param titleContent - Property
	 * @param titleScheme - Property
	 * @param titleTransparency - Property
	 * @param colorScheme - Property
	 * @param adhoc - Property
	 * @param skipDublettes - Property
	 * @param voiceNotes - Property
	 * @param lastAccessDate - Property
	 * @param perspective - Property
	 */
	public SlideShow_typeImpl(String name, String description,
			boolean fromPreview, int duration, int effect, int fading,
			int zoom, int titleDisplay, int titleContent, int titleScheme, int titleTransparency, int colorScheme, boolean adhoc,
			boolean skipDublettes, boolean voiceNotes, Date lastAccessDate,
			String perspective) {
		super();
		this.name = name;
		this.description = description;
		this.fromPreview = fromPreview;
		this.duration = duration;
		this.effect = effect;
		this.fading = fading;
		this.zoom = zoom;
		this.titleDisplay = titleDisplay;
		this.titleContent = titleContent;
		this.titleScheme = titleScheme;
		this.titleTransparency = titleTransparency;
		this.colorScheme = colorScheme;
		this.adhoc = adhoc;
		this.skipDublettes = skipDublettes;
		this.voiceNotes = voiceNotes;
		this.lastAccessDate = lastAccessDate;
		this.perspective = perspective;

	}

	/* ----- Fields ----- */

	/* *** Property name *** */

	private String name = AomConstants.INIT_String;

	/**
	 * Set value of property name
	 *
	 * @param _value - new field value
	 */
	public void setName(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));
		name = _value;
	}

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName() {
		return name;
	}

	/* *** Property description *** */

	private String description = AomConstants.INIT_String;

	/**
	 * Set value of property description
	 *
	 * @param _value - new field value
	 */
	public void setDescription(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "description"));
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

	/* *** Property fromPreview *** */

	private boolean fromPreview;

	/**
	 * Set value of property fromPreview
	 *
	 * @param _value - new field value
	 */
	public void setFromPreview(boolean _value) {
		fromPreview = _value;
	}

	/**
	 * Get value of property fromPreview
	 *
	 * @return - value of field fromPreview
	 */
	public boolean getFromPreview() {
		return fromPreview;
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

	/* *** Property fading *** */

	private int fading;

	/**
	 * Set value of property fading
	 *
	 * @param _value - new field value
	 */
	public void setFading(int _value) {
		fading = _value;
	}

	/**
	 * Get value of property fading
	 *
	 * @return - value of field fading
	 */
	public int getFading() {
		return fading;
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

	/* *** Property titleDisplay *** */

	private int titleDisplay;

	/**
	 * Set value of property titleDisplay
	 *
	 * @param _value - new field value
	 */
	public void setTitleDisplay(int _value) {
		titleDisplay = _value;
	}

	/**
	 * Get value of property titleDisplay
	 *
	 * @return - value of field titleDisplay
	 */
	public int getTitleDisplay() {
		return titleDisplay;
	}

	/* *** Property titleContent *** */

	private int titleContent;

	/**
	 * Set value of property titleContent
	 *
	 * @param _value - new field value
	 */
	public void setTitleContent(int _value) {
		titleContent = _value;
	}

	/**
	 * Get value of property titleContent
	 *
	 * @return - value of field titleContent
	 */
	public int getTitleContent() {
		return titleContent;
	}
	
	/* *** Property titleScheme *** */

	private int titleScheme;

	/**
	 * Set value of property titleScheme
	 *
	 * @param _value - new field value
	 */
	public void setTitleScheme(int _value) {
		titleScheme = _value;
	}

	/**
	 * Get value of property titleScheme
	 *
	 * @return - value of field titleScheme
	 */
	public int getTitleScheme() {
		return titleScheme;
	}
	
	/* *** Property titleTransparency *** */

	private int titleTransparency;

	/**
	 * Set value of property titleTransparency
	 *
	 * @param _value - new field value
	 */
	public void setTitleTransparency(int _value) {
		titleTransparency = _value;
	}

	/**
	 * Get value of property titleTransparency
	 *
	 * @return - value of field titleTransparency
	 */
	public int getTitleTransparency() {
		return titleTransparency;
	}
	
	/* *** Property colorScheme *** */

	private int colorScheme;

	/**
	 * Set value of property colorScheme
	 *
	 * @param _value - new field value
	 */
	public void setColorScheme(int _value) {
		colorScheme = _value;
	}

	/**
	 * Get value of property colorScheme
	 *
	 * @return - value of field colorScheme
	 */
	public int getColorScheme() {
		return colorScheme;
	}



	/* *** Property adhoc *** */

	private boolean adhoc;

	/**
	 * Set value of property adhoc
	 *
	 * @param _value - new field value
	 */
	public void setAdhoc(boolean _value) {
		adhoc = _value;
	}

	/**
	 * Get value of property adhoc
	 *
	 * @return - value of field adhoc
	 */
	public boolean getAdhoc() {
		return adhoc;
	}

	/* *** Property skipDublettes *** */

	private transient boolean skipDublettes;

	/**
	 * Set value of property skipDublettes
	 *
	 * @param _value - new field value
	 */
	public void setSkipDublettes(boolean _value) {
		skipDublettes = _value;
	}

	/**
	 * Get value of property skipDublettes
	 *
	 * @return - value of field skipDublettes
	 */
	public boolean getSkipDublettes() {
		return skipDublettes;
	}

	/* *** Property voiceNotes *** */

	private boolean voiceNotes;

	/**
	 * Set value of property voiceNotes
	 *
	 * @param _value - new field value
	 */
	public void setVoiceNotes(boolean _value) {
		voiceNotes = _value;
	}

	/**
	 * Get value of property voiceNotes
	 *
	 * @return - value of field voiceNotes
	 */
	public boolean getVoiceNotes() {
		return voiceNotes;
	}

	/* *** Property lastAccessDate *** */

	private Date lastAccessDate = new Date();

	/**
	 * Set value of property lastAccessDate
	 *
	 * @param _value - new field value
	 */
	public void setLastAccessDate(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastAccessDate"));
		lastAccessDate = _value;
	}

	/**
	 * Get value of property lastAccessDate
	 *
	 * @return - value of field lastAccessDate
	 */
	public Date getLastAccessDate() {
		return lastAccessDate;
	}

	/* *** Property perspective *** */

	private String perspective;

	/**
	 * Set value of property perspective
	 *
	 * @param _value - new field value
	 */
	public void setPerspective(String _value) {
		perspective = _value;
	}

	/**
	 * Get value of property perspective
	 *
	 * @return - value of field perspective
	 */
	public String getPerspective() {
		return perspective;
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

		if (!(o instanceof SlideShow_type) || !super.equals(o))
			return false;
		SlideShow_type other = (SlideShow_type) o;
		return ((getName() == null && other.getName() == null) || (getName() != null && getName()
				.equals(other.getName())))

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& getFromPreview() == other.getFromPreview()

				&& getDuration() == other.getDuration()

				&& getEffect() == other.getEffect()

				&& getFading() == other.getFading()

				&& getZoom() == other.getZoom()

				&& getTitleDisplay() == other.getTitleDisplay()

				&& getTitleContent() == other.getTitleContent()

				&& getAdhoc() == other.getAdhoc()

				&& getSkipDublettes() == other.getSkipDublettes()

				&& getVoiceNotes() == other.getVoiceNotes()

				&& ((getLastAccessDate() == null && other.getLastAccessDate() == null) || (getLastAccessDate() != null && getLastAccessDate()
						.equals(other.getLastAccessDate())))

				&& ((getPerspective() == null && other.getPerspective() == null) || (getPerspective() != null && getPerspective()
						.equals(other.getPerspective())))

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

		int hashCode = -1284088843
				+ ((getName() == null) ? 0 : getName().hashCode());

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode + (getFromPreview() ? 1231 : 1237);

		hashCode = 31 * hashCode + getDuration();

		hashCode = 31 * hashCode + getEffect();

		hashCode = 31 * hashCode + getFading();

		hashCode = 31 * hashCode + getZoom();

		hashCode = 31 * hashCode + getTitleDisplay();

		hashCode = 31 * hashCode + getTitleContent();

		hashCode = 31 * hashCode + (getAdhoc() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getSkipDublettes() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getVoiceNotes() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getLastAccessDate() == null) ? 0 : getLastAccessDate()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getPerspective() == null) ? 0 : getPerspective().hashCode());

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

		if (name == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));

		if (description == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "description"));

		if (lastAccessDate == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastAccessDate"));

	}

}
