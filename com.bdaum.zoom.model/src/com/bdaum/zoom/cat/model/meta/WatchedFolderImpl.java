package com.bdaum.zoom.cat.model.meta;

import java.util.*;
import com.bdaum.zoom.cat.model.WatchedFolder_typeImpl;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset watchedFolder
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class WatchedFolderImpl extends WatchedFolder_typeImpl implements
		WatchedFolder {

	static final long serialVersionUID = 2263314745L;

	/* ----- Constructors ----- */

	public WatchedFolderImpl() {
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
	public WatchedFolderImpl(String uri, String volume, long lastObservation,
			boolean recursive, String filters, boolean transfer, String artist,
			boolean skipDuplicates, int skipPolicy, String targetDir,
			int subfolderPolicy, String selectedTemplate, String cue,
			int fileSource) {
		super(uri, volume, lastObservation, recursive, filters, transfer,
				artist, skipDuplicates, skipPolicy, targetDir, subfolderPolicy,
				selectedTemplate, cue, fileSource);

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
		attachInstrumentation(_instrumentation, WatchedFolderImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc meta_parent *** */

	private String meta_parent;

	/**
	 * Set value of property meta_parent
	 *
	 * @param _value - new field value
	 */
	public void setMeta_parent(String _value) {
		meta_parent = _value;
	}

	/**
	 * Get value of property meta_parent
	 *
	 * @return - value of field meta_parent
	 */
	public String getMeta_parent() {
		return meta_parent;
	}

	/* ----- Equality and Identity ----- */

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

	@Override
	public String toString() {
		return getStringId();
	}

}
