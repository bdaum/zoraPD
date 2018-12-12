package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset watchedFolder
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface WatchedFolder_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property uri
	 *
	 * @param _value - new element value
	 */
	public void setUri(String _value);

	/**
	 * Get value of property uri
	 *
	 * @return - value of field uri
	 */
	public String getUri();

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
	 * Set value of property lastObservation
	 *
	 * @param _value - new element value
	 */
	public void setLastObservation(long _value);

	/**
	 * Get value of property lastObservation
	 *
	 * @return - value of field lastObservation
	 */
	public long getLastObservation();

	/**
	 * Set value of property recursive
	 *
	 * @param _value - new element value
	 */
	public void setRecursive(boolean _value);

	/**
	 * Get value of property recursive
	 *
	 * @return - value of field recursive
	 */
	public boolean getRecursive();

	/**
	 * Set value of property filters
	 *
	 * @param _value - new element value
	 */
	public void setFilters(String _value);

	/**
	 * Get value of property filters
	 *
	 * @return - value of field filters
	 */
	public String getFilters();

	/**
	 * Set value of property transfer
	 *
	 * @param _value - new element value
	 */
	public void setTransfer(boolean _value);

	/**
	 * Get value of property transfer
	 *
	 * @return - value of field transfer
	 */
	public boolean getTransfer();

	/**
	 * Set value of property artist
	 *
	 * @param _value - new element value
	 */
	public void setArtist(String _value);

	/**
	 * Get value of property artist
	 *
	 * @return - value of field artist
	 */
	public String getArtist();

	/**
	 * Set value of property skipDuplicates
	 *
	 * @param _value - new element value
	 */
	public void setSkipDuplicates(boolean _value);

	/**
	 * Get value of property skipDuplicates
	 *
	 * @return - value of field skipDuplicates
	 */
	public boolean getSkipDuplicates();

	/**
	 * Set value of property skipPolicy
	 *
	 * @param _value - new element value
	 */
	public void setSkipPolicy(int _value);

	/**
	 * Get value of property skipPolicy
	 *
	 * @return - value of field skipPolicy
	 */
	public int getSkipPolicy();

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
	 * Set value of property subfolderPolicy
	 *
	 * @param _value - new element value
	 */
	public void setSubfolderPolicy(int _value);

	/**
	 * Get value of property subfolderPolicy
	 *
	 * @return - value of field subfolderPolicy
	 */
	public int getSubfolderPolicy();

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
	 * Set value of property fileSource
	 *
	 * @param _value - new element value
	 */
	public void setFileSource(int _value);

	/**
	 * Get value of property fileSource
	 *
	 * @return - value of field fileSource
	 */
	public int getFileSource();

	/**
	 * Set value of property tethered
	 *
	 * @param _value - new element value
	 */
	public void setTethered(boolean _value);

	/**
	 * Get value of property tethered
	 *
	 * @return - value of field tethered
	 */
	public boolean getTethered();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
