package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset lastDeviceImport
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class LastDeviceImport_typeImpl extends AomObject implements
		LastDeviceImport_type {

	static final long serialVersionUID = -2782219318L;

	/* ----- Constructors ----- */

	public LastDeviceImport_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param volume - Property
	 * @param timestamp - Property
	 * @param description - Property
	 * @param owner - Property
	 * @param path - Property
	 * @param detectDuplicates - Property
	 * @param removeMedia - Property
	 * @param skipPolicy - Property
	 * @param targetDir - Property
	 * @param subfolders - Property
	 * @param deepSubfolders - Property
	 * @param selectedTemplate - Property
	 * @param cue - Property
	 * @param prefix - Property
	 * @param privacy - Property
	 */
	public LastDeviceImport_typeImpl(String volume, long timestamp,
			String description, String owner, String path,
			Boolean detectDuplicates, Boolean removeMedia, Integer skipPolicy,
			String targetDir, Integer subfolders, Boolean deepSubfolders,
			String selectedTemplate, String cue, String prefix, Integer privacy) {
		super();
		this.volume = volume;
		this.timestamp = timestamp;
		this.description = description;
		this.owner = owner;
		this.path = path;
		this.detectDuplicates = detectDuplicates;
		this.removeMedia = removeMedia;
		this.skipPolicy = skipPolicy;
		this.targetDir = targetDir;
		this.subfolders = subfolders;
		this.deepSubfolders = deepSubfolders;
		this.selectedTemplate = selectedTemplate;
		this.cue = cue;
		this.prefix = prefix;
		this.privacy = privacy;

	}

	/* ----- Fields ----- */

	/* *** Property volume *** */

	private String volume = AomConstants.INIT_String;

	/**
	 * Set value of property volume
	 *
	 * @param _value - new field value
	 */
	public void setVolume(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "volume"));
		volume = _value;
	}

	/**
	 * Get value of property volume
	 *
	 * @return - value of field volume
	 */
	public String getVolume() {
		return volume;
	}

	/* *** Property timestamp *** */

	private long timestamp;

	/**
	 * Set value of property timestamp
	 *
	 * @param _value - new field value
	 */
	public void setTimestamp(long _value) {
		timestamp = _value;
	}

	/**
	 * Get value of property timestamp
	 *
	 * @return - value of field timestamp
	 */
	public long getTimestamp() {
		return timestamp;
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

	/* *** Property owner *** */

	private String owner;

	/**
	 * Set value of property owner
	 *
	 * @param _value - new field value
	 */
	public void setOwner(String _value) {
		owner = _value;
	}

	/**
	 * Get value of property owner
	 *
	 * @return - value of field owner
	 */
	public String getOwner() {
		return owner;
	}

	/* *** Property path *** */

	private String path;

	/**
	 * Set value of property path
	 *
	 * @param _value - new field value
	 */
	public void setPath(String _value) {
		path = _value;
	}

	/**
	 * Get value of property path
	 *
	 * @return - value of field path
	 */
	public String getPath() {
		return path;
	}

	/* *** Property detectDuplicates *** */

	private Boolean detectDuplicates;

	/**
	 * Set value of property detectDuplicates
	 *
	 * @param _value - new field value
	 */
	public void setDetectDuplicates(Boolean _value) {
		detectDuplicates = _value;
	}

	/**
	 * Get value of property detectDuplicates
	 *
	 * @return - value of field detectDuplicates
	 */
	public Boolean getDetectDuplicates() {
		return detectDuplicates;
	}

	/* *** Property removeMedia *** */

	private Boolean removeMedia;

	/**
	 * Set value of property removeMedia
	 *
	 * @param _value - new field value
	 */
	public void setRemoveMedia(Boolean _value) {
		removeMedia = _value;
	}

	/**
	 * Get value of property removeMedia
	 *
	 * @return - value of field removeMedia
	 */
	public Boolean getRemoveMedia() {
		return removeMedia;
	}

	/* *** Property skipPolicy *** */

	private Integer skipPolicy;

	/**
	 * Set value of property skipPolicy
	 *
	 * @param _value - new field value
	 */
	public void setSkipPolicy(Integer _value) {
		skipPolicy = _value;
	}

	/**
	 * Get value of property skipPolicy
	 *
	 * @return - value of field skipPolicy
	 */
	public Integer getSkipPolicy() {
		return skipPolicy;
	}

	/* *** Property skippedFormats *** */

	private String[] skippedFormats = new String[0];

	/**
	 * Set value of property skippedFormats
	 *
	 * @param _value - new element value
	 */
	public void setSkippedFormats(String[] _value) {
		skippedFormats = _value;
	}

	/**
	 * Set single element of array skippedFormats
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSkippedFormats(String _element, int _i) {
		skippedFormats[_i] = _element;
	}

	/**
	 * Get value of property skippedFormats
	 *
	 * @return - value of field skippedFormats
	 */
	public String[] getSkippedFormats() {
		return skippedFormats;
	}

	/**
	 * Get single element of array skippedFormats
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array skippedFormats
	 */
	public String getSkippedFormats(int _i) {
		return skippedFormats[_i];
	}

	/* *** Property targetDir *** */

	private String targetDir;

	/**
	 * Set value of property targetDir
	 *
	 * @param _value - new field value
	 */
	public void setTargetDir(String _value) {
		targetDir = _value;
	}

	/**
	 * Get value of property targetDir
	 *
	 * @return - value of field targetDir
	 */
	public String getTargetDir() {
		return targetDir;
	}

	/* *** Property subfolders *** */

	private Integer subfolders;

	/**
	 * Set value of property subfolders
	 *
	 * @param _value - new field value
	 */
	public void setSubfolders(Integer _value) {
		subfolders = _value;
	}

	/**
	 * Get value of property subfolders
	 *
	 * @return - value of field subfolders
	 */
	public Integer getSubfolders() {
		return subfolders;
	}

	/* *** Property deepSubfolders *** */

	private Boolean deepSubfolders;

	/**
	 * Set value of property deepSubfolders
	 *
	 * @param _value - new field value
	 */
	public void setDeepSubfolders(Boolean _value) {
		deepSubfolders = _value;
	}

	/**
	 * Get value of property deepSubfolders
	 *
	 * @return - value of field deepSubfolders
	 */
	public Boolean getDeepSubfolders() {
		return deepSubfolders;
	}

	/* *** Property selectedTemplate *** */

	private String selectedTemplate;

	/**
	 * Set value of property selectedTemplate
	 *
	 * @param _value - new field value
	 */
	public void setSelectedTemplate(String _value) {
		selectedTemplate = _value;
	}

	/**
	 * Get value of property selectedTemplate
	 *
	 * @return - value of field selectedTemplate
	 */
	public String getSelectedTemplate() {
		return selectedTemplate;
	}

	/* *** Property cue *** */

	private String cue;

	/**
	 * Set value of property cue
	 *
	 * @param _value - new field value
	 */
	public void setCue(String _value) {
		cue = _value;
	}

	/**
	 * Get value of property cue
	 *
	 * @return - value of field cue
	 */
	public String getCue() {
		return cue;
	}

	/* *** Property keywords *** */

	private String[] keywords = new String[0];

	/**
	 * Set value of property keywords
	 *
	 * @param _value - new element value
	 */
	public void setKeywords(String[] _value) {
		keywords = _value;
	}

	/**
	 * Set single element of array keywords
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setKeywords(String _element, int _i) {
		keywords[_i] = _element;
	}

	/**
	 * Get value of property keywords
	 *
	 * @return - value of field keywords
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/**
	 * Get single element of array keywords
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array keywords
	 */
	public String getKeywords(int _i) {
		return keywords[_i];
	}

	/* *** Property prefix *** */

	private String prefix;

	/**
	 * Set value of property prefix
	 *
	 * @param _value - new field value
	 */
	public void setPrefix(String _value) {
		prefix = _value;
	}

	/**
	 * Get value of property prefix
	 *
	 * @return - value of field prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/* *** Property privacy *** */

	private Integer privacy;

	/**
	 * Set value of property privacy
	 *
	 * @param _value - new field value
	 */
	public void setPrivacy(Integer _value) {
		privacy = _value;
	}

	/**
	 * Get value of property privacy
	 *
	 * @return - value of field privacy
	 */
	public Integer getPrivacy() {
		return privacy;
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

		if (!(o instanceof LastDeviceImport_type) || !super.equals(o))
			return false;
		LastDeviceImport_type other = (LastDeviceImport_type) o;
		return ((getVolume() == null && other.getVolume() == null) || (getVolume() != null && getVolume()
				.equals(other.getVolume())))

				&& getTimestamp() == other.getTimestamp()

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& ((getOwner() == null && other.getOwner() == null) || (getOwner() != null && getOwner()
						.equals(other.getOwner())))

				&& ((getPath() == null && other.getPath() == null) || (getPath() != null && getPath()
						.equals(other.getPath())))

				&& ((getDetectDuplicates() == null && other
						.getDetectDuplicates() == null) || (getDetectDuplicates() != null && getDetectDuplicates()
						.equals(other.getDetectDuplicates())))

				&& ((getRemoveMedia() == null && other.getRemoveMedia() == null) || (getRemoveMedia() != null && getRemoveMedia()
						.equals(other.getRemoveMedia())))

				&& ((getSkipPolicy() == null && other.getSkipPolicy() == null) || (getSkipPolicy() != null && getSkipPolicy()
						.equals(other.getSkipPolicy())))

				&& ((getSkippedFormats() == null && other.getSkippedFormats() == null) || (getSkippedFormats() != null && getSkippedFormats()
						.equals(other.getSkippedFormats())))

				&& ((getTargetDir() == null && other.getTargetDir() == null) || (getTargetDir() != null && getTargetDir()
						.equals(other.getTargetDir())))

				&& ((getSubfolders() == null && other.getSubfolders() == null) || (getSubfolders() != null && getSubfolders()
						.equals(other.getSubfolders())))

				&& ((getDeepSubfolders() == null && other.getDeepSubfolders() == null) || (getDeepSubfolders() != null && getDeepSubfolders()
						.equals(other.getDeepSubfolders())))

				&& ((getSelectedTemplate() == null && other
						.getSelectedTemplate() == null) || (getSelectedTemplate() != null && getSelectedTemplate()
						.equals(other.getSelectedTemplate())))

				&& ((getCue() == null && other.getCue() == null) || (getCue() != null && getCue()
						.equals(other.getCue())))

				&& ((getKeywords() == null && other.getKeywords() == null) || (getKeywords() != null && getKeywords()
						.equals(other.getKeywords())))

				&& ((getPrefix() == null && other.getPrefix() == null) || (getPrefix() != null && getPrefix()
						.equals(other.getPrefix())))

				&& ((getPrivacy() == null && other.getPrivacy() == null) || (getPrivacy() != null && getPrivacy()
						.equals(other.getPrivacy())))

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

		int hashCode = -1688414344
				+ ((getVolume() == null) ? 0 : getVolume().hashCode());

		hashCode = 31 * hashCode
				+ (int) (getTimestamp() ^ (getTimestamp() >>> 32));

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode
				+ ((getOwner() == null) ? 0 : getOwner().hashCode());

		hashCode = 31 * hashCode
				+ ((getPath() == null) ? 0 : getPath().hashCode());

		hashCode = 31
				* hashCode
				+ ((getDetectDuplicates() == null) ? 0 : getDetectDuplicates()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getRemoveMedia() == null) ? 0 : getRemoveMedia().hashCode());

		hashCode = 31 * hashCode
				+ ((getSkipPolicy() == null) ? 0 : getSkipPolicy().hashCode());

		hashCode = 31
				* hashCode
				+ ((getSkippedFormats() == null) ? 0 : getSkippedFormats()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getTargetDir() == null) ? 0 : getTargetDir().hashCode());

		hashCode = 31 * hashCode
				+ ((getSubfolders() == null) ? 0 : getSubfolders().hashCode());

		hashCode = 31
				* hashCode
				+ ((getDeepSubfolders() == null) ? 0 : getDeepSubfolders()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getSelectedTemplate() == null) ? 0 : getSelectedTemplate()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getCue() == null) ? 0 : getCue().hashCode());

		hashCode = 31 * hashCode
				+ ((getKeywords() == null) ? 0 : getKeywords().hashCode());

		hashCode = 31 * hashCode
				+ ((getPrefix() == null) ? 0 : getPrefix().hashCode());

		hashCode = 31 * hashCode
				+ ((getPrivacy() == null) ? 0 : getPrivacy().hashCode());

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

		if (volume == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "volume"));

	}

}
