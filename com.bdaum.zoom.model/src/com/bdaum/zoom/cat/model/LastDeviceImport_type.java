package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset lastDeviceImport
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface LastDeviceImport_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property volume
	 *
	 * @param _value - new element value
	 */
	public void setVolume(String _value);

	/**
	 * Get value of property volume
	 *
	 * @return - value of field volume
	 */
	public String getVolume();

	/**
	 * Set value of property timestamp
	 *
	 * @param _value - new element value
	 */
	public void setTimestamp(long _value);

	/**
	 * Get value of property timestamp
	 *
	 * @return - value of field timestamp
	 */
	public long getTimestamp();

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
	 * Set value of property owner
	 *
	 * @param _value - new element value
	 */
	public void setOwner(String _value);

	/**
	 * Get value of property owner
	 *
	 * @return - value of field owner
	 */
	public String getOwner();

	/**
	 * Set value of property path
	 *
	 * @param _value - new element value
	 */
	public void setPath(String _value);

	/**
	 * Get value of property path
	 *
	 * @return - value of field path
	 */
	public String getPath();

	/**
	 * Set value of property detectDuplicates
	 *
	 * @param _value - new element value
	 */
	public void setDetectDuplicates(Boolean _value);

	/**
	 * Get value of property detectDuplicates
	 *
	 * @return - value of field detectDuplicates
	 */
	public Boolean getDetectDuplicates();

	/**
	 * Set value of property removeMedia
	 *
	 * @param _value - new element value
	 */
	public void setRemoveMedia(Boolean _value);

	/**
	 * Get value of property removeMedia
	 *
	 * @return - value of field removeMedia
	 */
	public Boolean getRemoveMedia();

	/**
	 * Set value of property skipPolicy
	 *
	 * @param _value - new element value
	 */
	public void setSkipPolicy(Integer _value);

	/**
	 * Get value of property skipPolicy
	 *
	 * @return - value of field skipPolicy
	 */
	public Integer getSkipPolicy();

	/**
	 * Set value of property skippedFormats
	 *
	 * @param _value - new element value
	 */
	public void setSkippedFormats(String[] _value);

	/**
	 * Set single element of array skippedFormats
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSkippedFormats(String _element, int _i);

	/**
	 * Get value of property skippedFormats
	 *
	 * @return - value of field skippedFormats
	 */
	public String[] getSkippedFormats();

	/**
	 * Get single element of array skippedFormats
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array skippedFormats
	 */
	public String getSkippedFormats(int _i);

	/**
	 * Set value of property targetDir
	 *
	 * @param _value - new element value
	 */
	public void setTargetDir(String _value);

	/**
	 * Get value of property targetDir
	 *
	 * @return - value of field targetDir
	 */
	public String getTargetDir();

	/**
	 * Set value of property subfolders
	 *
	 * @param _value - new element value
	 */
	public void setSubfolders(Integer _value);

	/**
	 * Get value of property subfolders
	 *
	 * @return - value of field subfolders
	 */
	public Integer getSubfolders();

	/**
	 * Set value of property deepSubfolders
	 *
	 * @param _value - new element value
	 */
	public void setDeepSubfolders(Boolean _value);

	/**
	 * Get value of property deepSubfolders
	 *
	 * @return - value of field deepSubfolders
	 */
	public Boolean getDeepSubfolders();

	/**
	 * Set value of property selectedTemplate
	 *
	 * @param _value - new element value
	 */
	public void setSelectedTemplate(String _value);

	/**
	 * Get value of property selectedTemplate
	 *
	 * @return - value of field selectedTemplate
	 */
	public String getSelectedTemplate();

	/**
	 * Set value of property cue
	 *
	 * @param _value - new element value
	 */
	public void setCue(String _value);

	/**
	 * Get value of property cue
	 *
	 * @return - value of field cue
	 */
	public String getCue();

	/**
	 * Set value of property keywords
	 *
	 * @param _value - new element value
	 */
	public void setKeywords(String[] _value);

	/**
	 * Set single element of array keywords
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setKeywords(String _element, int _i);

	/**
	 * Get value of property keywords
	 *
	 * @return - value of field keywords
	 */
	public String[] getKeywords();

	/**
	 * Get single element of array keywords
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array keywords
	 */
	public String getKeywords(int _i);

	/**
	 * Set value of property prefix
	 *
	 * @param _value - new element value
	 */
	public void setPrefix(String _value);

	/**
	 * Get value of property prefix
	 *
	 * @return - value of field prefix
	 */
	public String getPrefix();

	/**
	 * Set value of property privacy
	 *
	 * @param _value - new element value
	 */
	public void setPrivacy(Integer _value);

	/**
	 * Get value of property privacy
	 *
	 * @return - value of field privacy
	 */
	public Integer getPrivacy();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
