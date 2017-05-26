package com.bdaum.zoom.cat.model.meta;

import java.util.*;
import java.lang.String;
import com.bdaum.zoom.cat.model.Meta_typeImpl;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset meta
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class MetaImpl extends Meta_typeImpl implements Meta {

	static final long serialVersionUID = 546961218L;

	/* ----- Constructors ----- */

	public MetaImpl() {
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
	 */
	public MetaImpl(int version, int relevantLireVersion, Date creationDate,
			String owner, String themeID, String description,
			String userFieldLabel1, String userFieldLabel2, String timeline,
			String locationFolders, Date lastImport, int lastSequenceNo,
			int lastYearSequenceNo, Date lastBackup, String lastBackupFolder,
			String backupLocation, Date lastSessionEnd,
			String thumbnailResolution, boolean thumbnailFromPreview,
			String lastSelection, String lastCollection,
			boolean pauseFolderWatch, int folderWatchLatency, boolean cleaned,
			boolean readonly, boolean autoWatch, int sharpen, String locale,
			String platform, Date lastPicasaScan, int picasaScannerVersion,
			boolean cumulateImports, boolean webpCompression, int jpegQuality,
			boolean noIndex, Boolean personsToKeywords) {
		super(version, relevantLireVersion, creationDate, owner, themeID,
				description, userFieldLabel1, userFieldLabel2, timeline,
				locationFolders, lastImport, lastSequenceNo,
				lastYearSequenceNo, lastBackup, lastBackupFolder,
				backupLocation, lastSessionEnd, thumbnailResolution,
				thumbnailFromPreview, lastSelection, lastCollection,
				pauseFolderWatch, folderWatchLatency, cleaned, readonly,
				autoWatch, sharpen, locale, platform, lastPicasaScan,
				picasaScannerVersion, cumulateImports, webpCompression,
				jpegQuality, noIndex, personsToKeywords);

	}

	/* ----- Initialisation ----- */

	private static List<Instrumentation> _instrumentation = new ArrayList<Instrumentation>();

	public static void attachInstrumentation(int point, Aspect aspect,
			Object extension) {
		attachInstrumentation(_instrumentation, point, aspect, extension);
	}

	public static void attachInstrumentation(int point, Aspect aspect) {
		attachInstrumentation(_instrumentation, point, aspect);
	}

	public static void attachInstrumentation(Properties properties,
			Aspect aspect) {
		attachInstrumentation(_instrumentation, MetaImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Arc category *** */

	private AomMap<String, Category> category = new FastHashMap<String, Category>(
			"category", PackageInterface.Meta_category, 0, Integer.MAX_VALUE,
			null, null);

	/**
	 * Set value of property category
	 *
	 * @param _value - new element value
	 */
	public void setCategory(AomMap<String, Category> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "category"));
		category = _value;
		for (Category _element : _value.values()) {
			if (_element != null)
				_element.setMeta_parent(this);

		}
	}

	/**
	 * Set value of property category
	 *
	 * @param _value - new element value
	 */
	public void setCategory(Map<String, Category> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "category"));
		category = new FastHashMap<String, Category>(_value, "category",
				PackageInterface.Meta_category, 0, Integer.MAX_VALUE, null,
				null);

		for (Category _element : _value.values()) {
			if (_element != null)
				_element.setMeta_parent(this);
		}
	}

	/**
	 * Add an element to map category under key _element.getLabel()
	 *
	 * @param _element - the element to add
	 * @return - the previous element stored under that key
	 */
	public Category putCategory(Category _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Category._element"));
		_element.setMeta_parent(this);

		return (Category) category.put(_element.getLabel(), _element);
	}

	/**
	 * Remove an element from map category
	 *
	 * @param _key - the key of the element to remove
	 * @return - the previous element stored under that key
	 */
	public Category removeCategory(String _key) {
		return (Category) category.remove(_key);
	}

	/**
	 * Make category empty 
	 */
	public void clearCategory() {
		category.clear();
	}

	/**
	 * Get value of property category
	 *
	 * @return - value of field category
	 */
	public AomMap<String, Category> getCategory() {
		return category;
	}

	/**
	 * Get single element of map category
	 *
	 * @param _key - the key of the element
	 * @return - the element belonging to the specified key
	 */
	public Category getCategory(String _key) {
		return category.get(_key);
	}

	/* *** Arc watchedFolder *** */

	private AomList<String> watchedFolder = new FastArrayList<String>(
			"watchedFolder", PackageInterface.Meta_watchedFolder, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property watchedFolder
	 *
	 * @param _value - new element value
	 */
	public void setWatchedFolder(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "watchedFolder"));
		watchedFolder = _value;
	}

	/**
	 * Set value of property watchedFolder
	 *
	 * @param _value - new element value
	 */
	public void setWatchedFolder(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "watchedFolder"));
		watchedFolder = new FastArrayList<String>(_value, "watchedFolder",
				PackageInterface.Meta_watchedFolder, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Set single element of list watchedFolder
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setWatchedFolder(String _element, int _i) {
		watchedFolder.set(_i, _element);
	}

	/**
	 * Add an element to list watchedFolder
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addWatchedFolder(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "WatchedFolder._element"));

		return watchedFolder.add(_element);
	}

	/**
	 * Remove an element from list watchedFolder
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeWatchedFolder(String _element) {
		return watchedFolder.remove(_element);
	}

	/**
	 * Make watchedFolder empty 
	 */
	public void clearWatchedFolder() {
		watchedFolder.clear();
	}

	/**
	 * Get value of property watchedFolder
	 *
	 * @return - value of field watchedFolder
	 */
	public AomList<String> getWatchedFolder() {
		return watchedFolder;
	}

	/**
	 * Get single element of list watchedFolder
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list watchedFolder
	 */
	public String getWatchedFolder(int _i) {
		return watchedFolder.get(_i);
	}

	/* *** Arc lastDeviceImport *** */

	private AomMap<String, LastDeviceImport> lastDeviceImport = new FastHashMap<String, LastDeviceImport>(
			"lastDeviceImport", PackageInterface.Meta_lastDeviceImport, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property lastDeviceImport
	 *
	 * @param _value - new element value
	 */
	public void setLastDeviceImport(AomMap<String, LastDeviceImport> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastDeviceImport"));
		lastDeviceImport = _value;
		for (LastDeviceImport _element : _value.values()) {
			if (_element != null)
				_element.setMeta_parent(this);

		}
	}

	/**
	 * Set value of property lastDeviceImport
	 *
	 * @param _value - new element value
	 */
	public void setLastDeviceImport(Map<String, LastDeviceImport> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastDeviceImport"));
		lastDeviceImport = new FastHashMap<String, LastDeviceImport>(_value,
				"lastDeviceImport", PackageInterface.Meta_lastDeviceImport, 0,
				Integer.MAX_VALUE, null, null);

		for (LastDeviceImport _element : _value.values()) {
			if (_element != null)
				_element.setMeta_parent(this);
		}
	}

	/**
	 * Add an element to map lastDeviceImport under key _element.getVolume()
	 *
	 * @param _element - the element to add
	 * @return - the previous element stored under that key
	 */
	public LastDeviceImport putLastDeviceImport(LastDeviceImport _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"LastDeviceImport._element"));
		_element.setMeta_parent(this);

		return (LastDeviceImport) lastDeviceImport.put(_element.getVolume(),
				_element);
	}

	/**
	 * Remove an element from map lastDeviceImport
	 *
	 * @param _key - the key of the element to remove
	 * @return - the previous element stored under that key
	 */
	public LastDeviceImport removeLastDeviceImport(String _key) {
		return (LastDeviceImport) lastDeviceImport.remove(_key);
	}

	/**
	 * Make lastDeviceImport empty 
	 */
	public void clearLastDeviceImport() {
		lastDeviceImport.clear();
	}

	/**
	 * Get value of property lastDeviceImport
	 *
	 * @return - value of field lastDeviceImport
	 */
	public AomMap<String, LastDeviceImport> getLastDeviceImport() {
		return lastDeviceImport;
	}

	/**
	 * Get single element of map lastDeviceImport
	 *
	 * @param _key - the key of the element
	 * @return - the element belonging to the specified key
	 */
	public LastDeviceImport getLastDeviceImport(String _key) {
		return lastDeviceImport.get(_key);
	}

	/* ----- Equality and Identity ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof Meta) || !super.equals(o))
			return false;
		Meta other = (Meta) o;
		return ((getCategory() == null && other.getCategory() == null) || (getCategory() != null && getCategory()
				.equals(other.getCategory())))

				&& ((getWatchedFolder() == null && other.getWatchedFolder() == null) || (getWatchedFolder() != null && getWatchedFolder()
						.equals(other.getWatchedFolder())))

				&& ((getLastDeviceImport() == null && other
						.getLastDeviceImport() == null) || (getLastDeviceImport() != null && getLastDeviceImport()
						.equals(other.getLastDeviceImport())))

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

		int hashCode = super.hashCode() * 31
				+ ((getCategory() == null) ? 0 : getCategory().hashCode());

		hashCode = 31
				* hashCode
				+ ((getWatchedFolder() == null) ? 0 : getWatchedFolder()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getLastDeviceImport() == null) ? 0 : getLastDeviceImport()
						.hashCode());

		return hashCode;
	}

	/**
	 * Compares the specified object with this object for primary key equality.
	 *
	 * @param o the object to be compared with this object
	 * @return true if the specified object is key-identical to this object
	 * @see com.bdaum.aoModeling.runtime.IAsset#isKeyIdentical
	 */
	public boolean isKeyIdentical(Object o) {
		return this == o;
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	public int keyHashCode() {
		return hashCode();
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

		super.validateCompleteness();
	}

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException {
		validateCompleteness();
	}

}
