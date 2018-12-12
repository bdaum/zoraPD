package com.bdaum.zoom.cat.model.meta;

import com.bdaum.zoom.cat.model.LastDeviceImport_typeImpl;
import java.util.*;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset lastDeviceImport
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class LastDeviceImportImpl extends LastDeviceImport_typeImpl implements
		LastDeviceImport {

	static final long serialVersionUID = -1083586930L;

	/* ----- Constructors ----- */

	public LastDeviceImportImpl() {
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
	public LastDeviceImportImpl(String volume, long timestamp,
			String description, String owner, String path,
			Boolean detectDuplicates, Boolean removeMedia, Integer skipPolicy,
			String targetDir, Integer subfolders, Boolean deepSubfolders,
			String selectedTemplate, String cue, String prefix, Integer privacy) {
		super(volume, timestamp, description, owner, path, detectDuplicates,
				removeMedia, skipPolicy, targetDir, subfolders, deepSubfolders,
				selectedTemplate, cue, prefix, privacy);

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
		attachInstrumentation(_instrumentation, LastDeviceImportImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc meta_parent *** */

	private Meta meta_parent;

	/**
	 * Set value of property meta_parent
	 *
	 * @param _value - new field value
	 */
	public void setMeta_parent(Meta _value) {
		meta_parent = _value;
	}

	/**
	 * Get value of property meta_parent
	 *
	 * @return - value of field meta_parent
	 */
	public Meta getMeta_parent() {
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
		if (!(o instanceof LastDeviceImport))
			return false;
		LastDeviceImport other = (LastDeviceImport) o;
		return ((getVolume() == null && other.getVolume() == null) || (getVolume() != null && getVolume()
				.equals(other.getVolume())));
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	public int keyHashCode() {
		return ((getVolume() == null) ? 0 : getVolume().hashCode());
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
