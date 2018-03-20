package com.bdaum.zoom.cat.model.creatorsContact;

import com.bdaum.aoModeling.runtime.ConstraintException;
import com.bdaum.zoom.cat.model.Contact_typeImpl;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset Contact
 */

/* !! This class is not intended to be modified manually !! */

//Modified manually: no backpointers and other overhead

public class ContactImpl extends Contact_typeImpl implements Contact {

	private static final long serialVersionUID = 1L;


	/* ----- Constructors ----- */


	public ContactImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param city - Property
	 * @param country - Property
	 * @param postalCode - Property
	 * @param state - Property
	 */
	public ContactImpl(String city, String country, String postalCode,
			String state) {
		super(city, country, postalCode, state);

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
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException {
		// do nothing
	}

}
