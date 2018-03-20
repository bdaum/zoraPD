package com.bdaum.zoom.cat.model.artworkOrObjectShown;

import com.bdaum.zoom.cat.model.ArtworkOrObject_typeImpl;
import java.util.*;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset artworkOrObject
 */

/* !! This class is not intended to be modified manually !! */

//Modified manually: no backpointers and other overhead

public class ArtworkOrObjectImpl extends ArtworkOrObject_typeImpl implements
		ArtworkOrObject {


	private static final long serialVersionUID = 6924505287663092229L;


	/* ----- Constructors ----- */

	public ArtworkOrObjectImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param copyrightNotice - Property
	 * @param dateCreated - Property
	 * @param source - Property
	 * @param sourceInventoryNumber - Property
	 * @param title - Property
	 */
	public ArtworkOrObjectImpl(String copyrightNotice, Date dateCreated,
			String source, String sourceInventoryNumber, String title) {
		super(copyrightNotice, dateCreated, source, sourceInventoryNumber,
				title);

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


	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException {
		// do nothing
	}

}
