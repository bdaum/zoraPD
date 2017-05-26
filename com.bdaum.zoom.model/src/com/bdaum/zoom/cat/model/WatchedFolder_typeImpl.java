package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset watchedFolder
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class WatchedFolder_typeImpl extends AomObject implements
		WatchedFolder_type {

	static final long serialVersionUID = -193725441L;

	/* ----- Constructors ----- */

	public WatchedFolder_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param uri - Property
	 * @param volume - Property
	 * @param lastObservation - Property
	 * @param recursive - Property
	 * @param filters - Property
	 * @param transfer - Property
	 * @param artist - Property
	 * @param skipDuplicates - Property
	 * @param skipPolicy - Property
	 * @param targetDir - Property
	 * @param subfolderPolicy - Property
	 * @param selectedTemplate - Property
	 * @param cue - Property
	 * @param fileSource - Property
	 */
	public WatchedFolder_typeImpl(String uri, String volume,
			long lastObservation, boolean recursive, String filters,
			boolean transfer, String artist, boolean skipDuplicates,
			int skipPolicy, String targetDir, int subfolderPolicy,
			String selectedTemplate, String cue, int fileSource) {
		super();
		this.uri = uri;
		this.volume = volume;
		this.lastObservation = lastObservation;
		this.recursive = recursive;
		this.filters = filters;
		this.transfer = transfer;
		this.artist = artist;
		this.skipDuplicates = skipDuplicates;
		this.skipPolicy = skipPolicy;
		this.targetDir = targetDir;
		this.subfolderPolicy = subfolderPolicy;
		this.selectedTemplate = selectedTemplate;
		this.cue = cue;
		this.fileSource = fileSource;

	}

	/* ----- Fields ----- */

	/* *** Property uri *** */

	private String uri = AomConstants.INIT_String;

	/**
	 * Set value of property uri
	 *
	 * @param _value - new field value
	 */
	public void setUri(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "uri"));
		uri = _value;
	}

	/**
	 * Get value of property uri
	 *
	 * @return - value of field uri
	 */
	public String getUri() {
		return uri;
	}

	/* *** Property volume *** */

	private String volume;

	/**
	 * Set value of property volume
	 *
	 * @param _value - new field value
	 */
	public void setVolume(String _value) {
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

	/* *** Property lastObservation *** */

	private long lastObservation;

	/**
	 * Set value of property lastObservation
	 *
	 * @param _value - new field value
	 */
	public void setLastObservation(long _value) {
		lastObservation = _value;
	}

	/**
	 * Get value of property lastObservation
	 *
	 * @return - value of field lastObservation
	 */
	public long getLastObservation() {
		return lastObservation;
	}

	/* *** Property recursive *** */

	private boolean recursive;

	/**
	 * Set value of property recursive
	 *
	 * @param _value - new field value
	 */
	public void setRecursive(boolean _value) {
		recursive = _value;
	}

	/**
	 * Get value of property recursive
	 *
	 * @return - value of field recursive
	 */
	public boolean getRecursive() {
		return recursive;
	}

	/* *** Property filters *** */

	private String filters;

	/**
	 * Set value of property filters
	 *
	 * @param _value - new field value
	 */
	public void setFilters(String _value) {
		filters = _value;
	}

	/**
	 * Get value of property filters
	 *
	 * @return - value of field filters
	 */
	public String getFilters() {
		return filters;
	}

	/* *** Property transfer *** */

	private boolean transfer;

	/**
	 * Set value of property transfer
	 *
	 * @param _value - new field value
	 */
	public void setTransfer(boolean _value) {
		transfer = _value;
	}

	/**
	 * Get value of property transfer
	 *
	 * @return - value of field transfer
	 */
	public boolean getTransfer() {
		return transfer;
	}

	/* *** Property artist *** */

	private String artist;

	/**
	 * Set value of property artist
	 *
	 * @param _value - new field value
	 */
	public void setArtist(String _value) {
		artist = _value;
	}

	/**
	 * Get value of property artist
	 *
	 * @return - value of field artist
	 */
	public String getArtist() {
		return artist;
	}

	/* *** Property skipDuplicates *** */

	private boolean skipDuplicates;

	/**
	 * Set value of property skipDuplicates
	 *
	 * @param _value - new field value
	 */
	public void setSkipDuplicates(boolean _value) {
		skipDuplicates = _value;
	}

	/**
	 * Get value of property skipDuplicates
	 *
	 * @return - value of field skipDuplicates
	 */
	public boolean getSkipDuplicates() {
		return skipDuplicates;
	}

	/* *** Property skipPolicy *** */

	private int skipPolicy;

	/**
	 * Set value of property skipPolicy
	 *
	 * @param _value - new field value
	 */
	public void setSkipPolicy(int _value) {
		skipPolicy = _value;
	}

	/**
	 * Get value of property skipPolicy
	 *
	 * @return - value of field skipPolicy
	 */
	public int getSkipPolicy() {
		return skipPolicy;
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

	/* *** Property subfolderPolicy *** */

	private int subfolderPolicy;

	/**
	 * Set value of property subfolderPolicy
	 *
	 * @param _value - new field value
	 */
	public void setSubfolderPolicy(int _value) {
		subfolderPolicy = _value;
	}

	/**
	 * Get value of property subfolderPolicy
	 *
	 * @return - value of field subfolderPolicy
	 */
	public int getSubfolderPolicy() {
		return subfolderPolicy;
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

	/* *** Property fileSource *** */

	private int fileSource;

	/**
	 * Set value of property fileSource
	 *
	 * @param _value - new field value
	 */
	public void setFileSource(int _value) {
		fileSource = _value;
	}

	/**
	 * Get value of property fileSource
	 *
	 * @return - value of field fileSource
	 */
	public int getFileSource() {
		return fileSource;
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

		if (!(o instanceof WatchedFolder_type) || !super.equals(o))
			return false;
		WatchedFolder_type other = (WatchedFolder_type) o;
		return ((getUri() == null && other.getUri() == null) || (getUri() != null && getUri()
				.equals(other.getUri())))

				&& ((getVolume() == null && other.getVolume() == null) || (getVolume() != null && getVolume()
						.equals(other.getVolume())))

				&& getLastObservation() == other.getLastObservation()

				&& getRecursive() == other.getRecursive()

				&& ((getFilters() == null && other.getFilters() == null) || (getFilters() != null && getFilters()
						.equals(other.getFilters())))

				&& getTransfer() == other.getTransfer()

				&& ((getArtist() == null && other.getArtist() == null) || (getArtist() != null && getArtist()
						.equals(other.getArtist())))

				&& getSkipDuplicates() == other.getSkipDuplicates()

				&& getSkipPolicy() == other.getSkipPolicy()

				&& ((getTargetDir() == null && other.getTargetDir() == null) || (getTargetDir() != null && getTargetDir()
						.equals(other.getTargetDir())))

				&& getSubfolderPolicy() == other.getSubfolderPolicy()

				&& ((getSelectedTemplate() == null && other
						.getSelectedTemplate() == null) || (getSelectedTemplate() != null && getSelectedTemplate()
						.equals(other.getSelectedTemplate())))

				&& ((getCue() == null && other.getCue() == null) || (getCue() != null && getCue()
						.equals(other.getCue())))

				&& getFileSource() == other.getFileSource()

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

		int hashCode = 1245484515 + ((getUri() == null) ? 0 : getUri()
				.hashCode());

		hashCode = 31 * hashCode
				+ ((getVolume() == null) ? 0 : getVolume().hashCode());

		hashCode = 31 * hashCode
				+ (int) (getLastObservation() ^ (getLastObservation() >>> 32));

		hashCode = 31 * hashCode + (getRecursive() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getFilters() == null) ? 0 : getFilters().hashCode());

		hashCode = 31 * hashCode + (getTransfer() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getArtist() == null) ? 0 : getArtist().hashCode());

		hashCode = 31 * hashCode + (getSkipDuplicates() ? 1231 : 1237);

		hashCode = 31 * hashCode + getSkipPolicy();

		hashCode = 31 * hashCode
				+ ((getTargetDir() == null) ? 0 : getTargetDir().hashCode());

		hashCode = 31 * hashCode + getSubfolderPolicy();

		hashCode = 31
				* hashCode
				+ ((getSelectedTemplate() == null) ? 0 : getSelectedTemplate()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getCue() == null) ? 0 : getCue().hashCode());

		hashCode = 31 * hashCode + getFileSource();

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

		if (uri == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "uri"));

	}

}
