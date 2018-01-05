package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset meta
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Meta_type extends AomValueChangedNotifier, IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property version
	 *
	 * @param _value - new element value
	 */
	public void setVersion(int _value);

	/**
	 * Get value of property version
	 *
	 * @return - value of field version
	 */
	public int getVersion();

	/**
	 * Set value of property relevantLireVersion
	 *
	 * @param _value - new element value
	 */
	public void setRelevantLireVersion(int _value);

	/**
	 * Get value of property relevantLireVersion
	 *
	 * @return - value of field relevantLireVersion
	 */
	public int getRelevantLireVersion();

	/**
	 * Set value of property creationDate
	 *
	 * @param _value - new element value
	 */
	public void setCreationDate(Date _value);

	/**
	 * Get value of property creationDate
	 *
	 * @return - value of field creationDate
	 */
	public Date getCreationDate();

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
	 * Set value of property themeID
	 *
	 * @param _value - new element value
	 */
	public void setThemeID(String _value);

	/**
	 * Get value of property themeID
	 *
	 * @return - value of field themeID
	 */
	public String getThemeID();

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
	 * Set value of property userFieldLabel1
	 *
	 * @param _value - new element value
	 */
	public void setUserFieldLabel1(String _value);

	/**
	 * Get value of property userFieldLabel1
	 *
	 * @return - value of field userFieldLabel1
	 */
	public String getUserFieldLabel1();

	/**
	 * Set value of property userFieldLabel2
	 *
	 * @param _value - new element value
	 */
	public void setUserFieldLabel2(String _value);

	/**
	 * Get value of property userFieldLabel2
	 *
	 * @return - value of field userFieldLabel2
	 */
	public String getUserFieldLabel2();

	/**
	 * Set value of property colorLabels
	 *
	 * @param _value - new element value
	 */
	public void setColorLabels(Collection<String> _value);

	/**
	 * Set single element of list colorLabels
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setColorLabels(String _value, int _i);

	/**
	 * Add an element to list colorLabels
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addColorLabels(String _element);

	/**
	 * Remove an element from list colorLabels
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeColorLabels(String _element);

	/**
	 * Make colorLabels empty 
	 */
	public void clearColorLabels();

	/**
	 * Get value of property colorLabels
	 *
	 * @return - value of field colorLabels
	 */
	public AomList<String> getColorLabels();

	/**
	 * Get single element of list colorLabels
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list colorLabels
	 */
	public String getColorLabels(int _i);

	/**
	 * Set value of property keywords
	 *
	 * @param _value - new element value
	 */
	public void setKeywords(Collection<String> _value);

	/**
	 * Add an element to set keywords
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addKeywords(String _element);

	/**
	 * Remove an element from set keywords
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removeKeywords(String _element);

	/**
	 * Make keywords empty 
	 */
	public void clearKeywords();

	/**
	 * Get value of property keywords
	 *
	 * @return - value of field keywords
	 */
	public AomSet<String> getKeywords();

	/**
	 * Test if set keywords contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsKeywords(String _element);

	public static final String timeline_no = "no";
	public static final String timeline_year = "year";
	public static final String timeline_month = "month";
	public static final String timeline_day = "day";
	public static final String timeline_week = "week";
	public static final String timeline_weekAndDay = "weekAndDay";

	public static final String[] timelineALLVALUES = new String[] {
			timeline_no, timeline_year, timeline_month, timeline_day,
			timeline_week, timeline_weekAndDay };

	/**
	 * Set value of property timeline
	 *
	 * @param _value - new element value
	 */
	public void setTimeline(String _value);

	/**
	 * Get value of property timeline
	 *
	 * @return - value of field timeline
	 */
	public String getTimeline();

	public static final String locationFolders_no = "no";
	public static final String locationFolders_country = "country";
	public static final String locationFolders_state = "state";
	public static final String locationFolders_city = "city";

	public static final String[] locationFoldersALLVALUES = new String[] {
			locationFolders_no, locationFolders_country, locationFolders_state,
			locationFolders_city };

	/**
	 * Set value of property locationFolders
	 *
	 * @param _value - new element value
	 */
	public void setLocationFolders(String _value);

	/**
	 * Get value of property locationFolders
	 *
	 * @return - value of field locationFolders
	 */
	public String getLocationFolders();

	/**
	 * Set value of property lastImport
	 *
	 * @param _value - new element value
	 */
	public void setLastImport(Date _value);

	/**
	 * Get value of property lastImport
	 *
	 * @return - value of field lastImport
	 */
	public Date getLastImport();

	/**
	 * Set value of property lastSequenceNo
	 *
	 * @param _value - new element value
	 */
	public void setLastSequenceNo(int _value);

	/**
	 * Get value of property lastSequenceNo
	 *
	 * @return - value of field lastSequenceNo
	 */
	public int getLastSequenceNo();

	/**
	 * Set value of property lastYearSequenceNo
	 *
	 * @param _value - new element value
	 */
	public void setLastYearSequenceNo(int _value);

	/**
	 * Get value of property lastYearSequenceNo
	 *
	 * @return - value of field lastYearSequenceNo
	 */
	public int getLastYearSequenceNo();

	/**
	 * Set value of property lastBackup
	 *
	 * @param _value - new element value
	 */
	public void setLastBackup(Date _value);

	/**
	 * Get value of property lastBackup
	 *
	 * @return - value of field lastBackup
	 */
	public Date getLastBackup();

	/**
	 * Set value of property lastBackupFolder
	 *
	 * @param _value - new element value
	 */
	public void setLastBackupFolder(String _value);

	/**
	 * Get value of property lastBackupFolder
	 *
	 * @return - value of field lastBackupFolder
	 */
	public String getLastBackupFolder();

	/**
	 * Set value of property backupLocation
	 *
	 * @param _value - new element value
	 */
	public void setBackupLocation(String _value);

	/**
	 * Get value of property backupLocation
	 *
	 * @return - value of field backupLocation
	 */
	public String getBackupLocation();

	/**
	 * Set value of property lastSessionEnd
	 *
	 * @param _value - new element value
	 */
	public void setLastSessionEnd(Date _value);

	/**
	 * Get value of property lastSessionEnd
	 *
	 * @return - value of field lastSessionEnd
	 */
	public Date getLastSessionEnd();

	public static final String thumbnailResolution_low = "low";
	public static final String thumbnailResolution_medium = "medium";
	public static final String thumbnailResolution_high = "high";
	public static final String thumbnailResolution_veryHigh = "veryHigh";

	public static final String[] thumbnailResolutionALLVALUES = new String[] {
			thumbnailResolution_low, thumbnailResolution_medium,
			thumbnailResolution_high, thumbnailResolution_veryHigh };

	/**
	 * Set value of property thumbnailResolution
	 *
	 * @param _value - new element value
	 */
	public void setThumbnailResolution(String _value);

	/**
	 * Get value of property thumbnailResolution
	 *
	 * @return - value of field thumbnailResolution
	 */
	public String getThumbnailResolution();

	/**
	 * Set value of property thumbnailFromPreview
	 *
	 * @param _value - new element value
	 */
	public void setThumbnailFromPreview(boolean _value);

	/**
	 * Get value of property thumbnailFromPreview
	 *
	 * @return - value of field thumbnailFromPreview
	 */
	public boolean getThumbnailFromPreview();

	/**
	 * Set value of property lastSelection
	 *
	 * @param _value - new element value
	 */
	public void setLastSelection(String _value);

	/**
	 * Get value of property lastSelection
	 *
	 * @return - value of field lastSelection
	 */
	public String getLastSelection();

	/**
	 * Set value of property lastExpansion
	 *
	 * @param _value - new element value
	 */
	public void setLastExpansion(Collection<String> _value);

	/**
	 * Set single element of list lastExpansion
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setLastExpansion(String _value, int _i);

	/**
	 * Add an element to list lastExpansion
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addLastExpansion(String _element);

	/**
	 * Remove an element from list lastExpansion
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeLastExpansion(String _element);

	/**
	 * Make lastExpansion empty 
	 */
	public void clearLastExpansion();

	/**
	 * Get value of property lastExpansion
	 *
	 * @return - value of field lastExpansion
	 */
	public AomList<String> getLastExpansion();

	/**
	 * Get single element of list lastExpansion
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list lastExpansion
	 */
	public String getLastExpansion(int _i);

	/**
	 * Set value of property lastCollection
	 *
	 * @param _value - new element value
	 */
	public void setLastCollection(String _value);

	/**
	 * Get value of property lastCollection
	 *
	 * @return - value of field lastCollection
	 */
	public String getLastCollection();

	/**
	 * Set value of property pauseFolderWatch
	 *
	 * @param _value - new element value
	 */
	public void setPauseFolderWatch(boolean _value);

	/**
	 * Get value of property pauseFolderWatch
	 *
	 * @return - value of field pauseFolderWatch
	 */
	public boolean getPauseFolderWatch();

	/**
	 * Set value of property folderWatchLatency
	 *
	 * @param _value - new element value
	 */
	public void setFolderWatchLatency(int _value);

	/**
	 * Get value of property folderWatchLatency
	 *
	 * @return - value of field folderWatchLatency
	 */
	public int getFolderWatchLatency();

	/**
	 * Set value of property cleaned
	 *
	 * @param _value - new element value
	 */
	public void setCleaned(boolean _value);

	/**
	 * Get value of property cleaned
	 *
	 * @return - value of field cleaned
	 */
	public boolean getCleaned();

	/**
	 * Set value of property postponed
	 *
	 * @param _value - new element value
	 */
	public void setPostponed(Collection<String> _value);

	/**
	 * Add an element to set postponed
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addPostponed(String _element);

	/**
	 * Remove an element from set postponed
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removePostponed(String _element);

	/**
	 * Make postponed empty 
	 */
	public void clearPostponed();

	/**
	 * Get value of property postponed
	 *
	 * @return - value of field postponed
	 */
	public AomSet<String> getPostponed();

	/**
	 * Test if set postponed contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsPostponed(String _element);

	/**
	 * Set value of property postponedNaming
	 *
	 * @param _value - new element value
	 */
	public void setPostponedNaming(Collection<String> _value);

	/**
	 * Add an element to set postponedNaming
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addPostponedNaming(String _element);

	/**
	 * Remove an element from set postponedNaming
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removePostponedNaming(String _element);

	/**
	 * Make postponedNaming empty 
	 */
	public void clearPostponedNaming();

	/**
	 * Get value of property postponedNaming
	 *
	 * @return - value of field postponedNaming
	 */
	public AomSet<String> getPostponedNaming();

	/**
	 * Test if set postponedNaming contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsPostponedNaming(String _element);

	/**
	 * Set value of property readonly
	 *
	 * @param _value - new element value
	 */
	public void setReadonly(boolean _value);

	/**
	 * Get value of property readonly
	 *
	 * @return - value of field readonly
	 */
	public boolean getReadonly();

	/**
	 * Set value of property autoWatch
	 *
	 * @param _value - new element value
	 */
	public void setAutoWatch(boolean _value);

	/**
	 * Get value of property autoWatch
	 *
	 * @return - value of field autoWatch
	 */
	public boolean getAutoWatch();

	/**
	 * Set value of property sharpen
	 *
	 * @param _value - new element value
	 */
	public void setSharpen(int _value);

	/**
	 * Get value of property sharpen
	 *
	 * @return - value of field sharpen
	 */
	public int getSharpen();

	/**
	 * Set value of property locale
	 *
	 * @param _value - new element value
	 */
	public void setLocale(String _value);

	/**
	 * Get value of property locale
	 *
	 * @return - value of field locale
	 */
	public String getLocale();

	/**
	 * Set value of property platform
	 *
	 * @param _value - new element value
	 */
	public void setPlatform(String _value);

	/**
	 * Get value of property platform
	 *
	 * @return - value of field platform
	 */
	public String getPlatform();

	/**
	 * Set value of property lastPicasaScan
	 *
	 * @param _value - new element value
	 */
	public void setLastPicasaScan(Date _value);

	/**
	 * Get value of property lastPicasaScan
	 *
	 * @return - value of field lastPicasaScan
	 */
	public Date getLastPicasaScan();

	/**
	 * Set value of property picasaScannerVersion
	 *
	 * @param _value - new element value
	 */
	public void setPicasaScannerVersion(int _value);

	/**
	 * Get value of property picasaScannerVersion
	 *
	 * @return - value of field picasaScannerVersion
	 */
	public int getPicasaScannerVersion();

	/**
	 * Set value of property cumulateImports
	 *
	 * @param _value - new element value
	 */
	public void setCumulateImports(boolean _value);

	/**
	 * Get value of property cumulateImports
	 *
	 * @return - value of field cumulateImports
	 */
	public boolean getCumulateImports();

	/**
	 * Set value of property webpCompression
	 *
	 * @param _value - new element value
	 */
	public void setWebpCompression(boolean _value);

	/**
	 * Get value of property webpCompression
	 *
	 * @return - value of field webpCompression
	 */
	public boolean getWebpCompression();

	/**
	 * Set value of property jpegQuality
	 *
	 * @param _value - new element value
	 */
	public void setJpegQuality(int _value);

	/**
	 * Get value of property jpegQuality
	 *
	 * @return - value of field jpegQuality
	 */
	public int getJpegQuality();

	/**
	 * Set value of property noIndex
	 *
	 * @param _value - new element value
	 */
	public void setNoIndex(boolean _value);

	/**
	 * Get value of property noIndex
	 *
	 * @return - value of field noIndex
	 */
	public boolean getNoIndex();

	/**
	 * Set value of property cbirAlgorithms
	 *
	 * @param _value - new element value
	 */
	public void setCbirAlgorithms(Collection<String> _value);

	/**
	 * Add an element to set cbirAlgorithms
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addCbirAlgorithms(String _element);

	/**
	 * Remove an element from set cbirAlgorithms
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removeCbirAlgorithms(String _element);

	/**
	 * Make cbirAlgorithms empty 
	 */
	public void clearCbirAlgorithms();

	/**
	 * Get value of property cbirAlgorithms
	 *
	 * @return - value of field cbirAlgorithms
	 */
	public AomSet<String> getCbirAlgorithms();

	/**
	 * Test if set cbirAlgorithms contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsCbirAlgorithms(String _element);

	/**
	 * Set value of property indexedTextFields
	 *
	 * @param _value - new element value
	 */
	public void setIndexedTextFields(Collection<String> _value);

	/**
	 * Add an element to set indexedTextFields
	 *
	 * @param _element - the element to add
	 * @return - true if the set did not already contain the specified element.
	 */
	public boolean addIndexedTextFields(String _element);

	/**
	 * Remove an element from set indexedTextFields
	 *
	 * @param _element - the element to remove
	 * @return - true if the set contained the specified element.
	 */
	public boolean removeIndexedTextFields(String _element);

	/**
	 * Make indexedTextFields empty 
	 */
	public void clearIndexedTextFields();

	/**
	 * Get value of property indexedTextFields
	 *
	 * @return - value of field indexedTextFields
	 */
	public AomSet<String> getIndexedTextFields();

	/**
	 * Test if set indexedTextFields contains specified element
	 *
	 * @param _element - the _element to be tested
	 * @return - true if the set contains the specified element
	 */
	public boolean containsIndexedTextFields(String _element);

	/**
	 * Set value of property personsToKeywords
	 *
	 * @param _value - new element value
	 */
	public void setPersonsToKeywords(Boolean _value);

	/**
	 * Get value of property personsToKeywords
	 *
	 * @return - value of field personsToKeywords
	 */
	public Boolean getPersonsToKeywords();

	/**
	 * Set value of property vocabularies
	 *
	 * @param _value - new element value
	 */
	public void setVocabularies(Collection<String> _value);

	/**
	 * Set single element of list vocabularies
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setVocabularies(String _value, int _i);

	/**
	 * Add an element to list vocabularies
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addVocabularies(String _element);

	/**
	 * Remove an element from list vocabularies
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeVocabularies(String _element);

	/**
	 * Make vocabularies empty 
	 */
	public void clearVocabularies();

	/**
	 * Get value of property vocabularies
	 *
	 * @return - value of field vocabularies
	 */
	public AomList<String> getVocabularies();

	/**
	 * Get single element of list vocabularies
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list vocabularies
	 */
	public String getVocabularies(int _i);

	/**
	 * Set value of property lastWatchedFolderScan
	 *
	 * @param _value - new element value
	 */
	public void setLastWatchedFolderScan(long _value);

	/**
	 * Get value of property lastWatchedFolderScan
	 *
	 * @return - value of field lastWatchedFolderScan
	 */
	public long getLastWatchedFolderScan();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
