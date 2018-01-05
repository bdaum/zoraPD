package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset meta
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Meta_typeImpl extends AomObject implements Meta_type {

	static final long serialVersionUID = -2906598506L;

	/* ----- Constructors ----- */

	public Meta_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param version - Property
	 * @param relevantLireVersion - Property
	 * @param creationDate - Property
	 * @param owner - Property
	 * @param themeID - Property
	 * @param description - Property
	 * @param userFieldLabel1 - Property
	 * @param userFieldLabel2 - Property
	 * @param timeline - Property
	 * @param locationFolders - Property
	 * @param lastImport - Property
	 * @param lastSequenceNo - Property
	 * @param lastYearSequenceNo - Property
	 * @param lastBackup - Property
	 * @param lastBackupFolder - Property
	 * @param backupLocation - Property
	 * @param lastSessionEnd - Property
	 * @param thumbnailResolution - Property
	 * @param thumbnailFromPreview - Property
	 * @param lastSelection - Property
	 * @param lastCollection - Property
	 * @param pauseFolderWatch - Property
	 * @param folderWatchLatency - Property
	 * @param cleaned - Property
	 * @param readonly - Property
	 * @param autoWatch - Property
	 * @param sharpen - Property
	 * @param locale - Property
	 * @param platform - Property
	 * @param lastPicasaScan - Property
	 * @param picasaScannerVersion - Property
	 * @param cumulateImports - Property
	 * @param webpCompression - Property
	 * @param jpegQuality - Property
	 * @param noIndex - Property
	 * @param personsToKeywords - Property
	 * @param lastWatchedFolderScan - Property
	 */
	public Meta_typeImpl(int version, int relevantLireVersion,
			Date creationDate, String owner, String themeID,
			String description, String userFieldLabel1, String userFieldLabel2,
			String timeline, String locationFolders, Date lastImport,
			int lastSequenceNo, int lastYearSequenceNo, Date lastBackup,
			String lastBackupFolder, String backupLocation,
			Date lastSessionEnd, String thumbnailResolution,
			boolean thumbnailFromPreview, String lastSelection,
			String lastCollection, boolean pauseFolderWatch,
			int folderWatchLatency, boolean cleaned, boolean readonly,
			boolean autoWatch, int sharpen, String locale, String platform,
			Date lastPicasaScan, int picasaScannerVersion,
			boolean cumulateImports, boolean webpCompression, int jpegQuality,
			boolean noIndex, Boolean personsToKeywords,
			long lastWatchedFolderScan) {
		super();
		this.version = version;
		this.relevantLireVersion = relevantLireVersion;
		this.creationDate = creationDate;
		this.owner = owner;
		this.themeID = themeID;
		this.description = description;
		this.userFieldLabel1 = userFieldLabel1;
		this.userFieldLabel2 = userFieldLabel2;
		this.timeline = timeline;
		this.locationFolders = locationFolders;
		this.lastImport = lastImport;
		this.lastSequenceNo = lastSequenceNo;
		this.lastYearSequenceNo = lastYearSequenceNo;
		this.lastBackup = lastBackup;
		this.lastBackupFolder = lastBackupFolder;
		this.backupLocation = backupLocation;
		this.lastSessionEnd = lastSessionEnd;
		this.thumbnailResolution = thumbnailResolution;
		this.thumbnailFromPreview = thumbnailFromPreview;
		this.lastSelection = lastSelection;
		this.lastCollection = lastCollection;
		this.pauseFolderWatch = pauseFolderWatch;
		this.folderWatchLatency = folderWatchLatency;
		this.cleaned = cleaned;
		this.readonly = readonly;
		this.autoWatch = autoWatch;
		this.sharpen = sharpen;
		this.locale = locale;
		this.platform = platform;
		this.lastPicasaScan = lastPicasaScan;
		this.picasaScannerVersion = picasaScannerVersion;
		this.cumulateImports = cumulateImports;
		this.webpCompression = webpCompression;
		this.jpegQuality = jpegQuality;
		this.noIndex = noIndex;
		this.personsToKeywords = personsToKeywords;
		this.lastWatchedFolderScan = lastWatchedFolderScan;

	}

	/* ----- Fields ----- */

	/* *** Property version *** */

	private int version;

	/**
	 * Set value of property version
	 *
	 * @param _value - new field value
	 */
	public void setVersion(int _value) {
		version = _value;
	}

	/**
	 * Get value of property version
	 *
	 * @return - value of field version
	 */
	public int getVersion() {
		return version;
	}

	/* *** Property relevantLireVersion *** */

	private int relevantLireVersion;

	/**
	 * Set value of property relevantLireVersion
	 *
	 * @param _value - new field value
	 */
	public void setRelevantLireVersion(int _value) {
		relevantLireVersion = _value;
	}

	/**
	 * Get value of property relevantLireVersion
	 *
	 * @return - value of field relevantLireVersion
	 */
	public int getRelevantLireVersion() {
		return relevantLireVersion;
	}

	/* *** Property creationDate *** */

	private Date creationDate = new Date();

	/**
	 * Set value of property creationDate
	 *
	 * @param _value - new field value
	 */
	public void setCreationDate(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "creationDate"));
		creationDate = _value;
	}

	/**
	 * Get value of property creationDate
	 *
	 * @return - value of field creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/* *** Property owner *** */

	private String owner = AomConstants.INIT_String;

	/**
	 * Set value of property owner
	 *
	 * @param _value - new field value
	 */
	public void setOwner(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "owner"));
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

	/* *** Property themeID *** */

	private String themeID = AomConstants.INIT_String;

	/**
	 * Set value of property themeID
	 *
	 * @param _value - new field value
	 */
	public void setThemeID(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "themeID"));
		themeID = _value;
	}

	/**
	 * Get value of property themeID
	 *
	 * @return - value of field themeID
	 */
	public String getThemeID() {
		return themeID;
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

	/* *** Property userFieldLabel1 *** */

	private String userFieldLabel1 = AomConstants.INIT_String;

	/**
	 * Set value of property userFieldLabel1
	 *
	 * @param _value - new field value
	 */
	public void setUserFieldLabel1(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "userFieldLabel1"));
		userFieldLabel1 = _value;
	}

	/**
	 * Get value of property userFieldLabel1
	 *
	 * @return - value of field userFieldLabel1
	 */
	public String getUserFieldLabel1() {
		return userFieldLabel1;
	}

	/* *** Property userFieldLabel2 *** */

	private String userFieldLabel2 = AomConstants.INIT_String;

	/**
	 * Set value of property userFieldLabel2
	 *
	 * @param _value - new field value
	 */
	public void setUserFieldLabel2(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "userFieldLabel2"));
		userFieldLabel2 = _value;
	}

	/**
	 * Get value of property userFieldLabel2
	 *
	 * @return - value of field userFieldLabel2
	 */
	public String getUserFieldLabel2() {
		return userFieldLabel2;
	}

	/* *** Property colorLabels *** */

	private AomList<String> colorLabels = new FastArrayList<String>(
			"colorLabels", PackageInterface.Meta_colorLabels, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property colorLabels
	 *
	 * @param _value - new element value
	 */
	public void setColorLabels(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "colorLabels"));
		colorLabels = _value;
	}

	/**
	 * Set value of property colorLabels
	 *
	 * @param _value - new element value
	 */
	public void setColorLabels(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "colorLabels"));
		colorLabels = new FastArrayList<String>(_value, "colorLabels",
				PackageInterface.Meta_colorLabels, 0, Integer.MAX_VALUE, null,
				null);
	}

	/**
	 * Set single element of list colorLabels
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setColorLabels(String _element, int _i) {
		colorLabels.set(_i, _element);
	}

	/**
	 * Add an element to list colorLabels
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addColorLabels(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "ColorLabels._element"));

		return colorLabels.add(_element);
	}

	/**
	 * Remove an element from list colorLabels
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeColorLabels(String _element) {
		return colorLabels.remove(_element);
	}

	/**
	 * Make colorLabels empty 
	 */
	public void clearColorLabels() {
		colorLabels.clear();
	}

	/**
	 * Get value of property colorLabels
	 *
	 * @return - value of field colorLabels
	 */
	public AomList<String> getColorLabels() {
		return colorLabels;
	}

	/**
	 * Get single element of list colorLabels
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list colorLabels
	 */
	public String getColorLabels(int _i) {
		return colorLabels.get(_i);
	}

	/* *** Property keywords *** */

	private AomSet<String> keywords = new FastHashSet<String>("keywords",
			PackageInterface.Meta_keywords, 0, Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property keywords
	 *
	 * @param _value - new element value
	 */
	public void setKeywords(AomSet<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "keywords"));
		keywords = _value;
	}

	/**
	 * Set value of property keywords
	 *
	 * @param _value - new element value
	 */
	public void setKeywords(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "keywords"));
		keywords = new FastHashSet<String>(_value, "keywords",
				PackageInterface.Meta_keywords, 0, Integer.MAX_VALUE, null,
				null);
	}

	/**
	 * Add an element to set keywords
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addKeywords(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Keywords._element"));

		return keywords.add(_element);
	}

	/**
	 * Remove an element from set keywords
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removeKeywords(String _element) {
		return keywords.remove(_element);
	}

	/**
	 * Make keywords empty 
	 */
	public void clearKeywords() {
		keywords.clear();
	}

	/**
	 * Get value of property keywords
	 *
	 * @return - value of field keywords
	 */
	public AomSet<String> getKeywords() {
		return keywords;
	}

	/**
	 * Test if set keywords contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsKeywords(String _element) {
		return keywords.contains(_element);
	}

	/* *** Property timeline *** */

	private String timeline = AomConstants.INIT_String;

	/**
	 * Set value of property timeline
	 *
	 * @param _value - new field value
	 */
	public void setTimeline(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "timeline"));
		String _valueIntern = _value.intern();
		if (!(_valueIntern == timeline_no || _valueIntern == timeline_year
				|| _valueIntern == timeline_month
				|| _valueIntern == timeline_day
				|| _valueIntern == timeline_week || _valueIntern == timeline_weekAndDay))
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_VIOLATES_ENUMERATION,
					String.valueOf(_value)));

		timeline = _value;
	}

	/**
	 * Get value of property timeline
	 *
	 * @return - value of field timeline
	 */
	public String getTimeline() {
		return timeline;
	}

	/* *** Property locationFolders *** */

	private String locationFolders = AomConstants.INIT_String;

	/**
	 * Set value of property locationFolders
	 *
	 * @param _value - new field value
	 */
	public void setLocationFolders(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "locationFolders"));
		String _valueIntern = _value.intern();
		if (!(_valueIntern == locationFolders_no
				|| _valueIntern == locationFolders_country
				|| _valueIntern == locationFolders_state || _valueIntern == locationFolders_city))
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_VIOLATES_ENUMERATION,
					String.valueOf(_value)));

		locationFolders = _value;
	}

	/**
	 * Get value of property locationFolders
	 *
	 * @return - value of field locationFolders
	 */
	public String getLocationFolders() {
		return locationFolders;
	}

	/* *** Property lastImport *** */

	private Date lastImport = new Date();

	/**
	 * Set value of property lastImport
	 *
	 * @param _value - new field value
	 */
	public void setLastImport(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastImport"));
		lastImport = _value;
	}

	/**
	 * Get value of property lastImport
	 *
	 * @return - value of field lastImport
	 */
	public Date getLastImport() {
		return lastImport;
	}

	/* *** Property lastSequenceNo *** */

	private int lastSequenceNo;

	/**
	 * Set value of property lastSequenceNo
	 *
	 * @param _value - new field value
	 */
	public void setLastSequenceNo(int _value) {
		lastSequenceNo = _value;
	}

	/**
	 * Get value of property lastSequenceNo
	 *
	 * @return - value of field lastSequenceNo
	 */
	public int getLastSequenceNo() {
		return lastSequenceNo;
	}

	/* *** Property lastYearSequenceNo *** */

	private int lastYearSequenceNo;

	/**
	 * Set value of property lastYearSequenceNo
	 *
	 * @param _value - new field value
	 */
	public void setLastYearSequenceNo(int _value) {
		lastYearSequenceNo = _value;
	}

	/**
	 * Get value of property lastYearSequenceNo
	 *
	 * @return - value of field lastYearSequenceNo
	 */
	public int getLastYearSequenceNo() {
		return lastYearSequenceNo;
	}

	/* *** Property lastBackup *** */

	private Date lastBackup = new Date();

	/**
	 * Set value of property lastBackup
	 *
	 * @param _value - new field value
	 */
	public void setLastBackup(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastBackup"));
		lastBackup = _value;
	}

	/**
	 * Get value of property lastBackup
	 *
	 * @return - value of field lastBackup
	 */
	public Date getLastBackup() {
		return lastBackup;
	}

	/* *** Property lastBackupFolder *** */

	private String lastBackupFolder;

	/**
	 * Set value of property lastBackupFolder
	 *
	 * @param _value - new field value
	 */
	public void setLastBackupFolder(String _value) {
		lastBackupFolder = _value;
	}

	/**
	 * Get value of property lastBackupFolder
	 *
	 * @return - value of field lastBackupFolder
	 */
	public String getLastBackupFolder() {
		return lastBackupFolder;
	}

	/* *** Property backupLocation *** */

	private String backupLocation;

	/**
	 * Set value of property backupLocation
	 *
	 * @param _value - new field value
	 */
	public void setBackupLocation(String _value) {
		backupLocation = _value;
	}

	/**
	 * Get value of property backupLocation
	 *
	 * @return - value of field backupLocation
	 */
	public String getBackupLocation() {
		return backupLocation;
	}

	/* *** Property lastSessionEnd *** */

	private Date lastSessionEnd = new Date();

	/**
	 * Set value of property lastSessionEnd
	 *
	 * @param _value - new field value
	 */
	public void setLastSessionEnd(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastSessionEnd"));
		lastSessionEnd = _value;
	}

	/**
	 * Get value of property lastSessionEnd
	 *
	 * @return - value of field lastSessionEnd
	 */
	public Date getLastSessionEnd() {
		return lastSessionEnd;
	}

	/* *** Property thumbnailResolution *** */

	private String thumbnailResolution = AomConstants.INIT_String;

	/**
	 * Set value of property thumbnailResolution
	 *
	 * @param _value - new field value
	 */
	public void setThumbnailResolution(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "thumbnailResolution"));
		String _valueIntern = _value.intern();
		if (!(_valueIntern == thumbnailResolution_low
				|| _valueIntern == thumbnailResolution_medium
				|| _valueIntern == thumbnailResolution_high || _valueIntern == thumbnailResolution_veryHigh))
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_VIOLATES_ENUMERATION,
					String.valueOf(_value)));

		thumbnailResolution = _value;
	}

	/**
	 * Get value of property thumbnailResolution
	 *
	 * @return - value of field thumbnailResolution
	 */
	public String getThumbnailResolution() {
		return thumbnailResolution;
	}

	/* *** Property thumbnailFromPreview *** */

	private boolean thumbnailFromPreview;

	/**
	 * Set value of property thumbnailFromPreview
	 *
	 * @param _value - new field value
	 */
	public void setThumbnailFromPreview(boolean _value) {
		thumbnailFromPreview = _value;
	}

	/**
	 * Get value of property thumbnailFromPreview
	 *
	 * @return - value of field thumbnailFromPreview
	 */
	public boolean getThumbnailFromPreview() {
		return thumbnailFromPreview;
	}

	/* *** Property lastSelection *** */

	private String lastSelection;

	/**
	 * Set value of property lastSelection
	 *
	 * @param _value - new field value
	 */
	public void setLastSelection(String _value) {
		lastSelection = _value;
	}

	/**
	 * Get value of property lastSelection
	 *
	 * @return - value of field lastSelection
	 */
	public String getLastSelection() {
		return lastSelection;
	}

	/* *** Property lastExpansion *** */

	private AomList<String> lastExpansion = new FastArrayList<String>(
			"lastExpansion", PackageInterface.Meta_lastExpansion, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property lastExpansion
	 *
	 * @param _value - new element value
	 */
	public void setLastExpansion(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastExpansion"));
		lastExpansion = _value;
	}

	/**
	 * Set value of property lastExpansion
	 *
	 * @param _value - new element value
	 */
	public void setLastExpansion(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastExpansion"));
		lastExpansion = new FastArrayList<String>(_value, "lastExpansion",
				PackageInterface.Meta_lastExpansion, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Set single element of list lastExpansion
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setLastExpansion(String _element, int _i) {
		lastExpansion.set(_i, _element);
	}

	/**
	 * Add an element to list lastExpansion
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addLastExpansion(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "LastExpansion._element"));

		return lastExpansion.add(_element);
	}

	/**
	 * Remove an element from list lastExpansion
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeLastExpansion(String _element) {
		return lastExpansion.remove(_element);
	}

	/**
	 * Make lastExpansion empty 
	 */
	public void clearLastExpansion() {
		lastExpansion.clear();
	}

	/**
	 * Get value of property lastExpansion
	 *
	 * @return - value of field lastExpansion
	 */
	public AomList<String> getLastExpansion() {
		return lastExpansion;
	}

	/**
	 * Get single element of list lastExpansion
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list lastExpansion
	 */
	public String getLastExpansion(int _i) {
		return lastExpansion.get(_i);
	}

	/* *** Property lastCollection *** */

	private String lastCollection;

	/**
	 * Set value of property lastCollection
	 *
	 * @param _value - new field value
	 */
	public void setLastCollection(String _value) {
		lastCollection = _value;
	}

	/**
	 * Get value of property lastCollection
	 *
	 * @return - value of field lastCollection
	 */
	public String getLastCollection() {
		return lastCollection;
	}

	/* *** Property pauseFolderWatch *** */

	private boolean pauseFolderWatch;

	/**
	 * Set value of property pauseFolderWatch
	 *
	 * @param _value - new field value
	 */
	public void setPauseFolderWatch(boolean _value) {
		pauseFolderWatch = _value;
	}

	/**
	 * Get value of property pauseFolderWatch
	 *
	 * @return - value of field pauseFolderWatch
	 */
	public boolean getPauseFolderWatch() {
		return pauseFolderWatch;
	}

	/* *** Property folderWatchLatency *** */

	private int folderWatchLatency;

	/**
	 * Set value of property folderWatchLatency
	 *
	 * @param _value - new field value
	 */
	public void setFolderWatchLatency(int _value) {
		folderWatchLatency = _value;
	}

	/**
	 * Get value of property folderWatchLatency
	 *
	 * @return - value of field folderWatchLatency
	 */
	public int getFolderWatchLatency() {
		return folderWatchLatency;
	}

	/* *** Property cleaned *** */

	private boolean cleaned;

	/**
	 * Set value of property cleaned
	 *
	 * @param _value - new field value
	 */
	public void setCleaned(boolean _value) {
		cleaned = _value;
	}

	/**
	 * Get value of property cleaned
	 *
	 * @return - value of field cleaned
	 */
	public boolean getCleaned() {
		return cleaned;
	}

	/* *** Property postponed *** */

	private AomSet<String> postponed = new FastHashSet<String>("postponed",
			PackageInterface.Meta_postponed, 0, Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property postponed
	 *
	 * @param _value - new element value
	 */
	public void setPostponed(AomSet<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "postponed"));
		postponed = _value;
	}

	/**
	 * Set value of property postponed
	 *
	 * @param _value - new element value
	 */
	public void setPostponed(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "postponed"));
		postponed = new FastHashSet<String>(_value, "postponed",
				PackageInterface.Meta_postponed, 0, Integer.MAX_VALUE, null,
				null);
	}

	/**
	 * Add an element to set postponed
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addPostponed(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Postponed._element"));

		return postponed.add(_element);
	}

	/**
	 * Remove an element from set postponed
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removePostponed(String _element) {
		return postponed.remove(_element);
	}

	/**
	 * Make postponed empty 
	 */
	public void clearPostponed() {
		postponed.clear();
	}

	/**
	 * Get value of property postponed
	 *
	 * @return - value of field postponed
	 */
	public AomSet<String> getPostponed() {
		return postponed;
	}

	/**
	 * Test if set postponed contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsPostponed(String _element) {
		return postponed.contains(_element);
	}

	/* *** Property postponedNaming *** */

	private AomSet<String> postponedNaming = new FastHashSet<String>(
			"postponedNaming", PackageInterface.Meta_postponedNaming, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property postponedNaming
	 *
	 * @param _value - new element value
	 */
	public void setPostponedNaming(AomSet<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "postponedNaming"));
		postponedNaming = _value;
	}

	/**
	 * Set value of property postponedNaming
	 *
	 * @param _value - new element value
	 */
	public void setPostponedNaming(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "postponedNaming"));
		postponedNaming = new FastHashSet<String>(_value, "postponedNaming",
				PackageInterface.Meta_postponedNaming, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Add an element to set postponedNaming
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addPostponedNaming(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(
					ModelMessages.getString(ErrorMessages.ARGUMENT_NOT_NULL,
							"PostponedNaming._element"));

		return postponedNaming.add(_element);
	}

	/**
	 * Remove an element from set postponedNaming
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removePostponedNaming(String _element) {
		return postponedNaming.remove(_element);
	}

	/**
	 * Make postponedNaming empty 
	 */
	public void clearPostponedNaming() {
		postponedNaming.clear();
	}

	/**
	 * Get value of property postponedNaming
	 *
	 * @return - value of field postponedNaming
	 */
	public AomSet<String> getPostponedNaming() {
		return postponedNaming;
	}

	/**
	 * Test if set postponedNaming contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsPostponedNaming(String _element) {
		return postponedNaming.contains(_element);
	}

	/* *** Property readonly *** */

	private boolean readonly;

	/**
	 * Set value of property readonly
	 *
	 * @param _value - new field value
	 */
	public void setReadonly(boolean _value) {
		readonly = _value;
	}

	/**
	 * Get value of property readonly
	 *
	 * @return - value of field readonly
	 */
	public boolean getReadonly() {
		return readonly;
	}

	/* *** Property autoWatch *** */

	private boolean autoWatch;

	/**
	 * Set value of property autoWatch
	 *
	 * @param _value - new field value
	 */
	public void setAutoWatch(boolean _value) {
		autoWatch = _value;
	}

	/**
	 * Get value of property autoWatch
	 *
	 * @return - value of field autoWatch
	 */
	public boolean getAutoWatch() {
		return autoWatch;
	}

	/* *** Property sharpen *** */

	private int sharpen;

	/**
	 * Set value of property sharpen
	 *
	 * @param _value - new field value
	 */
	public void setSharpen(int _value) {
		sharpen = _value;
	}

	/**
	 * Get value of property sharpen
	 *
	 * @return - value of field sharpen
	 */
	public int getSharpen() {
		return sharpen;
	}

	/* *** Property locale *** */

	private String locale;

	/**
	 * Set value of property locale
	 *
	 * @param _value - new field value
	 */
	public void setLocale(String _value) {
		locale = _value;
	}

	/**
	 * Get value of property locale
	 *
	 * @return - value of field locale
	 */
	public String getLocale() {
		return locale;
	}

	/* *** Property platform *** */

	private String platform;

	/**
	 * Set value of property platform
	 *
	 * @param _value - new field value
	 */
	public void setPlatform(String _value) {
		platform = _value;
	}

	/**
	 * Get value of property platform
	 *
	 * @return - value of field platform
	 */
	public String getPlatform() {
		return platform;
	}

	/* *** Property lastPicasaScan *** */

	private Date lastPicasaScan;

	/**
	 * Set value of property lastPicasaScan
	 *
	 * @param _value - new field value
	 */
	public void setLastPicasaScan(Date _value) {
		lastPicasaScan = _value;
	}

	/**
	 * Get value of property lastPicasaScan
	 *
	 * @return - value of field lastPicasaScan
	 */
	public Date getLastPicasaScan() {
		return lastPicasaScan;
	}

	/* *** Property picasaScannerVersion *** */

	private int picasaScannerVersion;

	/**
	 * Set value of property picasaScannerVersion
	 *
	 * @param _value - new field value
	 */
	public void setPicasaScannerVersion(int _value) {
		picasaScannerVersion = _value;
	}

	/**
	 * Get value of property picasaScannerVersion
	 *
	 * @return - value of field picasaScannerVersion
	 */
	public int getPicasaScannerVersion() {
		return picasaScannerVersion;
	}

	/* *** Property cumulateImports *** */

	private boolean cumulateImports;

	/**
	 * Set value of property cumulateImports
	 *
	 * @param _value - new field value
	 */
	public void setCumulateImports(boolean _value) {
		cumulateImports = _value;
	}

	/**
	 * Get value of property cumulateImports
	 *
	 * @return - value of field cumulateImports
	 */
	public boolean getCumulateImports() {
		return cumulateImports;
	}

	/* *** Property webpCompression *** */

	private boolean webpCompression;

	/**
	 * Set value of property webpCompression
	 *
	 * @param _value - new field value
	 */
	public void setWebpCompression(boolean _value) {
		webpCompression = _value;
	}

	/**
	 * Get value of property webpCompression
	 *
	 * @return - value of field webpCompression
	 */
	public boolean getWebpCompression() {
		return webpCompression;
	}

	/* *** Property jpegQuality *** */

	private int jpegQuality;

	/**
	 * Set value of property jpegQuality
	 *
	 * @param _value - new field value
	 */
	public void setJpegQuality(int _value) {
		jpegQuality = _value;
	}

	/**
	 * Get value of property jpegQuality
	 *
	 * @return - value of field jpegQuality
	 */
	public int getJpegQuality() {
		return jpegQuality;
	}

	/* *** Property noIndex *** */

	private boolean noIndex;

	/**
	 * Set value of property noIndex
	 *
	 * @param _value - new field value
	 */
	public void setNoIndex(boolean _value) {
		noIndex = _value;
	}

	/**
	 * Get value of property noIndex
	 *
	 * @return - value of field noIndex
	 */
	public boolean getNoIndex() {
		return noIndex;
	}

	/* *** Property cbirAlgorithms *** */

	private AomSet<String> cbirAlgorithms = new FastHashSet<String>(
			"cbirAlgorithms", PackageInterface.Meta_cbirAlgorithms, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property cbirAlgorithms
	 *
	 * @param _value - new element value
	 */
	public void setCbirAlgorithms(AomSet<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "cbirAlgorithms"));
		cbirAlgorithms = _value;
	}

	/**
	 * Set value of property cbirAlgorithms
	 *
	 * @param _value - new element value
	 */
	public void setCbirAlgorithms(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "cbirAlgorithms"));
		cbirAlgorithms = new FastHashSet<String>(_value, "cbirAlgorithms",
				PackageInterface.Meta_cbirAlgorithms, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Add an element to set cbirAlgorithms
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addCbirAlgorithms(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "CbirAlgorithms._element"));

		return cbirAlgorithms.add(_element);
	}

	/**
	 * Remove an element from set cbirAlgorithms
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removeCbirAlgorithms(String _element) {
		return cbirAlgorithms.remove(_element);
	}

	/**
	 * Make cbirAlgorithms empty 
	 */
	public void clearCbirAlgorithms() {
		cbirAlgorithms.clear();
	}

	/**
	 * Get value of property cbirAlgorithms
	 *
	 * @return - value of field cbirAlgorithms
	 */
	public AomSet<String> getCbirAlgorithms() {
		return cbirAlgorithms;
	}

	/**
	 * Test if set cbirAlgorithms contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsCbirAlgorithms(String _element) {
		return cbirAlgorithms.contains(_element);
	}

	/* *** Property indexedTextFields *** */

	private AomSet<String> indexedTextFields = new FastHashSet<String>(
			"indexedTextFields", PackageInterface.Meta_indexedTextFields, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property indexedTextFields
	 *
	 * @param _value - new element value
	 */
	public void setIndexedTextFields(AomSet<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "indexedTextFields"));
		indexedTextFields = _value;
	}

	/**
	 * Set value of property indexedTextFields
	 *
	 * @param _value - new element value
	 */
	public void setIndexedTextFields(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "indexedTextFields"));
		indexedTextFields = new FastHashSet<String>(_value,
				"indexedTextFields", PackageInterface.Meta_indexedTextFields,
				0, Integer.MAX_VALUE, null, null);
	}

	/**
	 * Add an element to set indexedTextFields
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addIndexedTextFields(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"IndexedTextFields._element"));

		return indexedTextFields.add(_element);
	}

	/**
	 * Remove an element from set indexedTextFields
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removeIndexedTextFields(String _element) {
		return indexedTextFields.remove(_element);
	}

	/**
	 * Make indexedTextFields empty 
	 */
	public void clearIndexedTextFields() {
		indexedTextFields.clear();
	}

	/**
	 * Get value of property indexedTextFields
	 *
	 * @return - value of field indexedTextFields
	 */
	public AomSet<String> getIndexedTextFields() {
		return indexedTextFields;
	}

	/**
	 * Test if set indexedTextFields contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsIndexedTextFields(String _element) {
		return indexedTextFields.contains(_element);
	}

	/* *** Property personsToKeywords *** */

	private Boolean personsToKeywords;

	/**
	 * Set value of property personsToKeywords
	 *
	 * @param _value - new field value
	 */
	public void setPersonsToKeywords(Boolean _value) {
		personsToKeywords = _value;
	}

	/**
	 * Get value of property personsToKeywords
	 *
	 * @return - value of field personsToKeywords
	 */
	public Boolean getPersonsToKeywords() {
		return personsToKeywords;
	}

	/* *** Property vocabularies *** */

	private AomList<String> vocabularies = new FastArrayList<String>(
			"vocabularies", PackageInterface.Meta_vocabularies, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property vocabularies
	 *
	 * @param _value - new element value
	 */
	public void setVocabularies(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "vocabularies"));
		vocabularies = _value;
	}

	/**
	 * Set value of property vocabularies
	 *
	 * @param _value - new element value
	 */
	public void setVocabularies(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "vocabularies"));
		vocabularies = new FastArrayList<String>(_value, "vocabularies",
				PackageInterface.Meta_vocabularies, 0, Integer.MAX_VALUE, null,
				null);
	}

	/**
	 * Set single element of list vocabularies
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setVocabularies(String _element, int _i) {
		vocabularies.set(_i, _element);
	}

	/**
	 * Add an element to list vocabularies
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addVocabularies(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Vocabularies._element"));

		return vocabularies.add(_element);
	}

	/**
	 * Remove an element from list vocabularies
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeVocabularies(String _element) {
		return vocabularies.remove(_element);
	}

	/**
	 * Make vocabularies empty 
	 */
	public void clearVocabularies() {
		vocabularies.clear();
	}

	/**
	 * Get value of property vocabularies
	 *
	 * @return - value of field vocabularies
	 */
	public AomList<String> getVocabularies() {
		return vocabularies;
	}

	/**
	 * Get single element of list vocabularies
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list vocabularies
	 */
	public String getVocabularies(int _i) {
		return vocabularies.get(_i);
	}

	/* *** Property lastWatchedFolderScan *** */

	private long lastWatchedFolderScan;

	/**
	 * Set value of property lastWatchedFolderScan
	 *
	 * @param _value - new field value
	 */
	public void setLastWatchedFolderScan(long _value) {
		lastWatchedFolderScan = _value;
	}

	/**
	 * Get value of property lastWatchedFolderScan
	 *
	 * @return - value of field lastWatchedFolderScan
	 */
	public long getLastWatchedFolderScan() {
		return lastWatchedFolderScan;
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

		if (!(o instanceof Meta_type) || !super.equals(o))
			return false;
		Meta_type other = (Meta_type) o;
		return getVersion() == other.getVersion()

				&& getRelevantLireVersion() == other.getRelevantLireVersion()

				&& ((getCreationDate() == null && other.getCreationDate() == null) || (getCreationDate() != null && getCreationDate()
						.equals(other.getCreationDate())))

				&& ((getOwner() == null && other.getOwner() == null) || (getOwner() != null && getOwner()
						.equals(other.getOwner())))

				&& ((getThemeID() == null && other.getThemeID() == null) || (getThemeID() != null && getThemeID()
						.equals(other.getThemeID())))

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& ((getUserFieldLabel1() == null && other.getUserFieldLabel1() == null) || (getUserFieldLabel1() != null && getUserFieldLabel1()
						.equals(other.getUserFieldLabel1())))

				&& ((getUserFieldLabel2() == null && other.getUserFieldLabel2() == null) || (getUserFieldLabel2() != null && getUserFieldLabel2()
						.equals(other.getUserFieldLabel2())))

				&& ((getColorLabels() == null && other.getColorLabels() == null) || (getColorLabels() != null && getColorLabels()
						.equals(other.getColorLabels())))

				&& ((getKeywords() == null && other.getKeywords() == null) || (getKeywords() != null && getKeywords()
						.equals(other.getKeywords())))

				&& ((getTimeline() == null && other.getTimeline() == null) || (getTimeline() != null && getTimeline()
						.equals(other.getTimeline())))

				&& ((getLocationFolders() == null && other.getLocationFolders() == null) || (getLocationFolders() != null && getLocationFolders()
						.equals(other.getLocationFolders())))

				&& ((getLastImport() == null && other.getLastImport() == null) || (getLastImport() != null && getLastImport()
						.equals(other.getLastImport())))

				&& getLastSequenceNo() == other.getLastSequenceNo()

				&& getLastYearSequenceNo() == other.getLastYearSequenceNo()

				&& ((getLastBackup() == null && other.getLastBackup() == null) || (getLastBackup() != null && getLastBackup()
						.equals(other.getLastBackup())))

				&& ((getLastBackupFolder() == null && other
						.getLastBackupFolder() == null) || (getLastBackupFolder() != null && getLastBackupFolder()
						.equals(other.getLastBackupFolder())))

				&& ((getBackupLocation() == null && other.getBackupLocation() == null) || (getBackupLocation() != null && getBackupLocation()
						.equals(other.getBackupLocation())))

				&& ((getLastSessionEnd() == null && other.getLastSessionEnd() == null) || (getLastSessionEnd() != null && getLastSessionEnd()
						.equals(other.getLastSessionEnd())))

				&& ((getThumbnailResolution() == null && other
						.getThumbnailResolution() == null) || (getThumbnailResolution() != null && getThumbnailResolution()
						.equals(other.getThumbnailResolution())))

				&& getThumbnailFromPreview() == other.getThumbnailFromPreview()

				&& ((getLastSelection() == null && other.getLastSelection() == null) || (getLastSelection() != null && getLastSelection()
						.equals(other.getLastSelection())))

				&& ((getLastExpansion() == null && other.getLastExpansion() == null) || (getLastExpansion() != null && getLastExpansion()
						.equals(other.getLastExpansion())))

				&& ((getLastCollection() == null && other.getLastCollection() == null) || (getLastCollection() != null && getLastCollection()
						.equals(other.getLastCollection())))

				&& getPauseFolderWatch() == other.getPauseFolderWatch()

				&& getFolderWatchLatency() == other.getFolderWatchLatency()

				&& getCleaned() == other.getCleaned()

				&& ((getPostponed() == null && other.getPostponed() == null) || (getPostponed() != null && getPostponed()
						.equals(other.getPostponed())))

				&& ((getPostponedNaming() == null && other.getPostponedNaming() == null) || (getPostponedNaming() != null && getPostponedNaming()
						.equals(other.getPostponedNaming())))

				&& getReadonly() == other.getReadonly()

				&& getAutoWatch() == other.getAutoWatch()

				&& getSharpen() == other.getSharpen()

				&& ((getLocale() == null && other.getLocale() == null) || (getLocale() != null && getLocale()
						.equals(other.getLocale())))

				&& ((getPlatform() == null && other.getPlatform() == null) || (getPlatform() != null && getPlatform()
						.equals(other.getPlatform())))

				&& ((getLastPicasaScan() == null && other.getLastPicasaScan() == null) || (getLastPicasaScan() != null && getLastPicasaScan()
						.equals(other.getLastPicasaScan())))

				&& getPicasaScannerVersion() == other.getPicasaScannerVersion()

				&& getCumulateImports() == other.getCumulateImports()

				&& getWebpCompression() == other.getWebpCompression()

				&& getJpegQuality() == other.getJpegQuality()

				&& getNoIndex() == other.getNoIndex()

				&& ((getCbirAlgorithms() == null && other.getCbirAlgorithms() == null) || (getCbirAlgorithms() != null && getCbirAlgorithms()
						.equals(other.getCbirAlgorithms())))

				&& ((getIndexedTextFields() == null && other
						.getIndexedTextFields() == null) || (getIndexedTextFields() != null && getIndexedTextFields()
						.equals(other.getIndexedTextFields())))

				&& ((getPersonsToKeywords() == null && other
						.getPersonsToKeywords() == null) || (getPersonsToKeywords() != null && getPersonsToKeywords()
						.equals(other.getPersonsToKeywords())))

				&& ((getVocabularies() == null && other.getVocabularies() == null) || (getVocabularies() != null && getVocabularies()
						.equals(other.getVocabularies())))

				&& getLastWatchedFolderScan() == other
						.getLastWatchedFolderScan()

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

		int hashCode = -1249201876 + getVersion();

		hashCode = 31 * hashCode + getRelevantLireVersion();

		hashCode = 31
				* hashCode
				+ ((getCreationDate() == null) ? 0 : getCreationDate()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getOwner() == null) ? 0 : getOwner().hashCode());

		hashCode = 31 * hashCode
				+ ((getThemeID() == null) ? 0 : getThemeID().hashCode());

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31
				* hashCode
				+ ((getUserFieldLabel1() == null) ? 0 : getUserFieldLabel1()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getUserFieldLabel2() == null) ? 0 : getUserFieldLabel2()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getColorLabels() == null) ? 0 : getColorLabels().hashCode());

		hashCode = 31 * hashCode
				+ ((getKeywords() == null) ? 0 : getKeywords().hashCode());

		hashCode = 31 * hashCode
				+ ((getTimeline() == null) ? 0 : getTimeline().hashCode());

		hashCode = 31
				* hashCode
				+ ((getLocationFolders() == null) ? 0 : getLocationFolders()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getLastImport() == null) ? 0 : getLastImport().hashCode());

		hashCode = 31 * hashCode + getLastSequenceNo();

		hashCode = 31 * hashCode + getLastYearSequenceNo();

		hashCode = 31 * hashCode
				+ ((getLastBackup() == null) ? 0 : getLastBackup().hashCode());

		hashCode = 31
				* hashCode
				+ ((getLastBackupFolder() == null) ? 0 : getLastBackupFolder()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getBackupLocation() == null) ? 0 : getBackupLocation()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getLastSessionEnd() == null) ? 0 : getLastSessionEnd()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getThumbnailResolution() == null) ? 0
						: getThumbnailResolution().hashCode());

		hashCode = 31 * hashCode + (getThumbnailFromPreview() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getLastSelection() == null) ? 0 : getLastSelection()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getLastExpansion() == null) ? 0 : getLastExpansion()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getLastCollection() == null) ? 0 : getLastCollection()
						.hashCode());

		hashCode = 31 * hashCode + (getPauseFolderWatch() ? 1231 : 1237);

		hashCode = 31 * hashCode + getFolderWatchLatency();

		hashCode = 31 * hashCode + (getCleaned() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getPostponed() == null) ? 0 : getPostponed().hashCode());

		hashCode = 31
				* hashCode
				+ ((getPostponedNaming() == null) ? 0 : getPostponedNaming()
						.hashCode());

		hashCode = 31 * hashCode + (getReadonly() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getAutoWatch() ? 1231 : 1237);

		hashCode = 31 * hashCode + getSharpen();

		hashCode = 31 * hashCode
				+ ((getLocale() == null) ? 0 : getLocale().hashCode());

		hashCode = 31 * hashCode
				+ ((getPlatform() == null) ? 0 : getPlatform().hashCode());

		hashCode = 31
				* hashCode
				+ ((getLastPicasaScan() == null) ? 0 : getLastPicasaScan()
						.hashCode());

		hashCode = 31 * hashCode + getPicasaScannerVersion();

		hashCode = 31 * hashCode + (getCumulateImports() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getWebpCompression() ? 1231 : 1237);

		hashCode = 31 * hashCode + getJpegQuality();

		hashCode = 31 * hashCode + (getNoIndex() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getCbirAlgorithms() == null) ? 0 : getCbirAlgorithms()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getIndexedTextFields() == null) ? 0
						: getIndexedTextFields().hashCode());

		hashCode = 31
				* hashCode
				+ ((getPersonsToKeywords() == null) ? 0
						: getPersonsToKeywords().hashCode());

		hashCode = 31
				* hashCode
				+ ((getVocabularies() == null) ? 0 : getVocabularies()
						.hashCode());

		hashCode = 31
				* hashCode
				+ (int) (getLastWatchedFolderScan() ^ (getLastWatchedFolderScan() >>> 32));

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

		if (creationDate == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "creationDate"));

		if (owner == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "owner"));

		if (themeID == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "themeID"));

		if (description == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "description"));

		if (userFieldLabel1 == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "userFieldLabel1"));

		if (userFieldLabel2 == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "userFieldLabel2"));

		if (timeline == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "timeline"));

		if (locationFolders == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "locationFolders"));

		if (lastImport == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastImport"));

		if (lastBackup == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastBackup"));

		if (lastSessionEnd == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastSessionEnd"));

		if (thumbnailResolution == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "thumbnailResolution"));

	}

}
