package com.bdaum.zoom.cat.model;

import java.util.*;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset bookmark
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class BookmarkImpl extends Bookmark_typeImpl implements Bookmark {

	static final long serialVersionUID = 634429144L;

	/* ----- Constructors ----- */

	public BookmarkImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param label - Property
	 * @param assetId - Property
	 * @param collectionId - Property
	 * @param createdAt - Property
	 * @param peer - Property
	 * @param catFile - Property
	 */
	public BookmarkImpl(String label, String assetId, String collectionId,
			Date createdAt, String peer, String catFile) {
		super(label, assetId, collectionId, createdAt, peer, catFile);

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
		attachInstrumentation(_instrumentation, BookmarkImpl.class, properties,
				aspect);
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

}
